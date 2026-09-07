package com.weatherwear.assistant.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Минимальный HTTP-клиент: сторонних библиотек приложению не нужно. */
internal object Http {

    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 15_000

    @Throws(IOException::class)
    fun getString(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "ChtoNadet/1.0 (Android)")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("Сервер ответил $code")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
