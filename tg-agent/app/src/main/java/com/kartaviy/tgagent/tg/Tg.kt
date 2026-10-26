package com.kartaviy.tgagent.tg

import android.content.Context
import android.os.Build
import com.kartaviy.tgagent.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TgException(val code: Int, message: String) : Exception(message)

/** Обёртка над TDLib: вход в аккаунт, кэш чатов/пользователей/папок и suspend-вызовы API. */
class Tg(private val context: Context, private val prefs: Prefs) {
    @Volatile
    private var client: Client? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val authState = MutableStateFlow<TdApi.AuthorizationState?>(null)
    val lastError = MutableStateFlow<String?>(null)
    val folders = MutableStateFlow<List<TdApi.ChatFolderInfo>>(emptyList())
    val chats = ConcurrentHashMap<Long, TdApi.Chat>()
    val users = ConcurrentHashMap<Long, TdApi.User>()

    @Volatile
    var myUserId: Long = 0
        private set

    val isStarted: Boolean get() = client != null

    fun start() {
        if (client != null) return
        try {
            Client.execute(TdApi.SetLogVerbosityLevel(1))
        } catch (_: Exception) {
        }
        client = Client.create({ onUpdate(it) }, null, null)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : TdApi.Object> send(query: TdApi.Function<T>): T =
        suspendCancellableCoroutine { cont ->
            val c = client
            if (c == null) {
                cont.resumeWithException(TgException(0, "Telegram не запущен"))
                return@suspendCancellableCoroutine
            }
            c.send(query) { result ->
                if (result is TdApi.Error) {
                    cont.resumeWithException(TgException(result.code, result.message))
                } else {
                    cont.resume(result as T)
                }
            }
        }

    private fun onUpdate(obj: TdApi.Object) {
        when (obj) {
            is TdApi.UpdateAuthorizationState -> onAuthState(obj.authorizationState)
            is TdApi.UpdateNewChat -> chats[obj.chat.id] = obj.chat
            is TdApi.UpdateChatTitle -> chats[obj.chatId]?.title = obj.title
            is TdApi.UpdateChatHasScheduledMessages ->
                chats[obj.chatId]?.hasScheduledMessages = obj.hasScheduledMessages
            is TdApi.UpdateUser -> users[obj.user.id] = obj.user
            is TdApi.UpdateChatFolders -> folders.value = obj.chatFolders.toList()
        }
    }

    private fun onAuthState(state: TdApi.AuthorizationState) {
        authState.value = state
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                val params = TdApi.SetTdlibParameters().apply {
                    useTestDc = false
                    databaseDirectory = File(context.filesDir, "tdlib").absolutePath
                    filesDirectory = File(context.cacheDir, "tdlib-files").absolutePath
                    databaseEncryptionKey = ByteArray(0)
                    useFileDatabase = false
                    useChatInfoDatabase = true
                    useMessageDatabase = true
                    useSecretChats = false
                    apiId = prefs.tgApiId
                    apiHash = prefs.tgApiHash
                    systemLanguageCode = Locale.getDefault().language.ifBlank { "ru" }
                    deviceModel = Build.MODEL
                    systemVersion = "Android " + Build.VERSION.RELEASE
                    applicationVersion = "0.1.0"
                }
                client?.send(params) { r -> if (r is TdApi.Error) lastError.value = r.message }
            }
            is TdApi.AuthorizationStateReady -> scope.launch { onReady() }
            is TdApi.AuthorizationStateClosed -> {
                // После выхода из аккаунта TDLib закрывается — создаём клиента заново для нового входа.
                client = null
                chats.clear()
                users.clear()
                folders.value = emptyList()
                start()
            }
            else -> Unit
        }
    }

    private suspend fun onReady() {
        runCatching { myUserId = send(TdApi.GetMe()).id }
        loadAllChats(TdApi.ChatListMain())
        loadAllChats(TdApi.ChatListArchive())
    }

    /** TDLib отдаёт чаты порциями; ошибка 404 означает, что загружены все. */
    private suspend fun loadAllChats(list: TdApi.ChatList) {
        repeat(50) {
            try {
                send(TdApi.LoadChats(list, 200))
            } catch (_: TgException) {
                return
            }
        }
    }

    suspend fun setPhone(phone: String) {
        send(TdApi.SetAuthenticationPhoneNumber(phone, null))
    }

    suspend fun checkCode(code: String) {
        send(TdApi.CheckAuthenticationCode(code))
    }

    suspend fun checkPassword(password: String) {
        send(TdApi.CheckAuthenticationPassword(password))
    }

    suspend fun logOut() {
        send(TdApi.LogOut())
    }

    // ---------- Вспомогательные функции для имён ----------

    suspend fun chat(id: Long): TdApi.Chat =
        chats[id] ?: send(TdApi.GetChat(id)).also { chats[it.id] = it }

    suspend fun chatTitle(id: Long): String =
        if (id == myUserId && myUserId != 0L) "Избранное"
        else runCatching { chat(id).title }.getOrDefault("чат $id")

    suspend fun user(id: Long): TdApi.User? =
        users[id] ?: runCatching { send(TdApi.GetUser(id)) }.getOrNull()?.also { users[it.id] = it }

    fun userName(u: TdApi.User): String {
        val name = listOf(u.firstName, u.lastName).filter { it.isNotBlank() }.joinToString(" ")
        val username = u.usernames?.activeUsernames?.firstOrNull()
        return when {
            name.isBlank() && username != null -> "@$username"
            name.isBlank() -> "Удалённый аккаунт"
            username != null -> "$name (@$username)"
            else -> name
        }
    }

    suspend fun senderName(sender: TdApi.MessageSender?): String = when (sender) {
        is TdApi.MessageSenderUser ->
            if (sender.userId == myUserId) "Я" else user(sender.userId)?.let { userName(it) } ?: "?"
        is TdApi.MessageSenderChat -> chatTitle(sender.chatId)
        else -> ""
    }

    /** Откуда переслано сообщение, или null, если оно не пересланное. */
    suspend fun forwardOrigin(m: TdApi.Message): String? {
        val info = m.forwardInfo ?: return null
        return when (val o = info.origin) {
            is TdApi.MessageOriginUser -> user(o.senderUserId)?.let { userName(it) } ?: "пользователь"
            is TdApi.MessageOriginHiddenUser -> o.senderName
            is TdApi.MessageOriginChat -> chatTitle(o.senderChatId)
            is TdApi.MessageOriginChannel -> chatTitle(o.chatId)
            else -> "неизвестно"
        }
    }

    fun chatType(chat: TdApi.Chat): String = when (val t = chat.type) {
        is TdApi.ChatTypePrivate -> "private"
        is TdApi.ChatTypeBasicGroup -> "group"
        is TdApi.ChatTypeSupergroup -> if (t.isChannel) "channel" else "group"
        is TdApi.ChatTypeSecret -> "secret"
        else -> "other"
    }
}
