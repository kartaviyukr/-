package com.kartaviy.tgagent.agent

import com.kartaviy.tgagent.Prefs
import com.kartaviy.tgagent.tg.Tg
import com.kartaviy.tgagent.tg.contentText
import com.kartaviy.tgagent.tg.formattedText
import org.drinkless.tdlib.TdApi
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Что агенту нужно от интерфейса. */
interface AgentUi {
    /** Показать пользователю действие и дождаться ответа «Выполнить» / «Отмена». */
    suspend fun confirm(description: String): Boolean
    suspend fun showResults(title: String, items: List<FoundMessage>)
    suspend fun note(text: String)
}

private val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun formatDate(epoch: Int): String =
    dateFmt.format(Instant.ofEpochSecond(epoch.toLong()).atZone(ZoneId.systemDefault()))

/** "2026-10-27" или "2026-10-27T09:00" (местное время) → unix-время. */
fun parseDate(s: String?, endOfDay: Boolean = false): Int? {
    if (s.isNullOrBlank()) return null
    val t = s.trim().replace(' ', 'T')
    val zone = ZoneId.systemDefault()
    return try {
        if (t.length <= 10) {
            val d = LocalDate.parse(t)
            (if (endOfDay) d.atTime(23, 59, 59) else d.atStartOfDay()).atZone(zone).toEpochSecond().toInt()
        } else {
            LocalDateTime.parse(t.take(19)).atZone(zone).toEpochSecond().toInt()
        }
    } catch (e: Exception) {
        throw IllegalArgumentException("Не понял дату «$s», нужен формат YYYY-MM-DD или YYYY-MM-DDTHH:MM")
    }
}

private fun JSONObject.optLongOrNull(key: String): Long? = if (has(key) && !isNull(key)) optLong(key) else null
private fun JSONObject.optStr(key: String): String? = if (has(key) && !isNull(key)) optString(key).ifBlank { null } else null
private fun JSONObject.longList(key: String): List<Long> {
    val arr = optJSONArray(key) ?: return emptyList()
    return (0 until arr.length()).map { arr.getLong(it) }
}

class ToolExecutor(private val tg: Tg, private val prefs: Prefs, private val ui: AgentUi) {
    private val searcher = Searcher(tg)

    suspend fun run(name: String, a: JSONObject): String {
        val result: Any = when (name) {
            "find_chats" -> findChats(a.getString("query"), a.optInt("limit", 10))
            "list_chats" -> listChats(a)
            "search_messages" -> searchMessages(a)
            "send_message" -> sendMessage(a.getLong("chat_id"), a.getString("text"), null)
            "schedule_message" -> sendMessage(a.getLong("chat_id"), a.getString("text"), parseDate(a.getString("send_at")))
            "list_scheduled" -> listScheduled(a.optLongOrNull("chat_id"))
            "reschedule_message" -> reschedule(a.getLong("chat_id"), a.getLong("message_id"), parseDate(a.getString("send_at"))!!)
            "cancel_scheduled" -> cancelScheduled(a.getLong("chat_id"), a.longList("message_ids"))
            "list_folders" -> listFolders()
            "folder_chats" -> folderChats(a.getInt("folder_id"))
            "create_folder" -> createFolder(a.getString("name"), a.longList("chat_ids"))
            "add_to_folder" -> editFolder(a.getInt("folder_id"), add = a.longList("chat_ids"))
            "remove_from_folder" -> editFolder(a.getInt("folder_id"), remove = a.longList("chat_ids"))
            "rename_folder" -> editFolder(a.getInt("folder_id"), newName = a.getString("name"))
            "delete_folder" -> deleteFolder(a.getInt("folder_id"))
            else -> JSONObject().put("error", "Нет такого инструмента: $name")
        }
        return result.toString().take(15_000)
    }

    private val cancelled = JSONObject().put("status", "cancelled_by_user")

    // ---------- Чаты ----------

    private suspend fun chatJson(id: Long): JSONObject {
        val chat = tg.chat(id)
        val o = JSONObject().put("chat_id", id).put("title", tg.chatTitle(id)).put("type", tg.chatType(chat))
        val type = chat.type
        if (type is TdApi.ChatTypePrivate) {
            tg.user(type.userId)?.usernames?.activeUsernames?.firstOrNull()?.let { o.put("username", "@$it") }
        }
        return o
    }

    private suspend fun findChats(query: String, limit: Int): JSONArray {
        val ids = LinkedHashSet<Long>()
        if (query.lowercase() in listOf("избранное", "saved", "saved messages", "я", "себе") && tg.myUserId != 0L) {
            ids += tg.myUserId
        }
        ids += runCatching { tg.send(TdApi.SearchChats(query, null, limit)).chatIds.toList() }.getOrDefault(emptyList())
        // Подстрахуемся локальным поиском по названию (TDLib ищет по началу слов).
        tg.chats.values.filter { it.title.contains(query, ignoreCase = true) }.take(limit).forEach { ids += it.id }
        if (ids.isEmpty()) {
            ids += runCatching { tg.send(TdApi.SearchChatsOnServer(query, null, limit)).chatIds.toList() }.getOrDefault(emptyList())
        }
        val arr = JSONArray()
        ids.take(limit).forEach { arr.put(chatJson(it)) }
        return arr
    }

    private suspend fun chatIdsOf(list: TdApi.ChatList, limit: Int): List<Long> =
        tg.send(TdApi.GetChats(list, limit)).chatIds.toList()

    private suspend fun listChats(a: JSONObject): JSONArray {
        val limit = a.optInt("limit", 50)
        val list: TdApi.ChatList = when {
            a.has("folder_id") && !a.isNull("folder_id") -> TdApi.ChatListFolder(a.getInt("folder_id"))
            a.optBoolean("archive") -> TdApi.ChatListArchive()
            else -> TdApi.ChatListMain()
        }
        val arr = JSONArray()
        for (id in chatIdsOf(list, limit)) {
            arr.put(chatJson(id).put("unread", tg.chat(id).unreadCount))
        }
        return arr
    }

    // ---------- Поиск ----------

    private suspend fun searchMessages(a: JSONObject): JSONObject {
        val filter = MsgFilter(
            text = a.optStr("text"),
            forwardedOnly = a.optBoolean("forwarded_only") || a.optStr("forwarded_from") != null,
            forwardedFrom = a.optStr("forwarded_from"),
            sender = a.optStr("sender"),
            contentType = a.optStr("content_type"),
            dateFrom = parseDate(a.optStr("date_from")),
            dateTo = parseDate(a.optStr("date_to"), endOfDay = true),
        )
        val limit = a.optInt("limit", 200).coerceIn(1, 2000)
        val budget = Searcher.Budget(a.optInt("scan_limit", 5000).coerceIn(100, 50_000))
        val out = ArrayList<FoundMessage>()

        val chatIds = a.longList("chat_ids").toMutableList()
        val folderId = a.optLongOrNull("folder_id")?.toInt()
        if (folderId != null) chatIds += chatIdsOf(TdApi.ChatListFolder(folderId), 500)
        val allChats = a.optBoolean("all_chats") || (chatIds.isEmpty() && folderId == null)

        when {
            allChats && filter.text != null -> {
                ui.note("🔎 Ищу «${filter.text}» по всем чатам…")
                searcher.global(filter, a.optStr("chat_type"), limit, budget, out)
            }
            allChats -> {
                // Без текста серверный поиск невозможен — листаем недавние чаты, пока хватает бюджета.
                ui.note("🔎 Просматриваю историю всех чатов…")
                for (id in chatIdsOf(TdApi.ChatListMain(), 300)) {
                    if (out.size >= limit || budget.left <= 0) break
                    val perChat = Searcher.Budget(minOf(budget.left, 500))
                    val before = perChat.left
                    runCatching { searcher.inChat(id, filter, limit, perChat, out) }
                    budget.left -= before - perChat.left
                }
            }
            else -> {
                ui.note("🔎 Ищу в ${chatIds.size} чат(ах)…")
                for (id in chatIds.distinct()) {
                    if (out.size >= limit || budget.left <= 0) break
                    searcher.inChat(id, filter, limit, budget, out)
                }
            }
        }

        out.sortByDescending { it.date }
        ui.showResults(describe(filter), out)

        val items = JSONArray()
        out.take(40).forEach { m ->
            val o = JSONObject()
                .put("chat", m.chatTitle).put("chat_id", m.chatId).put("message_id", m.messageId)
                .put("date", formatDate(m.date)).put("from", m.sender).put("type", m.type)
            m.forwardedFrom?.let { o.put("forwarded_from", it) }
            if (!prefs.privateMode) o.put("text", m.text.take(200))
            items.put(o)
        }
        return JSONObject()
            .put("found", out.size)
            .put("scan_budget_exhausted", budget.left <= 0)
            .put("note", if (prefs.privateMode) "Текст скрыт приватным режимом; пользователь видит полный список в приложении." else "Пользователь видит полный список в приложении.")
            .put("first_items", items)
    }

    private fun describe(f: MsgFilter): String = buildList {
        f.text?.let { add("текст «$it»") }
        if (f.forwardedOnly) add(if (f.forwardedFrom != null) "переслано от «${f.forwardedFrom}»" else "пересланные")
        f.sender?.let { add("отправитель «$it»") }
        f.contentType?.let { add("тип: $it") }
        f.dateFrom?.let { add("с ${formatDate(it)}") }
        f.dateTo?.let { add("по ${formatDate(it)}") }
    }.joinToString(", ").ifBlank { "Все сообщения" }

    // ---------- Отправка и отложенные ----------

    private suspend fun sendMessage(chatId: Long, text: String, sendAt: Int?): JSONObject {
        val title = tg.chatTitle(chatId)
        if (sendAt != null && sendAt <= System.currentTimeMillis() / 1000) {
            return JSONObject().put("error", "Время ${formatDate(sendAt)} уже прошло")
        }
        val what = if (sendAt == null) "Отправить в «$title» сейчас" else "Запланировать в «$title» на ${formatDate(sendAt)}"
        if (!ui.confirm("$what:\n\n$text")) return cancelled

        val options = TdApi.MessageSendOptions().apply {
            if (sendAt != null) schedulingState = TdApi.MessageSchedulingStateSendAtDate(sendAt, 0)
        }
        val content = TdApi.InputMessageText(formattedText(text), null, false)
        val msg = tg.send(TdApi.SendMessage(chatId, null, null, options, null, content))
        return JSONObject().put("status", if (sendAt == null) "sent" else "scheduled")
            .put("message_id", msg.id).put("chat", title)
    }

    private suspend fun listScheduled(chatId: Long?): JSONArray {
        val ids = chatId?.let { listOf(it) } ?: tg.chats.values.filter { it.hasScheduledMessages }.map { it.id }
        val arr = JSONArray()
        for (id in ids) {
            val title = tg.chatTitle(id)
            for (m in tg.send(TdApi.GetChatScheduledMessages(id)).messages) {
                val state = m.schedulingState
                arr.put(
                    JSONObject().put("chat", title).put("chat_id", id).put("message_id", m.id)
                        .put("send_at", if (state is TdApi.MessageSchedulingStateSendAtDate) formatDate(state.sendDate) else "когда будет в сети")
                        .put("text", contentText(m.content).take(300))
                )
            }
        }
        return arr
    }

    private suspend fun reschedule(chatId: Long, messageId: Long, sendAt: Int): JSONObject {
        if (!ui.confirm("Перенести сообщение в «${tg.chatTitle(chatId)}» на ${formatDate(sendAt)}?")) return cancelled
        tg.send(TdApi.EditMessageSchedulingState(chatId, messageId, TdApi.MessageSchedulingStateSendAtDate(sendAt, 0)))
        return JSONObject().put("status", "rescheduled")
    }

    private suspend fun cancelScheduled(chatId: Long, ids: List<Long>): JSONObject {
        if (ids.isEmpty()) return JSONObject().put("error", "Не указаны message_ids")
        if (!ui.confirm("Удалить ${ids.size} запланированных сообщ. в «${tg.chatTitle(chatId)}»?")) return cancelled
        tg.send(TdApi.DeleteMessages(chatId, ids.toLongArray(), true))
        return JSONObject().put("status", "deleted").put("count", ids.size)
    }

    // ---------- Папки ----------

    private fun folderName(info: TdApi.ChatFolderInfo): String = info.name?.text?.text.orEmpty()

    private fun nameOf(name: String) = TdApi.ChatFolderName(formattedText(name.take(12)), false)

    private suspend fun listFolders(): JSONArray {
        val arr = JSONArray()
        for (f in tg.folders.value) {
            val folder = runCatching { tg.send(TdApi.GetChatFolder(f.id)) }.getOrNull()
            val o = JSONObject().put("folder_id", f.id).put("name", folderName(f))
            if (folder != null) {
                o.put("chosen_chats", folder.includedChatIds.size + folder.pinnedChatIds.size)
                val rules = buildList {
                    if (folder.includeContacts) add("контакты")
                    if (folder.includeNonContacts) add("не контакты")
                    if (folder.includeGroups) add("группы")
                    if (folder.includeChannels) add("каналы")
                    if (folder.includeBots) add("боты")
                }
                if (rules.isNotEmpty()) o.put("auto_include", rules.joinToString(", "))
            }
            arr.put(o)
        }
        return arr
    }

    private suspend fun folderChats(folderId: Int): JSONArray {
        val arr = JSONArray()
        chatIdsOf(TdApi.ChatListFolder(folderId), 500).forEach { arr.put(chatJson(it)) }
        return arr
    }

    private suspend fun titles(ids: List<Long>) = ids.map { tg.chatTitle(it) }.joinToString(", ")

    private suspend fun createFolder(name: String, chatIds: List<Long>): JSONObject {
        if (!ui.confirm("Создать папку «${name.take(12)}» с чатами:\n${titles(chatIds)}")) return cancelled
        val folder = TdApi.ChatFolder().apply {
            this.name = nameOf(name)
            icon = null
            colorId = -1
            pinnedChatIds = LongArray(0)
            includedChatIds = chatIds.distinct().toLongArray()
            excludedChatIds = LongArray(0)
        }
        val info = tg.send(TdApi.CreateChatFolder(folder))
        return JSONObject().put("status", "created").put("folder_id", info.id)
    }

    private suspend fun editFolder(
        folderId: Int,
        add: List<Long> = emptyList(),
        remove: List<Long> = emptyList(),
        newName: String? = null,
    ): JSONObject {
        val folder = tg.send(TdApi.GetChatFolder(folderId))
        val current = folder.name?.text?.text.orEmpty()
        val description = when {
            newName != null -> "Переименовать папку «$current» в «${newName.take(12)}»?"
            add.isNotEmpty() -> "Добавить в папку «$current»:\n${titles(add)}"
            else -> "Убрать из папки «$current»:\n${titles(remove)}"
        }
        if (!ui.confirm(description)) return cancelled

        if (newName != null) folder.name = nameOf(newName)
        if (add.isNotEmpty()) {
            folder.includedChatIds = (folder.includedChatIds.toList() + add).distinct().toLongArray()
            folder.excludedChatIds = folder.excludedChatIds.filter { it !in add }.toLongArray()
        }
        if (remove.isNotEmpty()) {
            folder.includedChatIds = folder.includedChatIds.filter { it !in remove }.toLongArray()
            folder.pinnedChatIds = folder.pinnedChatIds.filter { it !in remove }.toLongArray()
            // Если папка набирает чаты по правилам (все группы и т.п.), явно исключаем их.
            val hasRules = folder.includeContacts || folder.includeNonContacts || folder.includeGroups ||
                folder.includeChannels || folder.includeBots
            if (hasRules) folder.excludedChatIds = (folder.excludedChatIds.toList() + remove).distinct().toLongArray()
        }
        tg.send(TdApi.EditChatFolder(folderId, folder))
        return JSONObject().put("status", "updated")
    }

    private suspend fun deleteFolder(folderId: Int): JSONObject {
        val name = tg.folders.value.firstOrNull { it.id == folderId }?.let { folderName(it) } ?: "#$folderId"
        if (!ui.confirm("Удалить папку «$name»? Чаты останутся.")) return cancelled
        tg.send(TdApi.DeleteChatFolder(folderId, LongArray(0)))
        return JSONObject().put("status", "deleted")
    }
}
