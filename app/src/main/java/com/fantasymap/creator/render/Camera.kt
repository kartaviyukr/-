package com.fantasymap.creator.render

import com.fantasymap.creator.model.BBox
import com.fantasymap.creator.model.Vec

/** Преобразование мировых координат карты в экранные и обратно. */
data class Camera(
    val scale: Float = 1f,
    val tx: Float = 0f,
    val ty: Float = 0f
) {
    fun screenX(worldX: Float): Float = worldX * scale + tx
    fun screenY(worldY: Float): Float = worldY * scale + ty
    fun worldX(screenX: Float): Float = (screenX - tx) / scale
    fun worldY(screenY: Float): Float = (screenY - ty) / scale

    fun toWorld(screenX: Float, screenY: Float): Vec = Vec(worldX(screenX), worldY(screenY))

    fun panned(dx: Float, dy: Float): Camera = copy(tx = tx + dx, ty = ty + dy)

    fun zoomed(factor: Float, focusX: Float, focusY: Float): Camera {
        val newScale = (scale * factor).coerceIn(MIN_SCALE, MAX_SCALE)
        val k = newScale / scale
        return Camera(
            scale = newScale,
            tx = focusX - (focusX - tx) * k,
            ty = focusY - (focusY - ty) * k
        )
    }

    /** Видимая область мира для отсечения невидимого при отрисовке. */
    fun visibleWorld(viewWidth: Float, viewHeight: Float): BBox =
        BBox(worldX(0f), worldY(0f), worldX(viewWidth), worldY(viewHeight))

    companion object {
        const val MIN_SCALE = 0.05f
        const val MAX_SCALE = 16f

        /** Камера, вписывающая мир в область просмотра. */
        fun fit(
            worldWidth: Float,
            worldHeight: Float,
            viewWidth: Float,
            viewHeight: Float,
            padding: Float = 24f
        ): Camera {
            if (viewWidth <= 0f || viewHeight <= 0f || worldWidth <= 0f || worldHeight <= 0f) {
                return Camera()
            }
            val scale = minOf(
                (viewWidth - padding * 2) / worldWidth,
                (viewHeight - padding * 2) / worldHeight
            ).coerceIn(MIN_SCALE, MAX_SCALE)
            return Camera(
                scale = scale,
                tx = (viewWidth - worldWidth * scale) / 2f,
                ty = (viewHeight - worldHeight * scale) / 2f
            )
        }
    }
}
