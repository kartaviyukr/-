package com.fantasymap.creator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.fantasymap.creator.editor.EditorViewModel
import com.fantasymap.creator.model.Tool
import com.fantasymap.creator.render.MapRenderer
import com.fantasymap.creator.render.RenderOptions
import kotlin.math.hypot

/**
 * Холст карты.
 * Один палец — рисование выбранным инструментом (или перемещение, если выбрана «рука»).
 * Два пальца — всегда перемещение и масштабирование.
 */
@Composable
fun MapCanvas(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier,
    onEditRequest: () -> Unit
) {
    val project = viewModel.project ?: return
    val renderer = remember { MapRenderer() }
    val uiScale = LocalDensity.current.density.coerceIn(1f, 3f)

    Canvas(
        modifier = modifier
            .onSizeChanged { viewModel.onViewSize(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(Unit) {
                val slop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var transforming = viewModel.tool == Tool.PAN
                    var drawing = false
                    var moved = 0f
                    var last = down.position
                    var lastSpread = 0f

                    if (!transforming) {
                        viewModel.startStroke(viewModel.camera.toWorld(down.position.x, down.position.y))
                        drawing = true
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }
                        if (pressed.isEmpty()) break

                        if (pressed.size >= 2) {
                            if (drawing) {
                                viewModel.cancelStroke()
                                drawing = false
                            }
                            transforming = true
                            moved = slop * 4f
                            var cx = 0f
                            var cy = 0f
                            for (change in pressed) {
                                cx += change.position.x
                                cy += change.position.y
                            }
                            cx /= pressed.size
                            cy /= pressed.size
                            var spread = 0f
                            for (change in pressed) {
                                spread += hypot(change.position.x - cx, change.position.y - cy)
                            }
                            spread /= pressed.size
                            if (lastSpread <= 0f) {
                                last = Offset(cx, cy)
                                lastSpread = spread
                            } else {
                                viewModel.pan(cx - last.x, cy - last.y)
                                if (spread > 2f) viewModel.zoom(spread / lastSpread, cx, cy)
                                last = Offset(cx, cy)
                                lastSpread = spread
                            }
                            pressed.forEach { it.consume() }
                        } else {
                            val change = pressed[0]
                            if (lastSpread > 0f) {
                                // палец остался один после щипка — не дёргаем карту
                                lastSpread = 0f
                                last = change.position
                                change.consume()
                                continue
                            }
                            val dx = change.position.x - last.x
                            val dy = change.position.y - last.y
                            moved += hypot(dx, dy)
                            when {
                                transforming -> viewModel.pan(dx, dy)
                                drawing -> viewModel.extendStroke(
                                    viewModel.camera.toWorld(change.position.x, change.position.y)
                                )
                            }
                            last = change.position
                            if (change.positionChange() != Offset.Zero) change.consume()
                        }
                    }

                    if (drawing) viewModel.finishStroke()
                    if (moved <= slop) {
                        val world = viewModel.camera.toWorld(down.position.x, down.position.y)
                        if (viewModel.tap(world)) onEditRequest()
                    }
                }
            }
    ) {
        drawIntoCanvas { canvas ->
            renderer.render(
                canvas = canvas.nativeCanvas,
                project = project,
                cam = viewModel.camera,
                viewWidth = size.width,
                viewHeight = size.height,
                options = RenderOptions(
                    uiScale = uiScale,
                    selection = viewModel.selection,
                    draft = viewModel.draft.toList(),
                    draftClosed = viewModel.toolDrawsArea(),
                    draftColor = viewModel.draftColor(),
                    activeCountryId = viewModel.activeCountryId
                )
            )
        }
    }
}
