package com.kartaviy.tgagent.agent

import com.kartaviy.tgagent.Prefs
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Клиент OpenAI-совместимого API (по умолчанию DeepSeek). Блокирующий — вызывать с Dispatchers.IO. */
class Llm(private val prefs: Prefs) {
    fun chat(messages: JSONArray, tools: JSONArray): JSONObject {
        val url = URL(prefs.aiBaseUrl.trimEnd('/') + "/chat/completions")
        val body = JSONObject()
            .put("model", prefs.aiModel)
            .put("messages", messages)
            .put("tools", tools)
            .put("tool_choice", "auto")
            .put("temperature", 0.2)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 180_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${prefs.aiApiKey}")
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val hint = when (code) {
                    401 -> " — неверный API-ключ"
                    402 -> " — на балансе API закончились деньги"
                    429 -> " — слишком много запросов, подождите"
                    else -> ""
                }
                throw IOException("Ошибка ИИ API $code$hint: ${text.take(300)}")
            }
            return JSONObject(text).getJSONArray("choices").getJSONObject(0).getJSONObject("message")
        } finally {
            conn.disconnect()
        }
    }
}
