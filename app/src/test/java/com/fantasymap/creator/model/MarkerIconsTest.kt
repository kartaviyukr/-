package com.fantasymap.creator.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerIconsTest {

    /** Значок объекта: собственный рисунок или общий знак. */
    private fun iconKey(type: MarkerType): String =
        MarkerIcons.source(type)?.let { "icon:$it" } ?: "glyph:${type.glyph}"

    @Test
    fun everyMarkerHasItsOwnIcon() {
        val byIcon = MarkerType.values().groupBy { iconKey(it) }
        val shared = byIcon.values.filter { it.size > 1 }.map { list -> list.joinToString { it.name } }
        assertTrue("Общие значки: $shared", shared.isEmpty())
    }

    @Test
    fun iconsParseAndStayInBox() {
        for (type in MarkerType.values()) {
            val src = MarkerIcons.source(type) ?: continue
            val ops = MarkerIcons.parse(src)
            assertTrue(type.name, ops.isNotEmpty())
            for (op in ops) {
                val coords = if (op.kind == 'A') op.v.copyOfRange(0, 4) else op.v
                for (c in coords) {
                    assertTrue("${type.name}: $c", c.isFinite() && c > -1.6f && c < 1.6f)
                }
            }
        }
    }

    @Test
    fun parserRejectsBrokenShapes() {
        assertEquals(1, MarkerIcons.parse("E 0 0 1 1").size)
        val bad = runCatching { MarkerIcons.parse("P 0 0 1") }
        assertTrue(bad.isFailure)
    }
}
