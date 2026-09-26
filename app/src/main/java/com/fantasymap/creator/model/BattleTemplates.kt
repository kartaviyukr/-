package com.fantasymap.creator.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.round
import kotlin.math.sin

/** Готовые планировки боевых локаций: основные места приключений D&D. */
enum class RoomTemplate(val title: String, val icon: String) {
    TAVERN("Таверна", "🍺"),
    CHURCH("Храм", "⛪"),
    PALACE("Тронный зал", "👑"),
    CASTLE("Замок", "🏰"),
    TOWER("Башня", "🗼"),
    FLOOR("Этаж с комнатами", "🚪"),
    HOUSE("Дом", "🏠"),
    SHOP("Лавка", "🛒"),
    SMITHY("Кузница", "⚒"),
    CRYPT("Склеп", "⚰"),
    PRISON("Темница", "⛓"),
    DUNGEON("Подземелье", "🗝"),
    CAVE("Пещера", "🕳"),
    CAMP("Лагерь", "⛺"),
    BARRACKS("Казарма", "🛡"),
    LIBRARY("Библиотека", "📚"),
    ARENA("Арена", "⚔"),
    SHIP("Палуба корабля", "⛵"),
    MANOR("Особняк", "🏛"),
    GUILD_HALL("Дом гильдии", "📜"),
    WIZARD_TOWER("Башня мага", "🔮"),
    ALCHEMIST("Лаборатория алхимика", "⚗"),
    STABLE("Конюшня", "🐎"),
    WAREHOUSE("Склад", "📦"),
    SEWER("Канализация", "🐀"),
    MINE("Шахта", "⛏"),
    GRAVEYARD("Кладбище", "🪦"),
    BRIDGE("Мост через реку", "🌉"),
    CITY_GATE("Городские ворота", "🚧"),
    INN_ROOMS("Постоялый двор, этаж", "🛏"),
    BATHHOUSE("Бани", "🛁"),
    THIEVES_DEN("Логово воров", "🗡"),
    DRAGON_LAIR("Логово дракона", "🐉"),
    FOREST_CLEARING("Лесная поляна", "🌲"),
    MARKET_SQUARE("Рыночная площадь", "🏪"),
    TEMPLE_RUINS("Руины храма", "🏚")
}

/**
 * Шаблон в клетках сетки: пол, стены (с проёмами под двери) и обстановка.
 * Шаблон ставится касанием в натуральную величину или растягивается рамкой.
 */
object BattleTemplates {

    class Plan(
        val cols: Int,
        val rows: Int,
        val floors: List<Pair<BiomeType, List<Vec>>>,
        val walls: List<Pair<LineFeatureType, List<Vec>>>,
        val props: List<Pair<MarkerType, Vec>>
    )

    data class Placed(val biomes: List<BiomeRegion>, val lines: List<LineFeature>, val markers: List<Marker>)

    /** Проём в стене: середина, ширина в клетках и что в нём стоит. */
    class Gap(val x: Float, val y: Float, val door: MarkerType? = MarkerType.B_DOOR, val width: Float = 1f)

    fun gap(x: Float, y: Float, door: MarkerType? = MarkerType.B_DOOR, width: Float = 1f) = Gap(x, y, door, width)

    private class Builder(val cols: Int, val rows: Int) {
        val floors = ArrayList<Pair<BiomeType, List<Vec>>>()
        val walls = ArrayList<Pair<LineFeatureType, List<Vec>>>()
        val props = ArrayList<Pair<MarkerType, Vec>>()

        fun floor(type: BiomeType, x0: Float, y0: Float, x1: Float, y1: Float) {
            floors.add(type to rectangle(x0, y0, x1, y1))
        }

        fun floorShape(type: BiomeType, vararg xy: Float) {
            floors.add(type to points(xy))
        }

        fun floorCircle(type: BiomeType, cx: Float, cy: Float, r: Float, sides: Int = 24) {
            floors.add(type to circle(cx, cy, r, sides))
        }

        /** Комната: стены по периметру с проёмами; в проёмах — двери. */
        fun room(type: LineFeatureType, x0: Float, y0: Float, x1: Float, y1: Float, vararg gaps: Gap) {
            outline(type, rectangle(x0, y0, x1, y1), gaps.toList())
        }

        /** Круглая или многоугольная стена с проёмами. */
        fun ring(type: LineFeatureType, cx: Float, cy: Float, r: Float, sides: Int, vararg gaps: Gap) {
            outline(type, circle(cx, cy, r, sides), gaps.toList())
        }

        fun shape(type: LineFeatureType, xy: FloatArray, vararg gaps: Gap) {
            outline(type, points(xy), gaps.toList())
        }

        fun wall(type: LineFeatureType, vararg xy: Float) {
            walls.add(type to points(xy))
        }

        fun put(type: MarkerType, x: Float, y: Float) {
            props.add(type to Vec(x, y))
        }

        fun row(type: MarkerType, y: Float, vararg xs: Float) {
            for (x in xs) put(type, x, y)
        }

        private fun outline(type: LineFeatureType, corners: List<Vec>, gaps: List<Gap>) {
            for (piece in cut(corners, gaps)) walls.add(type to piece)
            for (g in gaps) g.door?.let { put(it, g.x, g.y) }
        }
    }

    private fun points(xy: FloatArray): List<Vec> = xy.toList().chunked(2).map { Vec(it[0], it[1]) }

    private fun rectangle(x0: Float, y0: Float, x1: Float, y1: Float) =
        listOf(Vec(x0, y0), Vec(x1, y0), Vec(x1, y1), Vec(x0, y1))

    private fun circle(cx: Float, cy: Float, r: Float, sides: Int) = List(sides) { i ->
        val a = -PI.toFloat() / 2f + i * 2f * PI.toFloat() / sides
        Vec(cx + cos(a) * r, cy + sin(a) * r)
    }

    /**
     * Разрезать замкнутый контур проёмами. Без проёмов — замкнутая линия.
     */
    fun cut(corners: List<Vec>, gaps: List<Gap>): List<List<Vec>> {
        val n = corners.size
        val lengths = List(n) { corners[it].distanceTo(corners[(it + 1) % n]) }
        val starts = FloatArray(n)
        for (i in 1 until n) starts[i] = starts[i - 1] + lengths[i - 1]
        val total = lengths.sum()
        if (gaps.isEmpty() || total <= 0f) return listOf(corners + corners.first())

        fun positionOf(p: Vec): Float {
            var best = Float.MAX_VALUE
            var s = 0f
            for (i in 0 until n) {
                val a = corners[i]
                val b = corners[(i + 1) % n]
                val q = Geometry.closestOnSegment(p, a, b)
                val d = p.distanceTo(q)
                if (d < best) {
                    best = d
                    s = starts[i] + a.distanceTo(q)
                }
            }
            return s
        }

        fun pointAt(s: Float): Vec {
            var t = ((s % total) + total) % total
            for (i in 0 until n) {
                if (t <= lengths[i] || i == n - 1) {
                    val a = corners[i]
                    val b = corners[(i + 1) % n]
                    val k = if (lengths[i] > 0f) (t / lengths[i]).coerceIn(0f, 1f) else 0f
                    return Vec(a.x + (b.x - a.x) * k, a.y + (b.y - a.y) * k)
                }
                t -= lengths[i]
            }
            return corners.last()
        }

        val holes = gaps.map { g ->
            val s = positionOf(Vec(g.x, g.y))
            (s - g.width / 2f) to (s + g.width / 2f)
        }.sortedBy { it.first }
        val pieces = ArrayList<List<Vec>>()
        for (i in holes.indices) {
            val from = holes[i].second
            var to = holes[(i + 1) % holes.size].first
            if (to <= from) to += total
            if (to - from < 0.05f) continue
            val piece = ArrayList<Vec>()
            piece.add(pointAt(from))
            // Углы между концами куска.
            for (lap in 0..1) {
                for (k in 0 until n) {
                    val s = starts[k] + lap * total
                    if (s > from + 0.001f && s < to - 0.001f) piece.add(corners[k])
                }
            }
            piece.add(pointAt(to))
            pieces.add(piece)
        }
        return pieces
    }

    fun plan(template: RoomTemplate): Plan {
        val b = when (template) {
            RoomTemplate.TAVERN -> build(14, 10) {
                floor(BiomeType.WOOD_FLOOR, 0f, 0f, 14f, 10f)
                floor(BiomeType.STONE_FLOOR, 10f, 0f, 14f, 5f)
                room(LineFeatureType.TIMBER_WALL, 0f, 0f, 14f, 10f,
                    gap(7f, 10f, MarkerType.B_DOUBLE_DOOR, 2f), gap(14f, 8f), gap(3f, 0f, MarkerType.B_WINDOW), gap(0f, 7.5f, MarkerType.B_WINDOW))
                wall(LineFeatureType.TIMBER_WALL, 10f, 0f, 10f, 2.5f)
                wall(LineFeatureType.TIMBER_WALL, 10f, 3.5f, 10f, 5f, 11.5f, 5f)
                wall(LineFeatureType.TIMBER_WALL, 12.5f, 5f, 14f, 5f)
                put(MarkerType.B_DOOR, 10f, 3f)
                put(MarkerType.B_DOOR, 12f, 5f)
                put(MarkerType.B_BAR, 6f, 1.3f)
                row(MarkerType.B_BARREL, 0.8f, 1f, 2f)
                put(MarkerType.B_FIREPLACE, 0.8f, 4.5f)
                row(MarkerType.B_ROUND_TABLE, 4.5f, 3f, 7f)
                row(MarkerType.B_ROUND_TABLE, 7.5f, 3f, 7f)
                row(MarkerType.B_CHAIR, 4.5f, 2f, 4f, 6f, 8f)
                row(MarkerType.B_CHAIR, 7.5f, 2f, 4f, 6f, 8f)
                put(MarkerType.B_LONG_TABLE, 11.5f, 7.5f)
                put(MarkerType.B_STAIRS_UP, 13f, 6f)
                put(MarkerType.B_CAULDRON, 12f, 1.5f)
                put(MarkerType.B_SHELF, 13.3f, 3.2f)
                put(MarkerType.B_CRATE, 11f, 4.2f)
                put(MarkerType.B_CHANDELIER, 5f, 6f)
            }
            RoomTemplate.CHURCH -> build(12, 18) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 12f, 18f)
                floor(BiomeType.MARBLE_FLOOR, 2f, 0f, 10f, 4f)
                floor(BiomeType.HOLY_GROUND, 4f, 0.5f, 8f, 3.5f)
                floor(BiomeType.CARPET, 5f, 4f, 7f, 18f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 12f, 18f,
                    gap(6f, 18f, MarkerType.B_DOUBLE_DOOR, 2f), gap(0f, 12f), gap(12f, 6f, MarkerType.B_WINDOW), gap(0f, 6f, MarkerType.B_WINDOW))
                put(MarkerType.B_ALTAR, 6f, 2f)
                row(MarkerType.B_CANDLES, 2f, 4.3f, 7.7f)
                row(MarkerType.B_STATUE, 1.5f, 1.5f, 10.5f)
                for (y in floatArrayOf(6f, 9f, 12f, 15f)) row(MarkerType.B_PILLAR, y, 3f, 9f)
                for (y in floatArrayOf(7.5f, 9f, 10.5f, 12f, 13.5f, 15f)) row(MarkerType.B_BENCH, y, 4f, 8f)
                put(MarkerType.B_CHANDELIER, 6f, 10f)
                put(MarkerType.B_BELL, 10.5f, 16.5f)
            }
            RoomTemplate.PALACE -> build(20, 16) {
                floor(BiomeType.MARBLE_FLOOR, 0f, 0f, 20f, 16f)
                floor(BiomeType.CARPET, 8.5f, 2f, 11.5f, 16f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 20f, 16f,
                    gap(10f, 16f, MarkerType.B_DOUBLE_DOOR, 2f), gap(0f, 9f), gap(20f, 9f))
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 4f, 5f, gap(4f, 3f))
                room(LineFeatureType.BRICK_WALL, 16f, 0f, 20f, 5f, gap(16f, 3f))
                put(MarkerType.B_THRONE, 10f, 2f)
                row(MarkerType.B_BANNER, 1f, 7f, 13f)
                for (y in floatArrayOf(4f, 7f, 10f, 13f)) row(MarkerType.B_PILLAR, y, 6f, 14f)
                row(MarkerType.B_STATUE, 14.5f, 2f, 18f)
                put(MarkerType.B_CHANDELIER, 10f, 8f)
                put(MarkerType.B_CHEST, 2f, 1.5f)
                put(MarkerType.B_BED, 18f, 1.5f)
                put(MarkerType.B_WARDROBE, 18.5f, 4f)
                put(MarkerType.B_TABLE, 2f, 3.5f)
            }
            RoomTemplate.CASTLE -> build(24, 24) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 24f, 24f)
                floor(BiomeType.COBBLE_FLOOR, 3f, 3f, 21f, 21f)
                floor(BiomeType.CASTLE_FLOOR, 7f, 5f, 17f, 12f)
                for ((cx, cy) in listOf(3f to 3f, 21f to 3f, 3f to 21f, 21f to 21f)) floorCircle(BiomeType.CASTLE_FLOOR, cx, cy, 2.2f, 12)
                room(LineFeatureType.DUNGEON_WALL, 3f, 3f, 21f, 21f, gap(12f, 21f, MarkerType.B_BARRED_GATE, 2f))
                ring(LineFeatureType.DUNGEON_WALL, 3f, 3f, 2.2f, 12, gap(4.6f, 4.6f))
                ring(LineFeatureType.DUNGEON_WALL, 21f, 3f, 2.2f, 12, gap(19.4f, 4.6f))
                ring(LineFeatureType.DUNGEON_WALL, 3f, 21f, 2.2f, 12, gap(4.6f, 19.4f))
                ring(LineFeatureType.DUNGEON_WALL, 21f, 21f, 2.2f, 12, gap(19.4f, 19.4f))
                room(LineFeatureType.DUNGEON_WALL, 7f, 5f, 17f, 12f, gap(12f, 12f, MarkerType.B_DOUBLE_DOOR, 2f))
                put(MarkerType.B_THRONE, 12f, 6.3f)
                put(MarkerType.B_LONG_TABLE, 12f, 9f)
                put(MarkerType.B_FIREPLACE, 7.8f, 8.5f)
                put(MarkerType.B_WELL, 7f, 16f)
                put(MarkerType.B_CART, 17f, 17f)
                put(MarkerType.B_WEAPON_RACK, 5f, 13f)
                put(MarkerType.B_DUMMY, 15f, 18f)
                put(MarkerType.B_HAY, 19f, 14f)
                row(MarkerType.B_SPIRAL_STAIRS, 3f, 3f, 21f)
                row(MarkerType.B_SPIRAL_STAIRS, 21f, 3f, 21f)
            }
            RoomTemplate.TOWER -> build(10, 10) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 10f, 10f)
                floorCircle(BiomeType.STONE_FLOOR, 5f, 5f, 4.5f)
                ring(LineFeatureType.DUNGEON_WALL, 5f, 5f, 4.5f, 16,
                    gap(5f, 9.5f), gap(0.5f, 5f, MarkerType.B_ARROW_SLIT, 0.8f), gap(9.5f, 5f, MarkerType.B_ARROW_SLIT, 0.8f))
                put(MarkerType.B_SPIRAL_STAIRS, 5f, 5f)
                put(MarkerType.B_TABLE, 3f, 3.5f)
                put(MarkerType.B_BOOKSHELF, 6.8f, 2.2f)
                put(MarkerType.B_CHEST, 7.2f, 6.8f)
                put(MarkerType.B_TORCH, 2.3f, 7f)
            }
            RoomTemplate.FLOOR -> build(16, 10) {
                floor(BiomeType.STONE_FLOOR, 0f, 4f, 16f, 6f)
                floor(BiomeType.WOOD_FLOOR, 0f, 0f, 16f, 4f)
                floor(BiomeType.WOOD_FLOOR, 0f, 6f, 16f, 10f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 16f, 10f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 5f, 4f, gap(2.5f, 4f))
                room(LineFeatureType.BRICK_WALL, 5f, 0f, 11f, 4f, gap(8f, 4f))
                room(LineFeatureType.BRICK_WALL, 11f, 0f, 16f, 4f, gap(13.5f, 4f))
                room(LineFeatureType.BRICK_WALL, 0f, 6f, 5f, 10f, gap(2.5f, 6f))
                room(LineFeatureType.BRICK_WALL, 5f, 6f, 11f, 10f, gap(8f, 6f))
                room(LineFeatureType.BRICK_WALL, 11f, 6f, 16f, 10f, gap(13.5f, 6f))
                put(MarkerType.B_STAIRS_UP, 1f, 5f)
                put(MarkerType.B_STAIRS_DOWN, 15f, 5f)
                row(MarkerType.B_TORCH, 4.3f, 5f, 11f)
                put(MarkerType.B_BED, 1.5f, 1.3f); put(MarkerType.B_CHEST, 3.8f, 1f)
                put(MarkerType.B_TABLE, 8f, 1.8f); row(MarkerType.B_CHAIR, 1.8f, 6.8f, 9.2f)
                put(MarkerType.B_BOOKSHELF, 13.5f, 0.8f); put(MarkerType.B_ALCHEMY, 13.5f, 2.5f)
                put(MarkerType.B_BUNKS, 1.5f, 8.5f); put(MarkerType.B_BARREL, 4f, 9f)
                put(MarkerType.B_WARDROBE, 6f, 9f); put(MarkerType.B_BATH, 9f, 8.6f)
                put(MarkerType.B_CRATE_STACK, 13.5f, 8.5f)
            }
            RoomTemplate.HOUSE -> build(8, 7) {
                floor(BiomeType.WOOD_FLOOR, 0f, 0f, 8f, 7f)
                room(LineFeatureType.TIMBER_WALL, 0f, 0f, 8f, 7f, gap(3f, 7f), gap(0f, 3f, MarkerType.B_WINDOW), gap(8f, 5f, MarkerType.B_WINDOW))
                wall(LineFeatureType.TIMBER_WALL, 5f, 0f, 5f, 2.5f)
                wall(LineFeatureType.TIMBER_WALL, 5f, 3.5f, 5f, 7f)
                put(MarkerType.B_DOOR, 5f, 3f)
                put(MarkerType.B_BED, 6.5f, 1.3f)
                put(MarkerType.B_CHEST, 7.2f, 5.8f)
                put(MarkerType.B_TABLE, 2.5f, 3f)
                row(MarkerType.B_CHAIR, 4.1f, 2f, 3f)
                put(MarkerType.B_FIREPLACE, 1f, 0.8f)
                put(MarkerType.B_BARREL, 0.8f, 6.2f)
            }
            RoomTemplate.SHOP -> build(10, 8) {
                floor(BiomeType.WOOD_FLOOR, 0f, 0f, 10f, 8f)
                floor(BiomeType.CARPET, 3f, 4.5f, 7f, 7f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 10f, 8f, gap(5f, 8f), gap(2f, 8f, MarkerType.B_WINDOW), gap(8f, 8f, MarkerType.B_WINDOW))
                put(MarkerType.B_BAR, 5f, 3f)
                row(MarkerType.B_SHELF, 0.8f, 2f, 5f, 8f)
                row(MarkerType.B_CRATE, 6.5f, 1f, 9f)
                put(MarkerType.B_CHEST, 8.8f, 2.3f)
                put(MarkerType.B_SACKS, 1.2f, 3f)
                put(MarkerType.B_POTION, 4f, 2.8f)
            }
            RoomTemplate.SMITHY -> build(10, 10) {
                floor(BiomeType.DIRT_GROUND, 0f, 0f, 10f, 10f)
                floor(BiomeType.CLAY_FLOOR, 0f, 0f, 10f, 8f)
                wall(LineFeatureType.BRICK_WALL, 0f, 8f, 0f, 0f, 10f, 0f, 10f, 8f)
                put(MarkerType.B_FORGE, 3f, 1.8f)
                put(MarkerType.B_ANVIL, 5f, 3.5f)
                put(MarkerType.B_WEAPON_RACK, 8f, 1f)
                put(MarkerType.B_BARREL, 1f, 6f)
                put(MarkerType.B_CRATE_STACK, 9f, 6f)
                put(MarkerType.B_ARMOR, 8.5f, 3.5f)
                put(MarkerType.B_CART, 5f, 9f)
            }
            RoomTemplate.CRYPT -> build(12, 14) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 12f, 14f)
                floor(BiomeType.CURSED_GROUND, 4.5f, 4.5f, 7.5f, 7.5f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 12f, 14f, gap(6f, 14f, MarkerType.B_IRON_DOOR))
                put(MarkerType.B_SARCOPHAGUS, 6f, 6f)
                for (y in floatArrayOf(3f, 6f, 9f)) row(MarkerType.B_COFFIN, y, 1.2f, 10.8f)
                for (y in floatArrayOf(4f, 10f)) row(MarkerType.B_PILLAR, y, 4f, 8f)
                put(MarkerType.B_CANDLES, 6f, 3.5f)
                put(MarkerType.B_BONES, 3f, 12f)
                put(MarkerType.B_STAIRS_UP, 6f, 12.8f)
                put(MarkerType.B_TREASURE, 10.5f, 12f)
            }
            RoomTemplate.PRISON -> build(16, 10) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 16f, 10f)
                floor(BiomeType.STRAW_FLOOR, 0f, 3f, 16f, 10f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 16f, 10f, gap(0f, 1.5f, MarkerType.B_IRON_DOOR))
                for (x in floatArrayOf(4f, 8f, 12f)) wall(LineFeatureType.DUNGEON_WALL, x, 3f, x, 10f)
                for (i in 0 until 4) {
                    val x0 = i * 4f
                    wall(LineFeatureType.IRON_BARS, x0, 3f, x0 + 1.5f, 3f)
                    wall(LineFeatureType.IRON_BARS, x0 + 2.5f, 3f, x0 + 4f, 3f)
                    put(MarkerType.B_BARRED_GATE, x0 + 2f, 3f)
                    put(MarkerType.B_BUNKS, x0 + 1f, 8.5f)
                }
                put(MarkerType.B_BONES, 6f, 6f)
                put(MarkerType.B_STOCKS, 14f, 6.5f)
                put(MarkerType.B_TABLE, 12.5f, 1.4f)
                put(MarkerType.B_KEY, 13.8f, 1f)
                put(MarkerType.B_WEAPON_RACK, 9.5f, 0.8f)
                row(MarkerType.B_TORCH, 0.6f, 4f, 7f)
            }
            RoomTemplate.DUNGEON -> build(20, 16) {
                floor(BiomeType.CAVE_FLOOR, 0f, 0f, 20f, 16f)
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 8f, 7f)
                floor(BiomeType.STONE_FLOOR, 12f, 0f, 20f, 7f)
                floor(BiomeType.CASTLE_FLOOR, 6f, 10f, 14f, 16f)
                floor(BiomeType.STONE_FLOOR, 8f, 3f, 12f, 4f)
                floor(BiomeType.STONE_FLOOR, 3.5f, 7f, 4.5f, 13.5f)
                floor(BiomeType.STONE_FLOOR, 4.5f, 12.5f, 6f, 13.5f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 8f, 7f, gap(8f, 3.5f), gap(4f, 7f))
                room(LineFeatureType.DUNGEON_WALL, 12f, 0f, 20f, 7f, gap(12f, 3.5f, MarkerType.B_LOCKED_DOOR))
                room(LineFeatureType.DUNGEON_WALL, 6f, 10f, 14f, 16f, gap(6f, 13f))
                wall(LineFeatureType.DUNGEON_WALL, 8f, 3f, 12f, 3f)
                wall(LineFeatureType.DUNGEON_WALL, 8f, 4f, 12f, 4f)
                wall(LineFeatureType.DUNGEON_WALL, 3.5f, 7f, 3.5f, 13.5f, 6f, 13.5f)
                wall(LineFeatureType.DUNGEON_WALL, 4.5f, 7f, 4.5f, 12.5f, 6f, 12.5f)
                put(MarkerType.B_SPIKE_TRAP, 10f, 3.5f)
                put(MarkerType.B_TREASURE, 18f, 1.5f)
                put(MarkerType.B_ALTAR, 10f, 13f)
                for (x in floatArrayOf(8f, 12f)) row(MarkerType.B_PILLAR, 11.5f, x)
                put(MarkerType.B_BONES, 2f, 5f)
                put(MarkerType.B_STAIRS_UP, 1.5f, 1.5f)
                put(MarkerType.B_TORCH, 7.3f, 0.7f)
                put(MarkerType.B_START_HEROES, 2.5f, 2.5f)
            }
            RoomTemplate.CAVE -> build(16, 12) {
                val outline = floatArrayOf(2f, 1f, 7f, 0.5f, 11f, 1.5f, 15f, 3f, 15.5f, 7f, 13f, 11f, 8f, 11.5f, 3f, 10.5f, 0.5f, 7f, 0.8f, 3f)
                floorShape(BiomeType.CAVE_FLOOR, *outline)
                floorShape(BiomeType.SHALLOW_WATER, 10f, 6f, 12.5f, 5.5f, 13.5f, 7.5f, 11.5f, 9f, 9.5f, 8f)
                shape(LineFeatureType.CAVE_WALL, outline, gap(0.6f, 5f, null, 2f))
                row(MarkerType.B_STALAGMITE, 3f, 5f, 12f)
                put(MarkerType.B_STALAGMITE, 7f, 9f)
                put(MarkerType.B_MUSHROOMS, 3.5f, 8.5f)
                put(MarkerType.B_CRYSTAL, 13.5f, 4f)
                put(MarkerType.B_ROCKS, 8f, 2.5f)
                put(MarkerType.B_BONES, 6f, 6f)
                put(MarkerType.B_CAMPFIRE, 4.5f, 5.5f)
                put(MarkerType.B_NEST, 8.5f, 5f)
            }
            RoomTemplate.CAMP -> build(14, 12) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 14f, 12f)
                floorCircle(BiomeType.DIRT_GROUND, 7f, 6f, 4.8f, 16)
                ring(LineFeatureType.STAKE_WALL, 7f, 6f, 5.5f, 12, gap(7f, 11.5f, null, 2f))
                put(MarkerType.B_CAMPFIRE, 7f, 6f)
                for ((x, y) in listOf(3.5f to 3.5f, 10.5f to 3.5f, 3f to 7.5f, 11f to 7.5f, 7f to 1.8f)) put(MarkerType.B_TENT, x, y)
                row(MarkerType.B_LOG, 7.3f, 5.5f, 8.5f)
                put(MarkerType.B_CART, 11f, 10.5f)
                put(MarkerType.B_WEAPON_RACK, 7f, 9f)
                put(MarkerType.B_HAY, 1.5f, 10.5f)
                put(MarkerType.B_BARREL, 9f, 4f)
            }
            RoomTemplate.BARRACKS -> build(14, 8) {
                floor(BiomeType.WOOD_FLOOR, 0f, 0f, 14f, 8f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 14f, 8f, gap(7f, 8f), gap(0f, 4f, MarkerType.B_WINDOW), gap(14f, 4f, MarkerType.B_WINDOW))
                row(MarkerType.B_BUNKS, 1.3f, 1.5f, 4f, 6.5f, 9f, 11.5f)
                row(MarkerType.B_BUNKS, 6.7f, 1.5f, 4f, 10f, 12.5f)
                row(MarkerType.B_WEAPON_RACK, 4f, 1f, 13f)
                put(MarkerType.B_LONG_TABLE, 7f, 4f)
                row(MarkerType.B_CHEST, 4.8f, 3f, 11f)
                put(MarkerType.B_ARMOR, 13f, 6.8f)
            }
            RoomTemplate.LIBRARY -> build(14, 12) {
                floor(BiomeType.WOOD_FLOOR, 0f, 0f, 14f, 12f)
                floor(BiomeType.CARPET, 2f, 6.5f, 12f, 11f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 14f, 12f, gap(7f, 12f, MarkerType.B_DOUBLE_DOOR, 2f))
                for (y in floatArrayOf(1f, 3f, 5f)) row(MarkerType.B_BOOKSHELF, y, 2f, 4.5f, 9.5f, 12f)
                row(MarkerType.B_TABLE, 8f, 4f, 10f)
                row(MarkerType.B_CHAIR, 9f, 3f, 5f, 9f, 11f)
                put(MarkerType.B_MAP_TABLE, 7f, 9.5f)
                row(MarkerType.B_CANDLES, 8f, 7f)
                put(MarkerType.B_BOOK, 4f, 7.6f)
                put(MarkerType.B_LADDER, 7f, 1.5f)
                put(MarkerType.B_SCROLL, 10f, 7.6f)
            }
            RoomTemplate.ARENA -> build(18, 18) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 18f, 18f)
                floorCircle(BiomeType.SAND_GROUND, 9f, 9f, 7f)
                ring(LineFeatureType.DUNGEON_WALL, 9f, 9f, 8f, 20,
                    gap(9f, 1f, MarkerType.B_BARRED_GATE, 2f), gap(9f, 17f, MarkerType.B_BARRED_GATE, 2f))
                for (i in 0 until 8) {
                    val a = i * PI.toFloat() / 4f + PI.toFloat() / 8f
                    put(MarkerType.B_PILLAR, 9f + cos(a) * 6.3f, 9f + sin(a) * 6.3f)
                }
                row(MarkerType.B_WEAPON_RACK, 2.5f, 7f, 11f)
                put(MarkerType.B_STATUE, 9f, 9f)
                put(MarkerType.B_START_HEROES, 9f, 15f)
                put(MarkerType.B_START_ENEMIES, 9f, 3f)
            }
            RoomTemplate.SHIP -> build(8, 20) {
                val hull = floatArrayOf(4f, 0f, 7f, 4f, 7.5f, 9f, 7f, 18f, 5.5f, 20f, 2.5f, 20f, 1f, 18f, 0.5f, 9f, 1f, 4f)
                floor(BiomeType.DEEP_WATER, 0f, 0f, 8f, 20f)
                floorShape(BiomeType.WOOD_FLOOR, *hull)
                shape(LineFeatureType.TIMBER_WALL, hull)
                row(MarkerType.B_PILLAR, 4f, 7f, 13f)
                put(MarkerType.B_TRAPDOOR, 4f, 10f)
                row(MarkerType.B_BARREL, 3f, 2.5f, 5.5f)
                row(MarkerType.B_CRATE, 16f, 2f, 6f)
                put(MarkerType.B_MAP_TABLE, 4f, 17.5f)
                put(MarkerType.B_STAIRS_DOWN, 4f, 15f)
                put(MarkerType.B_ROPE_BRIDGE, 7.5f, 11f)
            }
            RoomTemplate.MANOR -> build(18, 14) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 18f, 14f)
                floor(BiomeType.MARBLE_FLOOR, 1f, 1f, 17f, 10f)
                floor(BiomeType.CARPET, 7f, 1f, 11f, 10f)
                room(LineFeatureType.BRICK_WALL, 1f, 1f, 17f, 10f, gap(9f, 10f, MarkerType.B_DOUBLE_DOOR, 2f))
                room(LineFeatureType.BRICK_WALL, 1f, 1f, 7f, 6f, gap(7f, 4f))
                room(LineFeatureType.BRICK_WALL, 11f, 1f, 17f, 6f, gap(11f, 4f))
                put(MarkerType.B_STAIRS_UP, 9f, 2.5f)
                put(MarkerType.B_LONG_TABLE, 4f, 3.5f)
                row(MarkerType.B_CHAIR, 2.6f, 3f, 5f)
                row(MarkerType.B_CHAIR, 4.4f, 3f, 5f)
                put(MarkerType.B_BED, 15f, 2.5f)
                put(MarkerType.B_WARDROBE, 12f, 2f)
                put(MarkerType.B_FIREPLACE, 1.8f, 8f)
                put(MarkerType.B_CHANDELIER, 9f, 6.5f)
                row(MarkerType.B_STATUE, 8f, 5f, 13f)
                put(MarkerType.B_FOUNTAIN, 9f, 12.3f)
                row(MarkerType.B_BUSH, 12.5f, 3f, 15f)
            }
            RoomTemplate.GUILD_HALL -> build(16, 12) {
                floor(BiomeType.WOOD_FLOOR, 0f, 0f, 16f, 12f)
                floor(BiomeType.CARPET, 5f, 2f, 11f, 9f)
                room(LineFeatureType.TIMBER_WALL, 0f, 0f, 16f, 12f, gap(8f, 12f, MarkerType.B_DOUBLE_DOOR, 2f))
                room(LineFeatureType.TIMBER_WALL, 12f, 0f, 16f, 5f, gap(12f, 3f, MarkerType.B_LOCKED_DOOR))
                put(MarkerType.B_MAP_TABLE, 8f, 5f)
                row(MarkerType.B_BANNER, 0.7f, 3f, 8f)
                row(MarkerType.B_BENCH, 10f, 2f, 5f, 11f)
                put(MarkerType.B_BAR, 2f, 5f)
                put(MarkerType.B_WEAPON_RACK, 0.8f, 9f)
                put(MarkerType.B_TREASURE, 14f, 1.5f)
                put(MarkerType.B_BOOKSHELF, 15.2f, 3.5f)
                put(MarkerType.B_SCROLL, 9f, 5f)
            }
            RoomTemplate.WIZARD_TOWER -> build(12, 12) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 12f, 12f)
                floorCircle(BiomeType.TILE_FLOOR, 6f, 6f, 5.5f)
                floorCircle(BiomeType.MAGIC_CIRCLE, 6f, 6f, 2f)
                ring(LineFeatureType.DUNGEON_WALL, 6f, 6f, 5.5f, 20, gap(6f, 11.5f, MarkerType.B_IRON_DOOR))
                wall(LineFeatureType.CURTAIN_WALL, 1.5f, 3.5f, 4f, 3.5f)
                put(MarkerType.B_SUMMON_CIRCLE, 6f, 6f)
                put(MarkerType.B_SPIRAL_STAIRS, 9f, 3f)
                put(MarkerType.B_BOOKSHELF, 3f, 2f)
                put(MarkerType.B_ALCHEMY, 9.5f, 8f)
                put(MarkerType.B_CRYSTAL, 2.5f, 8f)
                put(MarkerType.B_BOOK, 6f, 3.2f)
                put(MarkerType.B_ARTIFACT, 6f, 8.5f)
                put(MarkerType.B_CANDLES, 3.5f, 6f)
            }
            RoomTemplate.ALCHEMIST -> build(12, 10) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 12f, 10f)
                floor(BiomeType.ACID_POOL, 9f, 7f, 11f, 9f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 12f, 10f, gap(6f, 10f), gap(12f, 3f, MarkerType.B_WINDOW))
                row(MarkerType.B_ALCHEMY, 2f, 3f, 6f)
                put(MarkerType.B_CAULDRON, 9f, 3f)
                row(MarkerType.B_SHELF, 0.8f, 1f, 11f)
                put(MarkerType.B_POTION, 4.5f, 2f)
                put(MarkerType.B_BOOKSHELF, 0.8f, 6f)
                put(MarkerType.B_CAGE, 3f, 8f)
                put(MarkerType.B_GAS_TRAP, 10f, 8f)
                put(MarkerType.B_BRAZIER, 9f, 5.5f)
            }
            RoomTemplate.STABLE -> build(16, 8) {
                floor(BiomeType.DIRT_GROUND, 0f, 0f, 16f, 8f)
                floor(BiomeType.STRAW_FLOOR, 0f, 0f, 16f, 3.5f)
                room(LineFeatureType.TIMBER_WALL, 0f, 0f, 16f, 8f, gap(0f, 5.5f, MarkerType.B_DOUBLE_DOOR, 2f), gap(16f, 5.5f, MarkerType.B_DOUBLE_DOOR, 2f))
                for (x in floatArrayOf(3f, 6f, 9f, 12f)) wall(LineFeatureType.FENCE, x, 0f, x, 3.5f)
                for (x in floatArrayOf(1.5f, 4.5f, 7.5f, 10.5f, 13.5f)) put(MarkerType.B_HAY, x, 1.5f)
                put(MarkerType.B_CART, 8f, 6f)
                put(MarkerType.B_BARREL, 14.5f, 7f)
                put(MarkerType.B_SACKS, 1.2f, 7f)
                put(MarkerType.B_LADDER, 15f, 4f)
            }
            RoomTemplate.WAREHOUSE -> build(16, 12) {
                floor(BiomeType.OLD_WOOD_FLOOR, 0f, 0f, 16f, 12f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 16f, 12f, gap(8f, 12f, MarkerType.B_DOUBLE_DOOR, 3f), gap(0f, 3f))
                for (y in floatArrayOf(2f, 5f, 8f)) row(MarkerType.B_CRATE_STACK, y, 3f, 6f, 10f, 13f)
                row(MarkerType.B_BARREL, 10.5f, 1.5f, 2.5f, 13.5f, 14.5f)
                put(MarkerType.B_SACKS, 5f, 10.5f)
                put(MarkerType.B_CART, 11f, 10.5f)
                put(MarkerType.B_LADDER, 15f, 1f)
                put(MarkerType.B_STASH, 1.5f, 8.5f)
            }
            RoomTemplate.SEWER -> build(18, 12) {
                floor(BiomeType.BRICK_FLOOR, 0f, 0f, 18f, 12f)
                floor(BiomeType.SHALLOW_WATER, 0f, 5f, 18f, 7f)
                floor(BiomeType.SHALLOW_WATER, 8f, 0f, 10f, 12f)
                wall(LineFeatureType.DUNGEON_WALL, 0f, 3f, 7f, 3f, 7f, 0f)
                wall(LineFeatureType.DUNGEON_WALL, 11f, 0f, 11f, 3f, 18f, 3f)
                wall(LineFeatureType.DUNGEON_WALL, 0f, 9f, 7f, 9f, 7f, 12f)
                wall(LineFeatureType.DUNGEON_WALL, 11f, 12f, 11f, 9f, 18f, 9f)
                wall(LineFeatureType.IRON_BARS, 16f, 3f, 16f, 5f)
                put(MarkerType.B_LADDER, 2f, 4f)
                put(MarkerType.B_TRAPDOOR, 2f, 4f)
                put(MarkerType.B_BONES, 14f, 8f)
                put(MarkerType.B_WEB, 12f, 4f)
                put(MarkerType.B_STASH, 5f, 8f)
                put(MarkerType.B_LEVER, 16.5f, 3.6f)
                put(MarkerType.B_FLOOD_TRAP, 9f, 9f)
            }
            RoomTemplate.MINE -> build(16, 14) {
                floor(BiomeType.ROCKY_GROUND, 0f, 0f, 16f, 14f)
                floorShape(BiomeType.CAVE_FLOOR, 1f, 1f, 6f, 1f, 6f, 5f, 12f, 5f, 12f, 3f, 15f, 3f, 15f, 12f, 10f, 13f, 9f, 8f, 3f, 8f, 1f, 5f)
                shape(LineFeatureType.CAVE_WALL, floatArrayOf(1f, 1f, 6f, 1f, 6f, 5f, 12f, 5f, 12f, 3f, 15f, 3f, 15f, 12f, 10f, 13f, 9f, 8f, 3f, 8f, 1f, 5f), gap(1f, 3f, null, 1.6f))
                for (x in floatArrayOf(4f, 8f, 11f)) put(MarkerType.B_PILLAR, x, 6.5f)
                put(MarkerType.B_CART, 7f, 6.5f)
                put(MarkerType.B_GEMS, 14f, 5f)
                put(MarkerType.B_GOLD, 13f, 11f)
                put(MarkerType.B_ROCKFALL, 11f, 9f)
                put(MarkerType.B_TORCH, 5.5f, 1.5f)
                put(MarkerType.B_LADDER, 3f, 2f)
                put(MarkerType.B_CRATE, 2f, 6.5f)
            }
            RoomTemplate.GRAVEYARD -> build(16, 14) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 16f, 14f)
                floor(BiomeType.MOSS_GROUND, 1f, 1f, 15f, 13f)
                floor(BiomeType.GRAVEL_GROUND, 7f, 1f, 9f, 13f)
                room(LineFeatureType.FENCE, 1f, 1f, 15f, 13f, gap(8f, 13f, MarkerType.B_BARRED_GATE, 2f))
                room(LineFeatureType.DUNGEON_WALL, 10f, 2f, 14f, 6f, gap(10f, 4f, MarkerType.B_IRON_DOOR))
                put(MarkerType.B_SARCOPHAGUS, 12f, 4f)
                for (y in floatArrayOf(3f, 5.5f, 8f, 10.5f)) row(MarkerType.B_COFFIN, y, 2.5f, 4.5f)
                row(MarkerType.B_COFFIN, 8.5f, 11f, 13f)
                row(MarkerType.B_COFFIN, 11f, 11f, 13f)
                put(MarkerType.B_DEAD_TREE, 5.5f, 12f)
                put(MarkerType.B_STATUE, 8f, 7f)
                put(MarkerType.B_BONES, 13f, 12f)
            }
            RoomTemplate.BRIDGE -> build(20, 12) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 20f, 12f)
                floorShape(BiomeType.DEEP_WATER, 6f, 0f, 14f, 0f, 13f, 12f, 7f, 12f)
                floor(BiomeType.OLD_WOOD_FLOOR, 5f, 5f, 15f, 7f)
                wall(LineFeatureType.FENCE, 5.5f, 5f, 14.5f, 5f)
                wall(LineFeatureType.FENCE, 5.5f, 7f, 14.5f, 7f)
                put(MarkerType.B_BOULDER, 3f, 3f)
                put(MarkerType.B_TREE, 2f, 9f)
                put(MarkerType.B_BIG_TREE, 17f, 2f)
                put(MarkerType.B_BUSH, 17f, 9.5f)
                put(MarkerType.B_REEDS, 7f, 10f)
                put(MarkerType.B_REEDS, 13f, 2f)
                put(MarkerType.B_AMBUSH, 16.5f, 6f)
            }
            RoomTemplate.CITY_GATE -> build(16, 12) {
                floor(BiomeType.COBBLE_FLOOR, 0f, 0f, 16f, 12f)
                floor(BiomeType.DIRT_GROUND, 6f, 7f, 10f, 12f)
                wall(LineFeatureType.DUNGEON_WALL, 0f, 6f, 4f, 6f)
                wall(LineFeatureType.DUNGEON_WALL, 12f, 6f, 16f, 6f)
                room(LineFeatureType.DUNGEON_WALL, 4f, 3f, 7f, 9f, gap(7f, 4.5f))
                room(LineFeatureType.DUNGEON_WALL, 9f, 3f, 12f, 9f, gap(9f, 4.5f))
                wall(LineFeatureType.IRON_BARS, 7f, 6f, 9f, 6f)
                put(MarkerType.B_BARRED_GATE, 8f, 6f)
                row(MarkerType.B_SPIRAL_STAIRS, 7.5f, 5.5f, 10.5f)
                row(MarkerType.B_ARROW_SLIT, 9f, 5.5f, 10.5f)
                put(MarkerType.B_LEVER, 5f, 4f)
                put(MarkerType.B_WEAPON_RACK, 11f, 4f)
                put(MarkerType.B_CART, 8f, 10f)
                put(MarkerType.B_BRAZIER, 8f, 2f)
            }
            RoomTemplate.INN_ROOMS -> build(16, 10) {
                floor(BiomeType.WOOD_FLOOR, 0f, 0f, 16f, 10f)
                floor(BiomeType.CARPET, 0f, 4f, 16f, 6f)
                room(LineFeatureType.TIMBER_WALL, 0f, 0f, 16f, 10f)
                for (x0 in floatArrayOf(0f, 4f, 8f, 12f)) {
                    room(LineFeatureType.TIMBER_WALL, x0, 0f, x0 + 4f, 4f, gap(x0 + 2f, 4f))
                    room(LineFeatureType.TIMBER_WALL, x0, 6f, x0 + 4f, 10f, gap(x0 + 2f, 6f))
                    put(MarkerType.B_BED, x0 + 1.2f, 1.3f)
                    put(MarkerType.B_BED, x0 + 1.2f, 8.7f)
                }
                row(MarkerType.B_CHEST, 1.2f, 3.2f, 11.2f)
                row(MarkerType.B_TABLE, 8.5f, 6.8f, 14.8f)
                put(MarkerType.B_STAIRS_DOWN, 15f, 5f)
                put(MarkerType.B_CANDLES, 8f, 5f)
            }
            RoomTemplate.BATHHOUSE -> build(14, 12) {
                floor(BiomeType.TILE_FLOOR, 0f, 0f, 14f, 12f)
                floor(BiomeType.SHALLOW_WATER, 3f, 3f, 11f, 8f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 14f, 12f, gap(7f, 12f))
                room(LineFeatureType.BRICK_WALL, 0f, 9f, 4f, 12f, gap(4f, 10.5f))
                for (x in floatArrayOf(3f, 11f)) for (y in floatArrayOf(3f, 8f)) put(MarkerType.B_PILLAR, x, y)
                row(MarkerType.B_BENCH, 1.2f, 4f, 7f, 10f)
                put(MarkerType.B_BATH, 12f, 10.5f)
                put(MarkerType.B_FOUNTAIN, 7f, 5.5f)
                put(MarkerType.B_WARDROBE, 1.5f, 10.5f)
                put(MarkerType.B_BRAZIER, 12.5f, 1.5f)
            }
            RoomTemplate.THIEVES_DEN -> build(14, 12) {
                floor(BiomeType.OLD_WOOD_FLOOR, 0f, 0f, 14f, 12f)
                floor(BiomeType.CARPET, 8f, 1f, 13f, 5f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 14f, 12f, gap(0f, 10f, MarkerType.B_SECRET_DOOR), gap(14f, 9f, MarkerType.B_LOCKED_DOOR))
                room(LineFeatureType.BRICK_WALL, 7f, 0f, 14f, 6f, gap(7f, 4f, MarkerType.B_SECRET_DOOR))
                put(MarkerType.B_MAP_TABLE, 10.5f, 3f)
                put(MarkerType.B_TREASURE, 13f, 1f)
                put(MarkerType.B_GOLD, 12f, 5f)
                put(MarkerType.B_ROUND_TABLE, 3f, 3f)
                row(MarkerType.B_CHAIR, 3f, 1.8f, 4.2f)
                put(MarkerType.B_BUNKS, 1.5f, 7f)
                put(MarkerType.B_CRATE_STACK, 5f, 10.5f)
                put(MarkerType.B_TRIPWIRE, 11f, 9f)
                put(MarkerType.B_TRAPDOOR, 9f, 8f)
                put(MarkerType.B_WEAPON_RACK, 6f, 0.8f)
            }
            RoomTemplate.DRAGON_LAIR -> build(22, 16) {
                val outline = floatArrayOf(2f, 3f, 8f, 0.5f, 15f, 1f, 20.5f, 4f, 21f, 11f, 16f, 15.5f, 7f, 15f, 1f, 12f, 0.5f, 7f)
                floorShape(BiomeType.CAVE_FLOOR, *outline)
                floorShape(BiomeType.ASH_GROUND, 9f, 4f, 16f, 5f, 17f, 11f, 10f, 12f, 7f, 8f)
                floorShape(BiomeType.LAVA_POOL, 17.5f, 2.5f, 20f, 4.5f, 19.5f, 7f, 17f, 6f)
                shape(LineFeatureType.CAVE_WALL, outline, gap(0.7f, 9f, null, 2.5f))
                put(MarkerType.B_GOLD, 12f, 8f)
                put(MarkerType.B_GEMS, 14f, 9.5f)
                put(MarkerType.B_TREASURE, 11f, 10f)
                put(MarkerType.B_ARTIFACT, 13f, 7f)
                row(MarkerType.B_BONES, 13f, 4f, 7f)
                put(MarkerType.B_STALAGMITE, 5f, 4f)
                put(MarkerType.B_STALAGMITE, 17f, 13f)
                put(MarkerType.B_START_ENEMIES, 13f, 6f)
                put(MarkerType.B_START_HEROES, 2.5f, 9f)
            }
            RoomTemplate.FOREST_CLEARING -> build(18, 14) {
                floor(BiomeType.FOREST_GROUND, 0f, 0f, 18f, 14f)
                floorCircle(BiomeType.GRASS_GROUND, 9f, 7f, 5f)
                floorShape(BiomeType.SHALLOW_WATER, 13f, 9f, 16f, 9.5f, 16.5f, 12f, 13.5f, 12.5f)
                wall(LineFeatureType.HEDGE, 0f, 2f, 4f, 1f, 7f, 0.5f)
                wall(LineFeatureType.HEDGE, 11f, 13.5f, 14f, 13.2f, 17.5f, 13.5f)
                for ((x, y) in listOf(2f to 4f, 1.5f to 9f, 4f to 12f, 15f to 3f, 16.5f to 6.5f, 11f to 1.5f, 6f to 1.8f))
                    put(MarkerType.B_TREE, x, y)
                put(MarkerType.B_BIG_TREE, 16f, 1.5f)
                put(MarkerType.B_CAMPFIRE, 9f, 7f)
                row(MarkerType.B_LOG, 8.5f, 7.5f, 10.5f)
                put(MarkerType.B_STUMP, 6f, 5f)
                put(MarkerType.B_FLOWERS, 11f, 4.5f)
                put(MarkerType.B_BOULDER, 3.5f, 7f)
                put(MarkerType.B_BUSH, 8f, 12.5f)
            }
            RoomTemplate.MARKET_SQUARE -> build(20, 16) {
                floor(BiomeType.COBBLE_FLOOR, 0f, 0f, 20f, 16f)
                floorCircle(BiomeType.STONE_FLOOR, 10f, 8f, 2.5f, 16)
                put(MarkerType.B_FOUNTAIN, 10f, 8f)
                for ((x, y) in listOf(3f to 3f, 7f to 3f, 13f to 3f, 17f to 3f, 3f to 13f, 7f to 13f, 13f to 13f, 17f to 13f))
                    put(MarkerType.B_TENT, x, y)
                row(MarkerType.B_CRATE, 5f, 2f, 18f)
                row(MarkerType.B_BARREL, 11f, 2f, 18f)
                put(MarkerType.B_CART, 5f, 8f)
                put(MarkerType.B_SACKS, 15f, 8f)
                room(LineFeatureType.FENCE, 0.5f, 0.5f, 19.5f, 15.5f, gap(0.5f, 8f, null, 3f), gap(19.5f, 8f, null, 3f), gap(10f, 0.5f, null, 3f), gap(10f, 15.5f, null, 3f))
                put(MarkerType.B_STOCKS, 10f, 12f)
            }
            RoomTemplate.TEMPLE_RUINS -> build(14, 16) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 14f, 16f)
                floor(BiomeType.RUBBLE_FLOOR, 1f, 1f, 13f, 15f)
                floor(BiomeType.MARBLE_FLOOR, 4f, 2f, 10f, 6f)
                wall(LineFeatureType.RUBBLE_LINE, 1f, 6f, 1f, 1f, 6f, 1f)
                wall(LineFeatureType.DUNGEON_WALL, 9f, 1f, 13f, 1f, 13f, 9f)
                wall(LineFeatureType.RUBBLE_LINE, 13f, 12f, 13f, 15f, 9f, 15f)
                wall(LineFeatureType.DUNGEON_WALL, 1f, 9f, 1f, 15f, 5f, 15f)
                put(MarkerType.B_ALTAR, 7f, 3f)
                for (y in floatArrayOf(6f, 9f, 12f)) row(MarkerType.B_PILLAR, y, 4f, 10f)
                row(MarkerType.B_BROKEN_PILLAR, 7.5f, 4.5f, 9.5f)
                put(MarkerType.B_STATUE, 11.5f, 3f)
                put(MarkerType.B_ROCKS, 3f, 12f)
                put(MarkerType.B_BUSH, 11f, 13f)
                put(MarkerType.B_RUNE_TRAP, 7f, 7f)
            }
        }
        return Plan(b.cols, b.rows, b.floors, b.walls, b.props)
    }

    private fun build(cols: Int, rows: Int, block: Builder.() -> Unit): Builder = Builder(cols, rows).apply(block)

    /**
     * Поставить шаблон на карту. [origin] — левый верхний угол, [cell] — размер клетки;
     * [width] и [height] — рамка, в которую растянуть (0 — натуральная величина).
     * Размер округляется до целых клеток, чтобы стены легли на сетку.
     */
    fun place(template: RoomTemplate, origin: Vec, cell: Float, width: Float = 0f, height: Float = 0f): Placed {
        val plan = plan(template)
        val cols = if (width > 0f) max(3f, round(width / cell)) else plan.cols.toFloat()
        val rows = if (height > 0f) max(3f, round(height / cell)) else plan.rows.toFloat()
        val sx = cols / plan.cols * cell
        val sy = rows / plan.rows * cell
        fun map(p: Vec) = Vec(origin.x + p.x * sx, origin.y + p.y * sy)
        val biomes = plan.floors.map { (type, pts) -> BiomeRegion(biome = type, points = pts.map(::map)) }
        val lines = plan.walls.map { (type, pts) -> LineFeature(type = type, points = pts.map(::map)) }
        val markers = plan.props.map { (type, p) -> Marker(type = type, pos = map(p), showLabel = false) }
        return Placed(biomes, lines, markers)
    }
}
