package com.kartaviy.tgagent.agent

import com.kartaviy.tgagent.Prefs
import com.kartaviy.tgagent.tg.Tg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** ИИ-агент: ИИ решает, какие инструменты вызвать, инструменты выполняются на телефоне. */
class Agent(private val tg: Tg, private val prefs: Prefs, ui: AgentUi) {
    private val llm = Llm(prefs)
    private val tools = ToolExecutor(tg, prefs, ui)
    private val history = ArrayList<JSONObject>()

    fun reset() = history.clear()

    private fun systemPrompt(): String {
        val now = ZonedDateTime.now()
        val nowText = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm, EEEE", Locale("ru")))
        return """
Ты — личный ИИ-менеджер Telegram пользователя. Ты работаешь с его аккаунтом через инструменты.
Сейчас: $nowText, часовой пояс ${now.zone.id}.
chat_id чата «Избранное» (сообщения самому себе, напоминания): ${tg.myUserId}.

Правила:
- Если пользователь называет чат или человека по имени, сначала вызови find_chats. Если подходящих чатов несколько и неясно, какой нужен, спроси.
- Время передавай как YYYY-MM-DDTHH:MM по местному времени. «Завтра в 9» и т.п. считай от текущего момента.
- Действия, которые что-то меняют (отправка, отложенные, папки), вызывай сразу: приложение само спросит у пользователя подтверждение. Если результат cancelled_by_user — не повторяй.
- Для сложного поиска используй search_messages с нужными условиями. Найденные сообщения пользователь видит списком на экране, поэтому не перечисляй их все: дай короткий итог (сколько, от кого, за какой период).
- Если инструмент вернул error — объясни пользователю простыми словами.
- Отвечай по-русски, коротко и по делу.
""".trim()
    }

    suspend fun handle(userText: String): String = withContext(Dispatchers.IO) {
        history += JSONObject().put("role", "user").put("content", userText)
        repeat(MAX_STEPS) {
            val messages = JSONArray().put(JSONObject().put("role", "system").put("content", systemPrompt()))
            history.forEach { messages.put(it) }

            val msg = llm.chat(messages, ToolSpecs.all)
            val content = if (msg.isNull("content")) "" else msg.optString("content")
            val calls = msg.optJSONArray("tool_calls")

            val stored = JSONObject().put("role", "assistant").put("content", content)
            if (calls != null && calls.length() > 0) stored.put("tool_calls", calls)
            history += stored

            if (calls == null || calls.length() == 0) {
                return@withContext content.ifBlank { "Готово." }
            }
            for (i in 0 until calls.length()) {
                val call = calls.getJSONObject(i)
                val fn = call.getJSONObject("function")
                val result = try {
                    val args = JSONObject(fn.optString("arguments").ifBlank { "{}" })
                    tools.run(fn.getString("name"), args)
                } catch (e: Exception) {
                    JSONObject().put("error", e.message ?: e.toString()).toString()
                }
                history += JSONObject().put("role", "tool").put("tool_call_id", call.getString("id")).put("content", result)
            }
        }
        "Задача оказалась слишком длинной, я остановился. Попробуйте разбить её на части."
    }

    companion object {
        private const val MAX_STEPS = 15
    }
}
