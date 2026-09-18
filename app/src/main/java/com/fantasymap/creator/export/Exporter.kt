package com.fantasymap.creator.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.render.Camera
import com.fantasymap.creator.render.MapRenderer
import com.fantasymap.creator.render.RenderOptions
import kotlin.math.ceil
import kotlin.math.max

/** Сохранение карты в PNG, PDF и текст через системный выбор места. */
class Exporter(private val context: Context) {

    fun exportPng(project: MapProject, uri: Uri, longSide: Int, withLegend: Boolean): Boolean =
        runCatching {
            val ratio = project.worldHeight / project.worldWidth
            val width: Int
            val mapHeight: Int
            if (project.worldWidth >= project.worldHeight) {
                width = longSide
                mapHeight = (longSide * ratio).toInt().coerceAtLeast(64)
            } else {
                mapHeight = longSide
                width = (longSide / ratio).toInt().coerceAtLeast(64)
            }

            val renderer = MapRenderer()
            val uiScale = (width / 1100f).coerceIn(1f, 4f)
            val padding = width * 0.025f
            val legendHeight = if (withLegend) {
                renderer.legendHeight(project, width - padding * 2f, uiScale)
            } else {
                0f
            }

            val bitmap = Bitmap.createBitmap(
                width,
                mapHeight + legendHeight.toInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            val camera = Camera.fit(
                project.worldWidth, project.worldHeight,
                width.toFloat(), mapHeight.toFloat(), padding
            )
            canvas.save()
            canvas.clipRect(0f, 0f, width.toFloat(), mapHeight.toFloat())
            renderer.render(
                canvas, project, camera, width.toFloat(), mapHeight.toFloat(),
                RenderOptions(uiScale = uiScale)
            )
            canvas.restore()

            if (legendHeight > 0f) {
                val top = mapHeight.toFloat()
                renderer.fillPaper(canvas, 0f, top, width.toFloat(), top + legendHeight, project.style.landColor)
                renderer.drawLegend(canvas, project, padding, top, width - padding * 2f, uiScale)
            }

            val ok = context.contentResolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: false
            bitmap.recycle()
            ok
        }.getOrDefault(false)

    /**
     * Карта в PDF. tilesAcross = 1 — один лист целиком,
     * больше — карта режется на листы A4 для печати и склейки.
     */
    fun exportPdf(project: MapProject, uri: Uri, tilesAcross: Int, withLegend: Boolean): Boolean =
        runCatching {
            val renderer = MapRenderer()
            val document = PdfDocument()
            val landscape = project.worldWidth >= project.worldHeight
            val pageWidth = if (landscape) A4_LONG else A4_SHORT
            val pageHeight = if (landscape) A4_SHORT else A4_LONG
            val usableWidth = pageWidth - MARGIN * 2f
            val usableHeight = pageHeight - MARGIN * 2f
            val ink = project.style.inkColor
            var pageNumber = 1

            if (tilesAcross <= 1) {
                val legendHeight = if (withLegend) {
                    renderer.legendHeight(project, usableWidth, 1f)
                } else {
                    0f
                }
                val mapHeight = max(80f, usableHeight - legendHeight - if (withLegend) 12f else 0f)
                val page = document.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
                )
                val canvas = page.canvas
                val camera = Camera.fit(project.worldWidth, project.worldHeight, usableWidth, mapHeight, 0f)
                canvas.save()
                canvas.translate(MARGIN, MARGIN)
                canvas.clipRect(0f, 0f, usableWidth, mapHeight)
                renderer.render(
                    canvas, project, camera, usableWidth, mapHeight,
                    RenderOptions(uiScale = 1f)
                )
                canvas.restore()
                if (legendHeight > 0f) {
                    canvas.save()
                    canvas.translate(MARGIN, MARGIN + mapHeight + 12f)
                    renderer.fillPaper(canvas, 0f, 0f, usableWidth, legendHeight, project.style.landColor)
                    renderer.drawLegend(canvas, project, 10f, 0f, usableWidth - 20f, 1f)
                    canvas.restore()
                }
                renderer.drawPlainText(
                    canvas, project.name, MARGIN, pageHeight - 12f, 9f, ink
                )
                document.finishPage(page)
            } else {
                val columns = tilesAcross.coerceIn(2, 8)
                val tileWorldWidth = project.worldWidth / columns
                val scale = usableWidth / tileWorldWidth
                val tileWorldHeight = usableHeight / scale
                val rows = ceil(project.worldHeight / tileWorldHeight).toInt().coerceAtLeast(1)

                for (row in 0 until rows) {
                    for (column in 0 until columns) {
                        val page = document.startPage(
                            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
                        )
                        val canvas = page.canvas
                        val camera = Camera(
                            scale = scale,
                            tx = -column * tileWorldWidth * scale,
                            ty = -row * tileWorldHeight * scale
                        )
                        canvas.save()
                        canvas.translate(MARGIN, MARGIN)
                        canvas.clipRect(0f, 0f, usableWidth, usableHeight)
                        renderer.render(
                            canvas, project, camera, usableWidth, usableHeight,
                            RenderOptions(uiScale = 1f)
                        )
                        canvas.restore()
                        drawCropMarks(canvas, renderer, pageWidth.toFloat(), pageHeight.toFloat(), ink)
                        renderer.drawPlainText(
                            canvas,
                            "${project.name} · лист ${column + 1}-${row + 1} из ${columns}×$rows",
                            MARGIN, pageHeight - 12f, 9f, ink
                        )
                        document.finishPage(page)
                    }
                }

                if (withLegend) {
                    val page = document.startPage(
                        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
                    )
                    val canvas = page.canvas
                    renderer.fillPaper(canvas, 0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), project.style.landColor)
                    renderer.drawLegend(canvas, project, MARGIN, MARGIN, usableWidth, 1f)
                    renderer.drawPlainText(
                        canvas, "${project.name} · условные обозначения",
                        MARGIN, pageHeight - 12f, 9f, ink
                    )
                    document.finishPage(page)
                }
            }

            val ok = context.contentResolver.openOutputStream(uri)?.use { out ->
                document.writeTo(out)
                true
            } ?: false
            document.close()
            ok
        }.getOrDefault(false)

    /** Уголки для ровной склейки листов. */
    private fun drawCropMarks(
        canvas: Canvas,
        renderer: MapRenderer,
        pageWidth: Float,
        pageHeight: Float,
        ink: Int
    ) {
        val length = 12f
        val marks = arrayOf(
            floatArrayOf(MARGIN, MARGIN, 1f, 1f),
            floatArrayOf(pageWidth - MARGIN, MARGIN, -1f, 1f),
            floatArrayOf(MARGIN, pageHeight - MARGIN, 1f, -1f),
            floatArrayOf(pageWidth - MARGIN, pageHeight - MARGIN, -1f, -1f)
        )
        for (mark in marks) {
            renderer.drawHairline(canvas, mark[0], mark[1], mark[0] + mark[2] * length, mark[1], ink)
            renderer.drawHairline(canvas, mark[0], mark[1], mark[0], mark[1] + mark[3] * length, ink)
        }
    }

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

    private companion object {
        /** A4 в точках PDF (72 на дюйм). */
        const val A4_SHORT = 595
        const val A4_LONG = 842
        const val MARGIN = 28f
    }
}
