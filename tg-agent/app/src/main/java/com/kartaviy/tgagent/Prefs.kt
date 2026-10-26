package com.kartaviy.tgagent

import android.content.Context

/** Настройки хранятся в приватном хранилище приложения на телефоне. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var tgApiId: Int
        get() = sp.getInt("tg_api_id", 0)
        set(v) = sp.edit().putInt("tg_api_id", v).apply()

    var tgApiHash: String
        get() = sp.getString("tg_api_hash", "") ?: ""
        set(v) = sp.edit().putString("tg_api_hash", v).apply()

    var aiApiKey: String
        get() = sp.getString("ai_api_key", "") ?: ""
        set(v) = sp.edit().putString("ai_api_key", v).apply()

    var aiBaseUrl: String
        get() = sp.getString("ai_base_url", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(v) = sp.edit().putString("ai_base_url", v).apply()

    var aiModel: String
        get() = sp.getString("ai_model", DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(v) = sp.edit().putString("ai_model", v).apply()

    /** Если включено, текст сообщений не уходит в ИИ — только метаданные (дата, отправитель, тип). */
    var privateMode: Boolean
        get() = sp.getBoolean("private_mode", true)
        set(v) = sp.edit().putBoolean("private_mode", v).apply()

    val isConfigured: Boolean
        get() = tgApiId != 0 && tgApiHash.isNotBlank() && aiApiKey.isNotBlank()

    companion object {
        const val DEFAULT_BASE_URL = "https://api.deepseek.com"
        const val DEFAULT_MODEL = "deepseek-chat"
    }
}
