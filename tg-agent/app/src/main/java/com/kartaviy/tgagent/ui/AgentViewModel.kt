package com.kartaviy.tgagent.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kartaviy.tgagent.App
import com.kartaviy.tgagent.agent.Agent
import com.kartaviy.tgagent.agent.AgentUi
import com.kartaviy.tgagent.agent.FoundMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.TdApi

sealed class Item {
    class User(val text: String) : Item()
    class Bot(val text: String) : Item()
    class Note(val text: String) : Item()
    class Error(val text: String) : Item()
    class Results(val title: String, val messages: List<FoundMessage>) : Item()
    class Confirm(val text: String, val answer: CompletableDeferred<Boolean>) : Item() {
        var decided by mutableStateOf<Boolean?>(null)
    }
}

class AgentViewModel : ViewModel(), AgentUi {
    private val app = App.instance
    private val agent = Agent(app.tg, app.prefs, this)

    val items = mutableStateListOf<Item>()
    var busy by mutableStateOf(false)
        private set

    fun send(text: String) {
        if (text.isBlank() || busy) return
        items += Item.User(text)
        busy = true
        viewModelScope.launch {
            try {
                items += Item.Bot(agent.handle(text))
            } catch (e: Exception) {
                items += Item.Error(e.message ?: e.toString())
            } finally {
                busy = false
            }
        }
    }

    fun newDialog() {
        if (busy) return
        agent.reset()
        items.clear()
    }

    fun answer(item: Item.Confirm, ok: Boolean) {
        if (item.decided != null) return
        item.decided = ok
        item.answer.complete(ok)
    }

    override suspend fun confirm(description: String): Boolean {
        val item = Item.Confirm(description, CompletableDeferred())
        withContext(Dispatchers.Main) { items += item }
        return item.answer.await()
    }

    override suspend fun showResults(title: String, items: List<FoundMessage>) {
        withContext(Dispatchers.Main) { this@AgentViewModel.items += Item.Results(title, items) }
    }

    override suspend fun note(text: String) {
        withContext(Dispatchers.Main) { items += Item.Note(text) }
    }

    /** Открыть найденное сообщение в Telegram. */
    fun open(context: Context, m: FoundMessage) {
        viewModelScope.launch {
            val link = runCatching {
                app.tg.send(TdApi.GetMessageLink(m.chatId, m.messageId, 0, 0, "", false, false)).link
            }.getOrNull()
            val uri = when {
                link != null -> link
                // Личные чаты: у TDLib chat_id совпадает с user_id, серверный id сообщения = id >> 20.
                m.chatId > 0 -> "tg://openmessage?user_id=${m.chatId}&message_id=${m.messageId shr 20}"
                else -> null
            }
            if (uri == null) {
                Toast.makeText(context, "Для этого чата ссылку получить нельзя", Toast.LENGTH_SHORT).show()
                return@launch
            }
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.onFailure {
                Toast.makeText(context, "Не удалось открыть Telegram", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
