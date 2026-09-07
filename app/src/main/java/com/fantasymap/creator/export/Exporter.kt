package com.fantasymap.creator.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.render.Camera
import com.fantasymap.creator.render.MapRenderer
import com.fantasymap.creator.render.RenderOptions

/** Сохранение карты в PNG и текстовые файлы через системный выбор места. */
class Exporter(private val context: Context) {

    fun exportPng(project: MapProject, uri: Uri, longSide: Int): Boolean = runCatching {
        val ratio = project.worldHeight / project.worldWidth
        val width: Int
        val height: Int
        if (project.worldWidth >= project.worldHeight) {
            width = longSide
            height = (longSide * ratio).toInt().coerceAtLeast(64)
        } else {
            height = longSide
            width = (longSide / ratio).toInt().coerceAtLeast(64)
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val padding = width * 0.025f
        val camera = Camera.fit(
            project.worldWidth, project.worldHeight,
            width.toFloat(), height.toFloat(), padding
        )
        val uiScale = (width / 1100f).coerceIn(1f, 4f)
        MapRenderer().render(
            canvas, project, camera, width.toFloat(), height.toFloat(),
            RenderOptions(uiScale = uiScale, deskColor = 0xFFE6D9BA.toInt())
        )

        val ok = context.contentResolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        } ?: false
        bitmap.recycle()
        ok
    }.getOrDefault(false)

    fun writeText(uri: Uri, text: String): Boolean = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(text.toByteArray(Charsets.UTF_8))
            true
        } ?: false
    }.getOrDefault(false)

    fun readText(uri: Uri): String? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        }
    }.getOrNull()
}
