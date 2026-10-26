package com.kartaviy.tgagent.agent

import com.kartaviy.tgagent.tg.Tg
import com.kartaviy.tgagent.tg.contentText
import com.kartaviy.tgagent.tg.contentType
import com.kartaviy.tgagent.tg.hasLink
import org.drinkless.tdlib.TdApi

data class FoundMessage(
    val chatId: Long,
    val chatTitle: String,
    val messageId: Long,
    val date: Int,
    val sender: String,
    val forwardedFrom: String?,
    val type: String,
    val text: String,
)

/** Условия поиска. Все условия объединяются через «И». */
data class MsgFilter(
    val text: String? = null,
    val forwardedOnly: Boolean = false,
    val forwardedFrom: String? = null,
    val sender: String? = null,
    val contentType: String? = null,
    val dateFrom: Int? = null,
    val dateTo: Int? = null,
)

/**
 * Поиск по сложной логике. Если задан текст — используем серверный поиск Telegram,
 * иначе листаем историю чата. Остальные фильтры (пересланные, от кого, тип) применяются на телефоне.
 */
class Searcher(private val tg: Tg) {

    class Budget(var left: Int)

    suspend fun inChat(chatId: Long, f: MsgFilter, limit: Int, budget: Budget, out: MutableList<FoundMessage>) {
        val title = tg.chatTitle(chatId)
        val seen = HashSet<Long>()
        val query = f.text
        val dateFrom = f.dateFrom
        val dateTo = f.dateTo

        if (!query.isNullOrBlank()) {
            var from = 0L
            while (out.size < limit && budget.left > 0) {
                val r = tg.send(TdApi.SearchChatMessages(chatId, null, query, null, from, 0, 100, null))
                if (r.messages.isEmpty()) break
                for (m in r.messages) {
                    if (!seen.add(m.id)) continue
                    budget.left--
                    if (dateTo != null && m.date > dateTo) continue
                    if (dateFrom != null && m.date < dateFrom) return
                    if (matches(m, f, checkText = false)) {
                        out += toFound(m, title)
                        if (out.size >= limit) return
                    }
                }
                if (r.nextFromMessageId == 0L) break
                from = r.nextFromMessageId
            }
            return
        }

        var from = 0L
        if (dateTo != null) {
            from = runCatching { tg.send(TdApi.GetChatMessageByDate(chatId, dateTo)).id }.getOrDefault(0L)
        }
        while (out.size < limit && budget.left > 0) {
            val batch = tg.send(TdApi.GetChatHistory(chatId, from, 0, 100, false)).messages
            var fresh = 0
            for (m in batch) {
                if (!seen.add(m.id)) continue
                fresh++
                budget.left--
                if (dateTo != null && m.date > dateTo) continue
                if (dateFrom != null && m.date < dateFrom) return
                if (matches(m, f, checkText = true)) {
                    out += toFound(m, title)
                    if (out.size >= limit) return
                }
            }
            if (fresh == 0) break
            from = batch.last().id
        }
    }

    /** Поиск текста сразу по всем чатам (серверный). */
    suspend fun global(f: MsgFilter, chatType: String?, limit: Int, budget: Budget, out: MutableList<FoundMessage>) {
        val typeFilter: TdApi.SearchMessagesChatTypeFilter? = when (chatType) {
            "private" -> TdApi.SearchMessagesChatTypeFilterPrivate()
            "group" -> TdApi.SearchMessagesChatTypeFilterGroup()
            "channel" -> TdApi.SearchMessagesChatTypeFilterChannel()
            else -> null
        }
        var offset = ""
        while (out.size < limit && budget.left > 0) {
            val r = tg.send(
                TdApi.SearchMessages(null, f.text.orEmpty(), offset, 100, null, typeFilter, f.dateFrom ?: 0, f.dateTo ?: 0)
            )
            if (r.messages.isEmpty()) break
            for (m in r.messages) {
                budget.left--
                if (matches(m, f, checkText = false)) {
                    out += toFound(m, tg.chatTitle(m.chatId))
                    if (out.size >= limit) return
                }
            }
            if (r.nextOffset.isNullOrEmpty()) break
            offset = r.nextOffset
        }
    }

    private suspend fun matches(m: TdApi.Message, f: MsgFilter, checkText: Boolean): Boolean {
        if (f.forwardedOnly && m.forwardInfo == null) return false
        val fwdFrom = f.forwardedFrom
        if (!fwdFrom.isNullOrBlank()) {
            val origin = tg.forwardOrigin(m) ?: return false
            if (!origin.contains(fwdFrom, ignoreCase = true)) return false
        }
        val sender = f.sender
        if (!sender.isNullOrBlank()) {
            if (sender.equals("me", true) || sender.equals("я", true)) {
                if (!m.isOutgoing) return false
            } else if (!tg.senderName(m.senderId).contains(sender, ignoreCase = true)) {
                return false
            }
        }
        val type = f.contentType
        if (!type.isNullOrBlank()) {
            if (type == "link") {
                if (!hasLink(m.content)) return false
            } else if (contentType(m.content) != type) {
                return false
            }
        }
        val text = f.text
        if (checkText && !text.isNullOrBlank() && !contentText(m.content).contains(text, ignoreCase = true)) return false
        return true
    }

    private suspend fun toFound(m: TdApi.Message, chatTitle: String) = FoundMessage(
        chatId = m.chatId,
        chatTitle = chatTitle,
        messageId = m.id,
        date = m.date,
        sender = tg.senderName(m.senderId),
        forwardedFrom = tg.forwardOrigin(m),
        type = contentType(m.content),
        text = contentText(m.content),
    )
}
