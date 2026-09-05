package com.simple.notes.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val photos: List<String> = emptyList()
) {
    val preview: String
        get() = body.replace('\n', ' ').trim().take(120)

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("body", body)
        put("updatedAt", updatedAt)
        put("photos", JSONArray(photos))
    }

    companion object {
        fun fromJson(o: JSONObject): Note {
            val photos = mutableListOf<String>()
            val arr = o.optJSONArray("photos")
            if (arr != null) for (i in 0 until arr.length()) photos.add(arr.getString(i))
            return Note(
                id = o.optString("id", UUID.randomUUID().toString()),
                title = o.optString("title", ""),
                body = o.optString("body", ""),
                updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                photos = photos
            )
        }
    }
}
