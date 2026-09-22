package com.fantasymap.creator.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Typeface
import com.fantasymap.creator.model.BBox
import com.fantasymap.creator.model.BiomePattern
import com.fantasymap.creator.model.Country
import com.fantasymap.creator.model.Geometry
import com.fantasymap.creator.model.LineFeature
import com.fantasymap.creator.model.LineFeatureType
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.Marker
import com.fantasymap.creator.model.MarkerGroup
import com.fantasymap.creator.model.MarkerType
import com.fantasymap.creator.model.Selection
import com.fantasymap.creator.model.Vec
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Что дополнительно показать поверх карты. */
data class RenderOptions(
    val uiScale: Float = 1f,
    val selection: Selection? = null,
    val draft: List<Vec> = emptyList(),
    val draftClosed: Boolean = true,
    val draftColor: Int = 0xFF9B2C2C.toInt(),
    val activeCountryId: String? = null,
    /** 0 — взять цвет стола из настроек карты. */
    val deskColor: Int = 0
)

/**
 * Рисует карту на обычном android.graphics.Canvas.
 * Один и тот же код используется и для экрана, и для экспорта в PNG.
 * Экземпляр не потокобезопасен.
 */
class MapRenderer {

    private val glyphs = Glyphs()
    private val path = Path()
    private val path2 = Path()
    private val rectF = RectF()

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val thin = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val textHalo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
        style = Paint.Style.STROKE
    }

    private var inkColor = 0xFF3A2E22.toInt()
    private var haloColor = 0xFFFFF8E6.toInt()
    private var labelOverride = 0

    fun render(
        canvas: Canvas,
        project: MapProject,
        cam: Camera,
        viewWidth: Float,
        viewHeight: Float,
        options: RenderOptions = RenderOptions()
    ) {
        val style = project.style
        val u = options.uiScale
        inkColor = style.inkColor
        haloColor = if (luminance(style.inkColor) > 0.55f) 0xFF1A1814.toInt() else 0xFFFFF8E6.toInt()
        labelOverride = style.labelColor
        val visible = cam.visibleWorld(viewWidth, viewHeight).expand(40f / max(cam.scale, 0.01f))

        canvas.drawColor(if (options.deskColor != 0) options.deskColor else style.deskColor)

        // Океан — прямоугольник мира
        val x0 = cam.screenX(0f)
        val y0 = cam.screenY(0f)
        val x1 = cam.screenX(project.worldWidth)
        val y1 = cam.screenY(project.worldHeight)
        fill.color = style.oceanColor
        canvas.drawRect(x0, y0, x1, y1, fill)

        canvas.save()
        canvas.clipRect(x0, y0, x1, y1)

        drawOceanTexture(canvas, project, cam, visible, u)
        if (style.showGrid) drawGrid(canvas, project, cam, u)

        if (style.showLand) drawLandmasses(canvas, project, cam, visible, u)
        if (style.showBiomes) drawBiomes(canvas, project, cam, visible, u, style.showPatterns)
        if (style.showWater) drawWaters(canvas, project, cam, visible, u)
        if (style.showLines) drawLines(canvas, project, cam, visible, u)
        if (style.showDistricts) drawDistricts(canvas, project, cam, visible, u)
        if (style.showRoads) drawRoads(canvas, project, cam, visible, u)
        if (style.showBuildings) drawBuildings(canvas, project, cam, visible, u)
        if (style.showBorders) drawCountries(canvas, project, cam, u, options)
        if (style.showMarkers) drawMarkers(canvas, project, cam, visible, u)
        if (style.showLabels) drawLabels(canvas, project, cam, u)

        drawSelection(canvas, project, cam, u, options.selection)
        drawDraft(canvas, cam, u, options)

        if (style.showCompass) drawCompass(canvas, project, cam, u)
        drawScaleBar(canvas, project, cam, u)

        canvas.restore()

        if (style.showFrame) drawFrame(canvas, x0, y0, x1, y1, u)
    }

    // ---------------------------------------------------------------- океан

    private fun drawOceanTexture(canvas: Canvas, project: MapProject, cam: Camera, visible: BBox, u: Float) {
        var step = 70f
        while (step * cam.scale < 26f) step *= 2f
        if (step * cam.scale > 400f) return
        thin.color = withAlpha(lighten(project.style.oceanColor, 0.35f), 90)
        thin.strokeWidth = 1f * u
        val size = step * cam.scale * 0.3f
        var wy = (Math.floor((visible.minY / step).toDouble()) * step).toFloat()
        var guard = 0
        while (wy < visible.maxY && guard < 4000) {
            var wx = (Math.floor((visible.minX / step).toDouble()) * step).toFloat()
            while (wx < visible.maxX && guard < 4000) {
                guard++
                val n = Geometry.hashNoise((wx / step).toInt(), (wy / step).toInt(), project.style.seed)
                if (n > 0.55f && wx >= 0f && wy >= 0f && wx <= project.worldWidth && wy <= project.worldHeight) {
                    glyphs.drawPattern(
                        canvas, BiomePattern.WAVES,
                        cam.screenX(wx + n * step * 0.4f), cam.screenY(wy + n * step * 0.3f),
                        size, thin
                    )
                }
                wx += step
            }
            wy += step
        }
    }

    private fun drawGrid(canvas: Canvas, project: MapProject, cam: Camera, u: Float) {
        thin.color = withAlpha(inkColor, 40)
        thin.strokeWidth = 1f * u
        var step = 200f
        while (step * cam.scale < 40f) step *= 2f
        var x = 0f
        while (x <= project.worldWidth) {
            canvas.drawLine(cam.screenX(x), cam.screenY(0f), cam.screenX(x), cam.screenY(project.worldHeight), thin)
            x += step
        }
        var y = 0f
        while (y <= project.worldHeight) {
            canvas.drawLine(cam.screenX(0f), cam.screenY(y), cam.screenX(project.worldWidth), cam.screenY(y), thin)
            y += step
        }
    }

    // ---------------------------------------------------------------- суша

    private fun drawLandmasses(canvas: Canvas, project: MapProject, cam: Camera, visible: BBox, u: Float) {
        val style = project.style
        // Ореол вокруг берега — «мелководье»
        for (land in project.landmasses) {
            if (land.points.size < 3) continue
            if (!Geometry.bounds(land.points).intersects(visible)) continue
            buildPath(land.points, cam, true, path)
            stroke.pathEffect = null
            var width = 20f * u
            var alpha = 55
            repeat(3) {
                stroke.color = withAlpha(lighten(style.oceanColor, 0.45f), alpha)
                stroke.strokeWidth = width
                canvas.drawPath(path, stroke)
                width *= 0.55f
                alpha += 35
            }
        }
        // Заливка суши и линия берега
        for (land in project.landmasses) {
            if (land.points.size < 3) continue
            if (!Geometry.bounds(land.points).intersects(visible)) continue
            buildPath(land.points, cam, true, path)
            fill.color = style.landColor
            canvas.drawPath(path, fill)
            stroke.color = style.coastColor
            stroke.strokeWidth = 2.2f * u
            stroke.pathEffect = null
            canvas.drawPath(path, stroke)
        }
    }

    private fun drawWaters(canvas: Canvas, project: MapProject, cam: Camera, visible: BBox, u: Float) {
        for (water in project.waters) {
            if (water.points.size < 3) continue
            if (!Geometry.bounds(water.points).intersects(visible)) continue
            buildPath(water.points, cam, true, path)
            fill.color = water.kind.color
            canvas.drawPath(path, fill)
            stroke.color = darken(water.kind.color, 0.3f)
            stroke.strokeWidth = 1.6f * u
            stroke.pathEffect = null
            canvas.drawPath(path, stroke)
        }
    }

    // ---------------------------------------------------------------- зоны

    private fun drawBiomes(
        canvas: Canvas,
        project: MapProject,
        cam: Camera,
        visible: BBox,
        u: Float,
        patterns: Boolean
    ) {
        for (region in project.biomes) {
            val contours = region.contours().filter { it.size >= 3 }
            if (contours.isEmpty()) continue
            val bounds = Geometry.bounds(contours.flatten())
            if (!bounds.intersects(visible)) continue
            buildContoursPath(contours, cam, path)
            fill.color = withAlpha(region.biome.color, 205)
            canvas.drawPath(path, fill)
            stroke.color = withAlpha(darken(region.biome.color, 0.25f), 150)
            stroke.strokeWidth = 1.2f * u
            stroke.pathEffect = null
            canvas.drawPath(path, stroke)

            if (!patterns || region.biome.pattern == BiomePattern.NONE) continue

            canvas.save()
            canvas.clipPath(path)
            var step = 34f
            while (step * cam.scale < 17f) step *= 2f
            val glyphSize = step * cam.scale * 0.26f
            thin.color = withAlpha(darken(region.biome.color, 0.45f), 190)
            thin.strokeWidth = max(1f, 1.1f * u)
            val fromX = max(bounds.minX, visible.minX)
            val toX = min(bounds.maxX, visible.maxX)
            val fromY = max(bounds.minY, visible.minY)
            val toY = min(bounds.maxY, visible.maxY)
            var drawn = 0
            var wy = (Math.floor((fromY / step).toDouble()) * step).toFloat()
            while (wy < toY && drawn < 3000) {
                var wx = (Math.floor((fromX / step).toDouble()) * step).toFloat()
                while (wx < toX && drawn < 3000) {
                    val n1 = Geometry.hashNoise((wx / step).toInt(), (wy / step).toInt(), project.style.seed)
                    val n2 = Geometry.hashNoise((wx / step).toInt() + 71, (wy / step).toInt() - 13, project.style.seed)
                    if (n1 > 0.22f) {
                        glyphs.drawPattern(
                            canvas, region.biome.pattern,
                            cam.screenX(wx + (n1 - 0.5f) * step * 0.7f),
                            cam.screenY(wy + (n2 - 0.5f) * step * 0.7f),
                            glyphSize, thin
                        )
                        drawn++
                    }
                    wx += step
                }
                wy += step
            }
            canvas.restore()
        }
    }

    // ---------------------------------------------------------------- линии

    private fun drawLines(canvas: Canvas, project: MapProject, cam: Camera, visible: BBox, u: Float) {
        for (feature in project.lines) {
            if (feature.points.size < 2) continue
            if (!Geometry.bounds(feature.points).intersects(visible)) continue
            when (feature.type) {
                LineFeatureType.RIVER, LineFeatureType.BIG_RIVER,
                LineFeatureType.STREAM, LineFeatureType.CANAL -> drawRiver(canvas, feature, cam, u)
                LineFeatureType.MOUNTAIN_RANGE -> drawRidge(canvas, feature, cam, u, project.style.seed, true)
                LineFeatureType.HILL_RANGE -> drawRidge(canvas, feature, cam, u, project.style.seed, false)
                LineFeatureType.FOREST_BELT -> drawForestBelt(canvas, feature, cam, u, project.style.seed)
                LineFeatureType.CLIFF -> drawCliff(canvas, feature, cam, u)
                LineFeatureType.CANYON -> drawCanyon(canvas, feature, cam, u)
                LineFeatureType.REEF_LINE -> drawDots(canvas, feature, cam, u)
                LineFeatureType.GREAT_WALL, LineFeatureType.ICE_WALL -> drawWallLine(canvas, feature, cam, u)
                LineFeatureType.LAVA_FLOW -> drawRiver(canvas, feature, cam, u)
                LineFeatureType.AQUEDUCT -> drawWallLine(canvas, feature, cam, u)
                LineFeatureType.CHASM -> drawCanyon(canvas, feature, cam, u)
                LineFeatureType.ICE_RIDGE -> drawRidge(canvas, feature, cam, u, project.style.seed, true)
                LineFeatureType.SAND_RIDGE -> drawRidge(canvas, feature, cam, u, project.style.seed, false)
                LineFeatureType.ROOT_WALL -> drawForestBelt(canvas, feature, cam, u, project.style.seed)
                LineFeatureType.LEY_LINE,
                LineFeatureType.CORAL_WALL,
                LineFeatureType.MIGRATION_PATH -> drawDots(canvas, feature, cam, u)
                LineFeatureType.CITY_WALL,
                LineFeatureType.INNER_WALL,
                LineFeatureType.PALISADE -> drawWallLine(canvas, feature, cam, u)
                LineFeatureType.MOAT -> drawRiver(canvas, feature, cam, u)
                LineFeatureType.EMBANKMENT -> drawRidge(canvas, feature, cam, u, project.style.seed, false)
                LineFeatureType.HIGH_WALL,
                LineFeatureType.DOUBLE_WALL,
                LineFeatureType.TOWERED_WALL,
                LineFeatureType.RUINED_WALL,
                LineFeatureType.WOODEN_WALL,
                LineFeatureType.CITY_AQUEDUCT -> drawWallLine(canvas, feature, cam, u)
                LineFeatureType.DRY_MOAT,
                LineFeatureType.SPIKE_DITCH -> drawCanyon(canvas, feature, cam, u)
                LineFeatureType.HEDGE_WALL -> drawForestBelt(canvas, feature, cam, u, project.style.seed)
                LineFeatureType.MAGIC_WARD -> drawDots(canvas, feature, cam, u)
            }
            if (feature.name.isNotBlank()) {
                val size = 13f * u * cam.scale.coerceIn(0.7f, 1.8f)
                val color = darken(feature.type.color, 0.35f)
                if (!drawTextAlongPath(canvas, feature.name, feature.points, cam, size, color, true)) {
                    val mid = feature.points[feature.points.size / 2]
                    drawMapText(
                        canvas, feature.name,
                        cam.screenX(mid.x), cam.screenY(mid.y) - 8f * u,
                        size, color, true
                    )
                }
            }
        }
    }

    /** Река с расширением к устью. */
    private fun drawRiver(canvas: Canvas, feature: LineFeature, cam: Camera, u: Float) {
        val pts = feature.points
        val maxWidth = feature.effectiveWidth * u * cam.scale.coerceIn(0.35f, 2.2f)
        stroke.color = feature.type.color
        stroke.pathEffect = null
        for (i in 0 until pts.size - 1) {
            val t = i.toFloat() / max(1, pts.size - 1)
            stroke.strokeWidth = max(1f, maxWidth * (0.35f + 0.65f * t))
            canvas.drawLine(
                cam.screenX(pts[i].x), cam.screenY(pts[i].y),
                cam.screenX(pts[i + 1].x), cam.screenY(pts[i + 1].y), stroke
            )
        }
    }

    /** Горный хребет или гряда холмов — цепочка значков вдоль линии. */
    private fun drawRidge(canvas: Canvas, feature: LineFeature, cam: Camera, u: Float, seed: Int, mountains: Boolean) {
        val base = feature.effectiveWidth
        var stepWorld = base * 1.15f
        while (stepWorld * cam.scale < 9f) stepWorld *= 2f
        val pts = Geometry.resample(feature.points, stepWorld)
        val size = stepWorld * cam.scale * 0.62f
        val sorted = pts.sortedBy { it.y }
        fill.color = if (mountains) 0xFFC7BCA9.toInt() else 0xFFCFC7A7.toInt()
        stroke.color = darken(feature.type.color, 0.2f)
        stroke.strokeWidth = max(1f, 1.3f * u)
        stroke.pathEffect = null
        for ((index, p) in sorted.withIndex()) {
            val n = Geometry.hashNoise(index * 13, (p.x + p.y).toInt(), seed)
            val s = size * (0.75f + n * 0.5f)
            val sx = cam.screenX(p.x)
            val sy = cam.screenY(p.y)
            if (mountains) {
                path.reset()
                path.moveTo(sx - s, sy + s * 0.55f)
                path.lineTo(sx + (n - 0.5f) * s * 0.4f, sy - s * 0.95f)
                path.lineTo(sx + s, sy + s * 0.55f)
                path.close()
                canvas.drawPath(path, fill)
                canvas.drawPath(path, stroke)
                canvas.drawLine(sx + (n - 0.5f) * s * 0.4f, sy - s * 0.95f, sx - s * 0.3f, sy + s * 0.55f, stroke)
            } else {
                rectF.set(sx - s, sy - s * 0.35f, sx + s, sy + s * 0.9f)
                canvas.drawArc(rectF, 185f, 170f, false, stroke)
            }
        }
    }

    private fun drawForestBelt(canvas: Canvas, feature: LineFeature, cam: Camera, u: Float, seed: Int) {
        var stepWorld = feature.effectiveWidth
        while (stepWorld * cam.scale < 8f) stepWorld *= 2f
        val pts = Geometry.resample(feature.points, stepWorld)
        val size = stepWorld * cam.scale * 0.45f
        thin.color = feature.type.color
        thin.strokeWidth = max(1f, 1.2f * u)
        for ((index, p) in pts.withIndex()) {
            val n = Geometry.hashNoise(index * 7, index * 3, seed)
            glyphs.drawPattern(
                canvas, if (n > 0.5f) BiomePattern.TREES else BiomePattern.CONIFERS,
                cam.screenX(p.x), cam.screenY(p.y), size * (0.8f + n * 0.4f), thin
            )
        }
    }

    private fun drawCliff(canvas: Canvas, feature: LineFeature, cam: Camera, u: Float) {
        stroke.color = feature.type.color
        stroke.strokeWidth = max(1.5f, feature.effectiveWidth * 0.4f * u)
        stroke.pathEffect = null
        buildPath(feature.points, cam, false, path)
        canvas.drawPath(path, stroke)
        val pts = Geometry.resample(feature.points, max(8f, 14f / max(cam.scale, 0.05f)))
        val tick = 7f * u
        for (i in 0 until pts.size - 1) {
            val ax = cam.screenX(pts[i].x); val ay = cam.screenY(pts[i].y)
            val bx = cam.screenX(pts[i + 1].x); val by = cam.screenY(pts[i + 1].y)
            val dx = bx - ax; val dy = by - ay
            val len = max(0.001f, kotlin.math.sqrt(dx * dx + dy * dy))
            canvas.drawLine(ax, ay, ax - dy / len * tick, ay + dx / len * tick, stroke)
        }
    }

    private fun drawCanyon(canvas: Canvas, feature: LineFeature, cam: Camera, u: Float) {
        val offset = feature.effectiveWidth * 0.5f
        stroke.color = feature.type.color
        stroke.strokeWidth = max(1.2f, 1.8f * u)
        stroke.pathEffect = null
        for (sign in intArrayOf(-1, 1)) {
            path.reset()
            for (i in feature.points.indices) {
                val p = feature.points[i]
                val prev = feature.points[max(0, i - 1)]
                val dx = p.x - prev.x
                val dy = p.y - prev.y
                val len = max(0.001f, kotlin.math.sqrt(dx * dx + dy * dy))
                val nx = -dy / len * offset * sign
                val ny = dx / len * offset * sign
                val sx = cam.screenX(p.x + nx)
                val sy = cam.screenY(p.y + ny)
                if (i == 0) path.moveTo(sx, sy) else path.lineTo(sx, sy)
            }
            canvas.drawPath(path, stroke)
        }
    }

    private fun drawDots(canvas: Canvas, feature: LineFeature, cam: Camera, u: Float) {
        val pts = Geometry.resample(feature.points, max(6f, 10f / max(cam.scale, 0.05f)))
        fill.color = feature.type.color
        for (p in pts) {
            canvas.drawCircle(cam.screenX(p.x), cam.screenY(p.y), max(1f, 1.8f * u), fill)
        }
    }

    private fun drawWallLine(canvas: Canvas, feature: LineFeature, cam: Camera, u: Float) {
        stroke.color = feature.type.color
        stroke.strokeWidth = max(2f, feature.effectiveWidth * 0.5f * u)
        stroke.pathEffect = null
        buildPath(feature.points, cam, false, path)
        canvas.drawPath(path, stroke)
        val pts = Geometry.resample(feature.points, max(10f, 18f / max(cam.scale, 0.05f)))
        val tick = 4f * u
        stroke.strokeWidth = max(1.5f, 2f * u)
        for (p in pts) {
            val sx = cam.screenX(p.x); val sy = cam.screenY(p.y)
            canvas.drawLine(sx, sy - tick, sx, sy + tick, stroke)
        }
    }

    // ---------------------------------------------------------------- город

    private fun drawDistricts(canvas: Canvas, project: MapProject, cam: Camera, visible: BBox, u: Float) {
        for (district in project.districts) {
            if (district.points.size < 3) continue
            if (!Geometry.bounds(district.points).intersects(visible)) continue
            buildPath(district.points, cam, true, path)
            fill.color = withAlpha(district.type.color, 90)
            canvas.drawPath(path, fill)
            stroke.color = withAlpha(darken(district.type.color, 0.35f), 210)
            stroke.strokeWidth = 1.6f * u
            stroke.pathEffect = DashPathEffect(floatArrayOf(7f * u, 5f * u), 0f)
            canvas.drawPath(path, stroke)
            stroke.pathEffect = null

            val title = district.name.ifBlank { district.type.title }
            val center = Geometry.centroid(district.points)
            drawMapText(
                canvas, title,
                cam.screenX(center.x), cam.screenY(center.y),
                (13f * u * cam.scale.coerceIn(0.6f, 1.6f)),
                darken(district.type.color, 0.5f), true
            )
        }
    }

    private fun drawBuildings(canvas: Canvas, project: MapProject, cam: Camera, visible: BBox, u: Float) {
        for (building in project.buildings) {
            val points = building.points
            if (points.size < 3) continue
            val bounds = Geometry.bounds(points)
            if (!bounds.intersects(visible)) continue

            buildPath(points, cam, true, path)
            fill.color = building.type.color
            canvas.drawPath(path, fill)
            stroke.color = inkColor
            stroke.strokeWidth = max(0.7f, 1.05f * u)
            stroke.pathEffect = null
            canvas.drawPath(path, stroke)

            val screenSize = min(bounds.width, bounds.height) * cam.scale
            // Конёк крыши — вдоль длинной стороны дома.
            if (points.size == 4 && screenSize > 6f) {
                val first = middle(points[0], points[1])
                val second = middle(points[2], points[3])
                val third = middle(points[1], points[2])
                val fourth = middle(points[3], points[0])
                val along = if (first.distanceTo(second) >= third.distanceTo(fourth)) {
                    first to second
                } else {
                    third to fourth
                }
                stroke.color = darken(building.type.color, 0.32f)
                stroke.strokeWidth = max(0.7f, 1.1f * u)
                canvas.drawLine(
                    cam.screenX(along.first.x), cam.screenY(along.first.y),
                    cam.screenX(along.second.x), cam.screenY(along.second.y), stroke
                )
            }

            val mark = building.type.mark
            if (mark != null && screenSize > 13f) {
                val center = Geometry.centroid(points)
                fill.color = withAlpha(0xFFFFF6DF.toInt(), 210)
                stroke.color = inkColor
                stroke.strokeWidth = max(0.7f, 1f * u)
                glyphs.drawGlyph(
                    canvas, mark,
                    cam.screenX(center.x), cam.screenY(center.y),
                    min(screenSize * 0.3f, 8f * u), fill, stroke
                )
            }

            if (building.showLabel && building.name.isNotBlank() && screenSize > 18f) {
                val center = Geometry.centroid(points)
                drawMapText(
                    canvas, building.name,
                    cam.screenX(center.x),
                    cam.screenY(bounds.maxY) + 10f * u,
                    10.5f * u, inkColor, false
                )
            }
        }
    }

    private fun middle(a: Vec, b: Vec) = Vec((a.x + b.x) / 2f, (a.y + b.y) / 2f)

    // ---------------------------------------------------------------- дороги

    private fun drawRoads(canvas: Canvas, project: MapProject, cam: Camera, visible: BBox, u: Float) {
        for (road in project.roads) {
            if (road.points.size < 2) continue
            if (!Geometry.bounds(road.points).intersects(visible)) continue
            buildPath(road.points, cam, false, path)
            val width = max(1f, road.type.width * u * cam.scale.coerceIn(0.4f, 2f))
            if (!road.type.dashed) {
                stroke.color = withAlpha(0xFFF2E6C8.toInt(), 160)
                stroke.strokeWidth = width * 2.1f
                stroke.pathEffect = null
                canvas.drawPath(path, stroke)
            }
            stroke.color = road.type.color
            stroke.strokeWidth = width
            stroke.pathEffect = if (road.type.dashed) {
                DashPathEffect(floatArrayOf(width * 2.6f, width * 2.2f), 0f)
            } else {
                null
            }
            canvas.drawPath(path, stroke)
            stroke.pathEffect = null
            if (road.name.isNotBlank()) {
                val mid = road.points[road.points.size / 2]
                drawMapText(
                    canvas, road.name,
                    cam.screenX(mid.x), cam.screenY(mid.y) - 6f * u,
                    12f * u, darken(road.type.color, 0.3f), true
                )
            }
        }
    }

    // ---------------------------------------------------------------- страны

    private fun drawCountries(canvas: Canvas, project: MapProject, cam: Camera, u: Float, options: RenderOptions) {
        for (country in project.countries) {
            val active = country.id == options.activeCountryId
            for (area in country.areas) {
                if (area.size < 3) continue
                buildPath(area, cam, true, path)
                if (project.style.bordersFilled) {
                    fill.color = withAlpha(country.color, if (active) 70 else 45)
                    canvas.drawPath(path, fill)
                }
                stroke.color = withAlpha(country.color, 80)
                stroke.strokeWidth = 8f * u
                stroke.pathEffect = null
                canvas.drawPath(path, stroke)

                stroke.color = country.color
                stroke.strokeWidth = if (active) 3.4f * u else 2.6f * u
                stroke.pathEffect = DashPathEffect(floatArrayOf(11f * u, 6f * u), 0f)
                canvas.drawPath(path, stroke)
                stroke.pathEffect = null
            }
            val biggest = country.areas.maxByOrNull { Geometry.area(it) }
            if (country.name.isNotBlank() && biggest != null && biggest.size >= 3) {
                val c = Geometry.centroid(biggest)
                drawMapText(
                    canvas, country.name.uppercase(),
                    cam.screenX(c.x), cam.screenY(c.y),
                    (17f * u * cam.scale.coerceIn(0.6f, 1.8f)), darken(country.color, 0.2f), false, bold = true
                )
            }
        }
    }

    // ---------------------------------------------------------------- объекты

    private fun drawMarkers(canvas: Canvas, project: MapProject, cam: Camera, visible: BBox, u: Float) {
        val sorted = project.markers.sortedBy { it.pos.y }
        for (marker in sorted) {
            if (!visible.contains(marker.pos)) continue
            val country = project.countryById(marker.countryId)
            drawMarker(canvas, marker, cam, u, country)
        }
    }

    private fun drawMarker(canvas: Canvas, marker: Marker, cam: Camera, u: Float, country: Country?) {
        val sx = cam.screenX(marker.pos.x)
        val sy = cam.screenY(marker.pos.y)
        val size = 8.5f * u * marker.type.defaultScale * marker.scale

        stroke.color = inkColor
        stroke.strokeWidth = max(1f, 1.4f * u)
        stroke.pathEffect = null
        if (marker.type.group == MarkerGroup.GOODS) {
            // Ресурс рисуется кружком-жетоном со знаком внутри.
            fill.color = darken(markerFill(marker.type), 0.1f)
            canvas.drawCircle(sx, sy, size * 1.2f, fill)
            canvas.drawCircle(sx, sy, size * 1.2f, stroke)
            fill.color = markerFill(marker.type)
            glyphs.drawGlyph(canvas, marker.type.glyph, sx, sy, size * 0.6f, fill, stroke)
        } else {
            fill.color = markerFill(marker.type)
            glyphs.drawGlyph(canvas, marker.type.glyph, sx, sy, size, fill, stroke)
        }

        if (marker.linkedProjectId != null) {
            // Уголок-закладка: с этого объекта есть переход на подробную карту.
            fill.color = 0xFF3D6E8E.toInt()
            path.reset()
            path.moveTo(sx - size * 1.45f, sy - size * 1.45f)
            path.lineTo(sx - size * 0.55f, sy - size * 1.45f)
            path.lineTo(sx - size * 1.0f, sy - size * 0.85f)
            path.close()
            canvas.drawPath(path, fill)
            canvas.drawPath(path, stroke)
        }

        if (country != null && marker.type != MarkerType.CAPITAL) {
            fill.color = country.color
            canvas.drawCircle(sx + size * 1.35f, sy - size * 1.15f, max(2f, 2.6f * u), fill)
        }

        if (marker.showLabel && marker.name.isNotBlank()) {
            drawMapText(
                canvas, marker.name, sx, sy + size * 1.5f + 11f * u,
                (if (marker.type == MarkerType.CAPITAL) 13f else 11.5f) * u,
                inkColor, false, bold = marker.type == MarkerType.CAPITAL
            )
        }
    }

    private fun markerFill(type: MarkerType): Int = when (type) {
        MarkerType.CAPITAL -> 0xFFE9C86B.toInt()
        else -> when (type.group) {
            MarkerGroup.SETTLEMENT -> 0xFFF4E7C9.toInt()
            MarkerGroup.MILITARY -> 0xFFDDCBA8.toInt()
            MarkerGroup.PORT -> 0xFFD3E1E8.toInt()
            MarkerGroup.TRADE -> 0xFFEBDCB4.toInt()
            MarkerGroup.RELIGION -> 0xFFF0E4D6.toInt()
            MarkerGroup.MAGIC -> 0xFFD4C9E6.toInt()
            MarkerGroup.RUIN -> 0xFFCFC7B8.toInt()
            MarkerGroup.NATURE -> 0xFFD8DFC5.toInt()
            MarkerGroup.RESOURCE -> 0xFFE3D2B0.toInt()
            MarkerGroup.DANGER -> 0xFFE5C1B4.toInt()
            MarkerGroup.WONDER -> 0xFFF0DFA8.toInt()
            MarkerGroup.FAUNA -> 0xFFDCD0BC.toInt()
            MarkerGroup.GOODS -> 0xFFEFE0BE.toInt()
            MarkerGroup.ATLAS -> 0xFFCFE0EC.toInt()
            MarkerGroup.CITY_WALLS -> 0xFFDCD3C0.toInt()
            MarkerGroup.CITY_STREET -> 0xFFEDE3CA.toInt()
            MarkerGroup.CITY_SERVICE -> 0xFFE2D9C2.toInt()
            MarkerGroup.CITY_SPECIAL -> 0xFFE7D6C6.toInt()
        }
    }

    // ---------------------------------------------------------------- подписи

    private fun drawLabels(canvas: Canvas, project: MapProject, cam: Camera, u: Float) {
        for (label in project.labels) {
            if (label.text.isBlank()) continue
            val size = (label.style.size * project.style.labelScale * u * cam.scale)
                .coerceIn(9f * u, 96f * u)
            val color = if (labelOverride != 0) labelOverride else label.style.color
            if (label.curved) {
                if (drawTextAlongPath(canvas, label.text, label.path, cam, size, color, label.style.italic)) {
                    continue
                }
            }
            val sx = cam.screenX(label.pos.x)
            val sy = cam.screenY(label.pos.y)
            canvas.save()
            if (abs(label.rotation) > 0.01f) canvas.rotate(label.rotation, sx, sy)
            drawMapText(canvas, label.text, sx, sy, size, color, label.style.italic)
            canvas.restore()
        }
    }

    /** Подпись, изогнутая по линии. Возвращает false, если линия короче текста. */
    private fun drawTextAlongPath(
        canvas: Canvas,
        text: String,
        points: List<Vec>,
        cam: Camera,
        size: Float,
        color: Int,
        italic: Boolean
    ): Boolean {
        if (points.size < 2) return false
        buildPath(points, cam, false, path2)
        val face = Typeface.create(Typeface.SERIF, if (italic) Typeface.ITALIC else Typeface.NORMAL)
        textPaint.typeface = face
        textHalo.typeface = face
        textPaint.textSize = size
        textHalo.textSize = size
        val width = textPaint.measureText(text)
        val length = PathMeasure(path2, false).length
        if (length < width * 1.05f) return false

        textPaint.textAlign = Paint.Align.LEFT
        textHalo.textAlign = Paint.Align.LEFT
        textHalo.color = withAlpha(haloColor, 220)
        textHalo.strokeWidth = max(2f, size * 0.22f)
        textPaint.color = color
        val offset = (length - width) / 2f
        canvas.drawTextOnPath(text, path2, offset, -size * 0.35f, textHalo)
        canvas.drawTextOnPath(text, path2, offset, -size * 0.35f, textPaint)
        textPaint.textAlign = Paint.Align.CENTER
        textHalo.textAlign = Paint.Align.CENTER
        return true
    }

    private fun drawMapText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        size: Float,
        color: Int,
        italic: Boolean,
        bold: Boolean = false
    ) {
        val style = when {
            bold && italic -> Typeface.BOLD_ITALIC
            bold -> Typeface.BOLD
            italic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        val face = Typeface.create(Typeface.SERIF, style)
        textPaint.typeface = face
        textHalo.typeface = face
        textPaint.textSize = size
        textHalo.textSize = size
        textHalo.color = withAlpha(haloColor, 220)
        textHalo.strokeWidth = max(2f, size * 0.22f)
        canvas.drawText(text, x, y, textHalo)
        textPaint.color = color
        canvas.drawText(text, x, y, textPaint)
    }

    // ---------------------------------------------------------------- служебное

    private fun drawSelection(canvas: Canvas, project: MapProject, cam: Camera, u: Float, selection: Selection?) {
        if (selection == null) return
        stroke.color = 0xFFFFB300.toInt()
        stroke.strokeWidth = 3f * u
        stroke.pathEffect = DashPathEffect(floatArrayOf(9f * u, 5f * u), 0f)
        when (selection) {
            is Selection.Land -> project.landmasses.firstOrNull { it.id == selection.id }
                ?.let { outline(canvas, it.points, cam, true) }
            is Selection.Water -> project.waters.firstOrNull { it.id == selection.id }
                ?.let { outline(canvas, it.points, cam, true) }
            is Selection.BuildingSel -> project.buildings.firstOrNull { it.id == selection.id }
                ?.let { outline(canvas, it.points, cam, true) }
            is Selection.DistrictSel -> project.districts.firstOrNull { it.id == selection.id }
                ?.let { outline(canvas, it.points, cam, true) }
            is Selection.Biome -> project.biomes.firstOrNull { it.id == selection.id }
                ?.let { region -> region.contours().forEach { outline(canvas, it, cam, true) } }
            is Selection.Line -> project.lines.firstOrNull { it.id == selection.id }
                ?.let { outline(canvas, it.points, cam, false) }
            is Selection.RoadSel -> project.roads.firstOrNull { it.id == selection.id }
                ?.let { outline(canvas, it.points, cam, false) }
            is Selection.CountryArea -> project.countryById(selection.id)?.areas
                ?.getOrNull(selection.index)?.let { outline(canvas, it, cam, true) }
            is Selection.MarkerSel -> project.markers.firstOrNull { it.id == selection.id }?.let {
                stroke.pathEffect = null
                canvas.drawCircle(cam.screenX(it.pos.x), cam.screenY(it.pos.y), 20f * u, stroke)
            }
            is Selection.LabelSel -> project.labels.firstOrNull { it.id == selection.id }?.let {
                if (it.curved) {
                    outline(canvas, it.path, cam, false)
                } else {
                    stroke.pathEffect = null
                    canvas.drawCircle(cam.screenX(it.pos.x), cam.screenY(it.pos.y), 20f * u, stroke)
                }
            }
        }
        stroke.pathEffect = null
    }

    private fun outline(canvas: Canvas, points: List<Vec>, cam: Camera, close: Boolean) {
        if (points.size < 2) return
        buildPath(points, cam, close, path2)
        canvas.drawPath(path2, stroke)
    }

    private fun drawDraft(canvas: Canvas, cam: Camera, u: Float, options: RenderOptions) {
        val draft = options.draft
        if (draft.size < 2) return
        buildPath(draft, cam, options.draftClosed, path)
        if (options.draftClosed) {
            fill.color = withAlpha(options.draftColor, 55)
            canvas.drawPath(path, fill)
        }
        stroke.color = options.draftColor
        stroke.strokeWidth = 2.4f * u
        stroke.pathEffect = DashPathEffect(floatArrayOf(8f * u, 5f * u), 0f)
        canvas.drawPath(path, stroke)
        stroke.pathEffect = null
    }

    private fun drawFrame(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, u: Float) {
        stroke.pathEffect = null
        stroke.color = inkColor
        stroke.strokeWidth = 3.5f * u
        canvas.drawRect(x0, y0, x1, y1, stroke)
        stroke.strokeWidth = 1.4f * u
        val inset = 7f * u
        canvas.drawRect(x0 + inset, y0 + inset, x1 - inset, y1 - inset, stroke)
    }

    private fun drawCompass(canvas: Canvas, project: MapProject, cam: Camera, u: Float) {
        val r = 30f * u
        val cx = cam.screenX(project.worldWidth) - r * 1.9f
        val cy = cam.screenY(0f) + r * 1.9f
        fill.color = withAlpha(0xFFFFF6DF.toInt(), 200)
        canvas.drawCircle(cx, cy, r, fill)
        stroke.pathEffect = null
        stroke.color = inkColor
        stroke.strokeWidth = 1.8f * u
        canvas.drawCircle(cx, cy, r, stroke)
        canvas.drawCircle(cx, cy, r * 0.72f, stroke)
        for (i in 0 until 8) {
            val a = (i * Math.PI / 4).toFloat()
            val long = i % 2 == 0
            val inner = if (long) r * 0.12f else r * 0.45f
            path.reset()
            path.moveTo(cx + cos(a) * r * 0.92f, cy + sin(a) * r * 0.92f)
            path.lineTo(cx + cos(a + 0.35f) * inner, cy + sin(a + 0.35f) * inner)
            path.lineTo(cx + cos(a - 0.35f) * inner, cy + sin(a - 0.35f) * inner)
            path.close()
            fill.color = if (long) inkColor else withAlpha(inkColor, 120)
            canvas.drawPath(path, fill)
        }
        drawMapText(canvas, "С", cx, cy - r * 1.12f, 14f * u, inkColor, false, bold = true)
    }

    private fun drawScaleBar(canvas: Canvas, project: MapProject, cam: Camera, u: Float) {
        var units = 100f
        var guard = 0
        while (units * cam.scale < 60f && guard++ < 40) units *= 2f
        guard = 0
        while (units * cam.scale > 260f && guard++ < 40) units /= 2f
        val length = units * cam.scale
        val x = cam.screenX(0f) + 24f * u
        val y = cam.screenY(project.worldHeight) - 24f * u
        fill.color = withAlpha(0xFFFFF6DF.toInt(), 190)
        canvas.drawRect(x - 8f * u, y - 20f * u, x + length + 8f * u, y + 8f * u, fill)
        stroke.pathEffect = null
        stroke.color = inkColor
        stroke.strokeWidth = 1.6f * u
        canvas.drawLine(x, y, x + length, y, stroke)
        canvas.drawLine(x, y - 5f * u, x, y + 5f * u, stroke)
        canvas.drawLine(x + length, y - 5f * u, x + length, y + 5f * u, stroke)
        canvas.drawLine(x + length / 2f, y - 3f * u, x + length / 2f, y + 3f * u, stroke)
        textPaint.textAlign = Paint.Align.LEFT
        textHalo.textAlign = Paint.Align.LEFT
        drawMapText(canvas, "${units.toInt()} лиг", x, y - 8f * u, 11f * u, inkColor, true)
        textPaint.textAlign = Paint.Align.CENTER
        textHalo.textAlign = Paint.Align.CENTER
    }

    private fun buildPath(points: List<Vec>, cam: Camera, close: Boolean, out: Path): Path {
        out.reset()
        out.fillType = Path.FillType.WINDING
        if (points.isEmpty()) return out
        out.moveTo(cam.screenX(points[0].x), cam.screenY(points[0].y))
        for (i in 1 until points.size) {
            out.lineTo(cam.screenX(points[i].x), cam.screenY(points[i].y))
        }
        if (close) out.close()
        return out
    }

    /** Путь области из нескольких контуров: вложенный контур даёт дыру. */
    private fun buildContoursPath(contours: List<List<Vec>>, cam: Camera, out: Path): Path {
        out.reset()
        out.fillType = Path.FillType.EVEN_ODD
        for (contour in contours) {
            if (contour.size < 3) continue
            out.moveTo(cam.screenX(contour[0].x), cam.screenY(contour[0].y))
            for (i in 1 until contour.size) {
                out.lineTo(cam.screenX(contour[i].x), cam.screenY(contour[i].y))
            }
            out.close()
        }
        return out
    }

    // ---------------------------------------------------------------- легенда

    private data class LegendItem(
        val kind: Int,
        val color: Int,
        val glyph: com.fantasymap.creator.model.Glyph?,
        val title: String
    )

    private fun legendSections(project: MapProject): List<Pair<String, List<LegendItem>>> {
        val sections = ArrayList<Pair<String, List<LegendItem>>>()

        val zones = project.biomes.map { it.biome }.distinct()
            .map { LegendItem(0, it.color, null, it.title) }
        if (zones.isNotEmpty()) sections.add("Природные зоны" to zones)

        val objects = project.markers.map { it.type }.distinct()
            .map { LegendItem(1, markerFill(it), it.glyph, it.title) }
        if (objects.isNotEmpty()) sections.add("Объекты" to objects)

        val lines = project.lines.map { it.type }.distinct()
            .map { LegendItem(2, it.color, null, it.title) } +
            project.roads.map { it.type }.distinct()
                .map { LegendItem(2, it.color, null, it.title) }
        if (lines.isNotEmpty()) sections.add("Реки, хребты и дороги" to lines)

        val countries = project.countries
            .filter { it.name.isNotBlank() || it.areas.isNotEmpty() }
            .map { LegendItem(3, it.color, null, it.name.ifBlank { "Без названия" }) }
        if (countries.isNotEmpty()) sections.add("Государства" to countries)

        return sections
    }

    private fun legendColumns(width: Float, u: Float): Int =
        (width / (230f * u)).toInt().coerceIn(1, 4)

    /** Высота легенды при данной ширине — чтобы заранее выделить под неё место. */
    fun legendHeight(project: MapProject, width: Float, u: Float): Float {
        val sections = legendSections(project)
        if (sections.isEmpty()) return 0f
        val columns = legendColumns(width, u)
        var height = 42f * u
        for ((_, items) in sections) {
            height += 26f * u
            height += ((items.size + columns - 1) / columns) * 24f * u
            height += 10f * u
        }
        return height + 16f * u
    }

    /** Нарисовать условные обозначения — под картой при выводе в файл. */
    fun drawLegend(
        canvas: Canvas,
        project: MapProject,
        left: Float,
        top: Float,
        width: Float,
        u: Float
    ) {
        val sections = legendSections(project)
        if (sections.isEmpty()) return
        inkColor = project.style.inkColor
        haloColor = if (luminance(project.style.inkColor) > 0.55f) 0xFF1A1814.toInt() else 0xFFFFF8E6.toInt()

        val columns = legendColumns(width, u)
        val columnWidth = width / columns
        var y = top + 30f * u

        textPaint.textAlign = Paint.Align.LEFT
        textHalo.textAlign = Paint.Align.LEFT
        drawPlainText(canvas, "Условные обозначения", left, y, 19f * u, inkColor, bold = true)
        y += 22f * u

        for ((title, items) in sections) {
            drawPlainText(canvas, title, left, y + 14f * u, 14f * u, withAlpha(inkColor, 190), italic = true)
            y += 26f * u
            for ((index, item) in items.withIndex()) {
                val column = index % columns
                val row = index / columns
                val x = left + column * columnWidth
                val itemY = y + row * 24f * u + 12f * u
                drawLegendMark(canvas, item, x + 11f * u, itemY, u)
                drawPlainText(canvas, item.title, x + 26f * u, itemY + 5f * u, 13f * u, inkColor)
            }
            y += ((items.size + columns - 1) / columns) * 24f * u + 10f * u
        }

        textPaint.textAlign = Paint.Align.CENTER
        textHalo.textAlign = Paint.Align.CENTER
    }

    private fun drawLegendMark(canvas: Canvas, item: LegendItem, x: Float, y: Float, u: Float) {
        stroke.pathEffect = null
        stroke.color = inkColor
        stroke.strokeWidth = max(1f, 1.2f * u)
        when (item.kind) {
            0 -> {
                fill.color = item.color
                canvas.drawRect(x - 8f * u, y - 7f * u, x + 8f * u, y + 7f * u, fill)
                canvas.drawRect(x - 8f * u, y - 7f * u, x + 8f * u, y + 7f * u, stroke)
            }
            1 -> {
                fill.color = item.color
                item.glyph?.let { glyphs.drawGlyph(canvas, it, x, y, 7f * u, fill, stroke) }
            }
            2 -> {
                stroke.color = item.color
                stroke.strokeWidth = max(1.5f, 2.4f * u)
                canvas.drawLine(x - 9f * u, y, x + 9f * u, y, stroke)
            }
            else -> {
                fill.color = withAlpha(item.color, 90)
                canvas.drawRect(x - 8f * u, y - 7f * u, x + 8f * u, y + 7f * u, fill)
                stroke.color = item.color
                stroke.strokeWidth = max(1.5f, 2f * u)
                canvas.drawRect(x - 8f * u, y - 7f * u, x + 8f * u, y + 7f * u, stroke)
            }
        }
    }

    /** Текст без обводки — для легенды и подписей листов. */
    fun drawPlainText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        size: Float,
        color: Int,
        bold: Boolean = false,
        italic: Boolean = false
    ) {
        val style = when {
            bold && italic -> Typeface.BOLD_ITALIC
            bold -> Typeface.BOLD
            italic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        textPaint.typeface = Typeface.create(Typeface.SERIF, style)
        textPaint.textSize = size
        textPaint.color = color
        canvas.drawText(text, x, y, textPaint)
    }

    /** Тонкая линия — метки склейки на листах PDF. */
    fun drawHairline(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
        stroke.pathEffect = null
        stroke.color = color
        stroke.strokeWidth = 0.6f
        canvas.drawLine(x1, y1, x2, y2, stroke)
    }

    /** Залить прямоугольник цветом бумаги — фон легенды и полей листа. */
    fun fillPaper(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        fill.color = color
        canvas.drawRect(left, top, right, bottom, fill)
    }

    private fun luminance(color: Int): Float =
        (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color)) / 255f

    companion object {
        fun withAlpha(color: Int, alpha: Int): Int =
            Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

        fun lighten(color: Int, k: Float): Int = Color.argb(
            Color.alpha(color),
            (Color.red(color) + (255 - Color.red(color)) * k).toInt().coerceIn(0, 255),
            (Color.green(color) + (255 - Color.green(color)) * k).toInt().coerceIn(0, 255),
            (Color.blue(color) + (255 - Color.blue(color)) * k).toInt().coerceIn(0, 255)
        )

        fun darken(color: Int, k: Float): Int = Color.argb(
            Color.alpha(color),
            (Color.red(color) * (1 - k)).toInt().coerceIn(0, 255),
            (Color.green(color) * (1 - k)).toInt().coerceIn(0, 255),
            (Color.blue(color) * (1 - k)).toInt().coerceIn(0, 255)
        )
    }
}
