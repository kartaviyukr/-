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
    TEMPLE_RUINS("Руины храма", "🏚"),
    BAKERY("Пекарня", "🥖"),
    APOTHECARY("Лавка травника", "🌿"),
    WEAPON_SHOP("Оружейная лавка", "🗡"),
    BANK_VAULT("Банк и хранилище", "🏦"),
    TOWN_HALL("Ратуша", "🏛"),
    ACADEMY("Школа магии", "🎓"),
    OBSERVATORY("Обсерватория", "🔭"),
    MONASTERY("Монастырь с галереей", "📿"),
    WAYSIDE_SHRINE("Придорожное святилище", "🕯"),
    CATHEDRAL("Собор", "✝"),
    CULT_TEMPLE("Храм тёмного культа", "🩸"),
    NECROMANCER_LAIR("Логово некроманта", "💀"),
    GAMBLING_HALL("Игорный дом", "🎲"),
    THEATER("Театр", "🎭"),
    BALLROOM("Бальный зал", "💃"),
    KITCHEN("Кухня и кладовая", "🍲"),
    WINE_CELLAR("Винный погреб", "🍷"),
    ATTIC("Чердак", "🕸"),
    BASEMENT("Подвал", "🕯"),
    TORTURE_CHAMBER("Пыточная", "⛓"),
    TREASURE_VAULT("Сокровищница", "💎"),
    WAR_ROOM("Зал совета", "🗺"),
    ARMORY("Арсенал", "⚔"),
    BATTLEMENTS("Крепостная стена с башнями", "🧱"),
    KEEP("Донжон", "🏯"),
    WOODEN_FORT("Деревянный форт", "🪵"),
    DOCKS("Причал", "⚓"),
    FISHING_VILLAGE("Рыбацкая деревня", "🐟"),
    VILLAGE_SQUARE("Деревенская площадь", "🏘"),
    CITY_STREET("Городская улица", "🏙"),
    ROOFTOPS("Крыши города", "🏚"),
    CIRCUS("Цирк-шапито", "🎪"),
    CARAVAN_CAMP("Караван на привале", "🐫"),
    ROAD_AMBUSH("Засада на лесной дороге", "🏹"),
    CROSSROADS("Перекрёсток с виселицей", "🪧"),
    RIVER_FORD("Брод через реку", "🌊"),
    WATERFALL("Водопад и пещера за ним", "💧"),
    SMUGGLERS_COVE("Бухта контрабандистов", "🏴‍☠️"),
    SWAMP("Болото", "🐸"),
    WITCH_HUT("Хижина ведьмы", "🧙"),
    DRUID_GROVE("Роща друидов", "🌳"),
    STONE_CIRCLE("Каменный круг", "🗿"),
    MOUNTAIN_PASS("Горный перевал", "⛰"),
    CLIFFSIDE("Утёс над морем", "🌅"),
    OASIS("Оазис", "🌴"),
    BATTLEFIELD("Поле битвы", "⚔"),
    SIEGE_CAMP("Осадный лагерь", "🏹"),
    ABANDONED_VILLAGE("Разорённая деревня", "🏚"),
    RUINED_CASTLE("Руины замка", "🏰"),
    SHIPWRECK("Разбитый корабль на берегу", "🛶"),
    FEY_GLADE("Поляна фей", "🧚"),
    PYRAMID_TOMB("Гробница в пирамиде", "🔺"),
    ICE_CAVE("Ледяная пещера", "❄"),
    VOLCANO_FORGE("Жерло вулкана", "🌋"),
    UNDERDARK("Подземье: грибной лес", "🍄"),
    GOBLIN_CAVE("Логово гоблинов", "👺"),
    ORC_CAMP("Лагерь орков", "🪓"),
    BANDIT_HIDEOUT("Убежище разбойников", "🦹"),
    SPIDER_LAIR("Логово пауков", "🕷"),
    HAUNTED_HOUSE("Дом с привидениями", "👻"),
    SHIP_HOLD("Трюм корабля", "📦"),
    AIRSHIP("Палуба дирижабля", "🎈"),
    PORTAL_CHAMBER("Зал портала", "🌀"),
    MAZE("Лабиринт", "🌀"),
    PUZZLE_ROOM("Зал с головоломкой", "🧩"),
    BOSS_ROOM("Логово главного злодея", "👑"),
    GIANT_HALL("Чертог великанов", "🗿"),
    DWARF_HALL("Гномий чертог", "⛏"),
    ELF_TREEHOUSES("Эльфийские дома на деревьях", "🌲"),
    CATACOMBS("Катакомбы", "☠"),
    SUNKEN_TEMPLE("Затопленный храм", "🌊")
}
/** Разделы шаблонов — чтобы в сотне планировок было легко найти нужную. */
enum class TemplateGroup(val title: String) {
    TOWN("Город и дома"),
    FORT("Замки и крепости"),
    FAITH("Храмы и магия"),
    DUNGEON("Подземелья"),
    LAIR("Пещеры и логова"),
    WILD("Природа и дороги"),
    SEA("Вода и корабли");

    val templates: List<RoomTemplate> get() = RoomTemplate.entries.filter { it.group == this }
}

private val templateGroups: Map<RoomTemplate, TemplateGroup> = buildMap {
    for (t in listOf(RoomTemplate.TAVERN, RoomTemplate.HOUSE, RoomTemplate.SHOP, RoomTemplate.SMITHY, RoomTemplate.BARRACKS, RoomTemplate.LIBRARY, RoomTemplate.MANOR, RoomTemplate.GUILD_HALL, RoomTemplate.STABLE, RoomTemplate.WAREHOUSE, RoomTemplate.INN_ROOMS, RoomTemplate.BATHHOUSE, RoomTemplate.MARKET_SQUARE, RoomTemplate.FLOOR, RoomTemplate.BAKERY, RoomTemplate.APOTHECARY, RoomTemplate.WEAPON_SHOP, RoomTemplate.BANK_VAULT, RoomTemplate.TOWN_HALL, RoomTemplate.GAMBLING_HALL, RoomTemplate.THEATER, RoomTemplate.BALLROOM, RoomTemplate.KITCHEN, RoomTemplate.WINE_CELLAR, RoomTemplate.ATTIC, RoomTemplate.BASEMENT, RoomTemplate.VILLAGE_SQUARE, RoomTemplate.CITY_STREET, RoomTemplate.ROOFTOPS, RoomTemplate.CIRCUS, RoomTemplate.FISHING_VILLAGE, RoomTemplate.ACADEMY)) put(t, TemplateGroup.TOWN)
    for (t in listOf(RoomTemplate.PALACE, RoomTemplate.CASTLE, RoomTemplate.TOWER, RoomTemplate.PRISON, RoomTemplate.CITY_GATE, RoomTemplate.ARENA, RoomTemplate.WAR_ROOM, RoomTemplate.ARMORY, RoomTemplate.BATTLEMENTS, RoomTemplate.KEEP, RoomTemplate.WOODEN_FORT, RoomTemplate.SIEGE_CAMP, RoomTemplate.RUINED_CASTLE, RoomTemplate.TORTURE_CHAMBER, RoomTemplate.TREASURE_VAULT, RoomTemplate.BOSS_ROOM)) put(t, TemplateGroup.FORT)
    for (t in listOf(RoomTemplate.CHURCH, RoomTemplate.WIZARD_TOWER, RoomTemplate.ALCHEMIST, RoomTemplate.TEMPLE_RUINS, RoomTemplate.OBSERVATORY, RoomTemplate.MONASTERY, RoomTemplate.WAYSIDE_SHRINE, RoomTemplate.CATHEDRAL, RoomTemplate.CULT_TEMPLE, RoomTemplate.NECROMANCER_LAIR, RoomTemplate.PORTAL_CHAMBER, RoomTemplate.STONE_CIRCLE, RoomTemplate.DRUID_GROVE, RoomTemplate.FEY_GLADE)) put(t, TemplateGroup.FAITH)
    for (t in listOf(RoomTemplate.CRYPT, RoomTemplate.DUNGEON, RoomTemplate.SEWER, RoomTemplate.MINE, RoomTemplate.GRAVEYARD, RoomTemplate.PYRAMID_TOMB, RoomTemplate.MAZE, RoomTemplate.PUZZLE_ROOM, RoomTemplate.CATACOMBS, RoomTemplate.HAUNTED_HOUSE, RoomTemplate.DWARF_HALL, RoomTemplate.GIANT_HALL)) put(t, TemplateGroup.DUNGEON)
    for (t in listOf(RoomTemplate.CAVE, RoomTemplate.THIEVES_DEN, RoomTemplate.DRAGON_LAIR, RoomTemplate.ICE_CAVE, RoomTemplate.VOLCANO_FORGE, RoomTemplate.UNDERDARK, RoomTemplate.GOBLIN_CAVE, RoomTemplate.ORC_CAMP, RoomTemplate.BANDIT_HIDEOUT, RoomTemplate.SPIDER_LAIR, RoomTemplate.WITCH_HUT)) put(t, TemplateGroup.LAIR)
    for (t in listOf(RoomTemplate.CAMP, RoomTemplate.BRIDGE, RoomTemplate.FOREST_CLEARING, RoomTemplate.CARAVAN_CAMP, RoomTemplate.ROAD_AMBUSH, RoomTemplate.CROSSROADS, RoomTemplate.RIVER_FORD, RoomTemplate.WATERFALL, RoomTemplate.SWAMP, RoomTemplate.MOUNTAIN_PASS, RoomTemplate.OASIS, RoomTemplate.BATTLEFIELD, RoomTemplate.ABANDONED_VILLAGE, RoomTemplate.ELF_TREEHOUSES)) put(t, TemplateGroup.WILD)
    for (t in listOf(RoomTemplate.SHIP, RoomTemplate.DOCKS, RoomTemplate.SMUGGLERS_COVE, RoomTemplate.CLIFFSIDE, RoomTemplate.SHIPWRECK, RoomTemplate.SHIP_HOLD, RoomTemplate.AIRSHIP, RoomTemplate.SUNKEN_TEMPLE)) put(t, TemplateGroup.SEA)
}

val RoomTemplate.group: TemplateGroup get() = templateGroups.getValue(this)


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

        // ------------------------------------------------ помощники для больших наборов

        private var seed = cols * 7919 + rows * 104729

        /** Повторяемый «случайный» разброс: шаблон всегда одинаковый. */
        fun rnd(): Float {
            seed = seed * 1103515245 + 12345
            return ((seed ushr 8) and 0xFFFF) / 65535f
        }

        /** Несколько одинаковых предметов вразброс в прямоугольнике. */
        fun scatter(type: MarkerType, count: Int, x0: Float, y0: Float, x1: Float, y1: Float) {
            repeat(count) { put(type, x0 + rnd() * (x1 - x0), y0 + rnd() * (y1 - y0)) }
        }

        /** Неровный овал — лужа, поляна, пещера. Не выходит за rx, ry. */
        fun blob(cx: Float, cy: Float, rx: Float, ry: Float, sides: Int = 16): List<Vec> = List(sides) { i ->
            val a = i * 2f * PI.toFloat() / sides
            val k = 0.8f + 0.2f * rnd()
            Vec(cx + cos(a) * rx * k, cy + sin(a) * ry * k)
        }

        fun blobFloor(type: BiomeType, cx: Float, cy: Float, rx: Float, ry: Float) {
            floors.add(type to blob(cx, cy, rx, ry))
        }

        /** Пещера: неровный пол и стена по краю; входы — углы в градусах (0 — восток, 90 — юг). */
        fun cave(ground: BiomeType, wall: LineFeatureType, cx: Float, cy: Float, rx: Float, ry: Float, vararg entrances: Float) {
            val outlinePts = blob(cx, cy, rx, ry, 18)
            floors.add(ground to outlinePts)
            val gaps = entrances.map {
                val a = it * PI.toFloat() / 180f
                Gap(cx + cos(a) * rx * 0.9f, cy + sin(a) * ry * 0.9f, null, 1.8f)
            }
            outline(wall, outlinePts, gaps)
        }

        /** Дом: пол, стены и дверь посередине стороны N, S, W или E. */
        fun house(
            wall: LineFeatureType, ground: BiomeType,
            x0: Float, y0: Float, x1: Float, y1: Float,
            side: Char = 'S', door: MarkerType = MarkerType.B_DOOR
        ) {
            floor(ground, x0, y0, x1, y1)
            val g = when (side) {
                'N' -> Gap((x0 + x1) / 2f, y0, door)
                'W' -> Gap(x0, (y0 + y1) / 2f, door)
                'E' -> Gap(x1, (y0 + y1) / 2f, door)
                else -> Gap((x0 + x1) / 2f, y1, door)
            }
            outline(wall, rectangle(x0, y0, x1, y1), listOf(g))
        }

        /** Круг из предметов — стоячие камни, колонны, шатры. */
        fun circleOf(type: MarkerType, cx: Float, cy: Float, r: Float, count: Int) {
            for (i in 0 until count) {
                val a = i * 2f * PI.toFloat() / count
                put(type, cx + cos(a) * r, cy + sin(a) * r)
            }
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
                put(MarkerType.B_PILLAR, 4f, 7f)
                put(MarkerType.B_PILLAR, 4f, 13f)
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
            RoomTemplate.BAKERY -> build(10, 8) {
                house(LineFeatureType.BRICK_WALL, BiomeType.TILE_FLOOR, 0f, 0f, 10f, 8f, 'S')
                wall(LineFeatureType.BRICK_WALL, 6f, 0f, 6f, 3.5f)
                wall(LineFeatureType.BRICK_WALL, 6f, 4.5f, 6f, 8f)
                put(MarkerType.B_DOOR, 6f, 4f)
                put(MarkerType.B_FIREPLACE, 8f, 0.8f)
                put(MarkerType.B_TABLE, 8f, 3f)
                row(MarkerType.B_SACKS, 7f, 7f, 9f)
                put(MarkerType.B_BAR, 3f, 3f)
                row(MarkerType.B_SHELF, 0.8f, 1.5f, 4.5f)
                put(MarkerType.B_FOOD, 3f, 2.8f)
                put(MarkerType.B_BARREL, 1f, 7f)
            }
            RoomTemplate.APOTHECARY -> build(10, 8) {
                house(LineFeatureType.TIMBER_WALL, BiomeType.WOOD_FLOOR, 0f, 0f, 10f, 8f, 'S')
                put(MarkerType.B_BAR, 5f, 3.5f)
                row(MarkerType.B_SHELF, 0.8f, 2f, 5f, 8f)
                put(MarkerType.B_ALCHEMY, 8.5f, 2.5f)
                put(MarkerType.B_CAULDRON, 1.5f, 3f)
                row(MarkerType.B_POTION, 3.3f, 4f, 6f)
                put(MarkerType.B_FLOWERS, 1.2f, 6.8f)
                put(MarkerType.B_FLOWERS, 8.8f, 6.8f)
                put(MarkerType.B_BOOK, 8f, 4.5f)
            }
            RoomTemplate.WEAPON_SHOP -> build(10, 8) {
                house(LineFeatureType.BRICK_WALL, BiomeType.STONE_FLOOR, 0f, 0f, 10f, 8f, 'S')
                row(MarkerType.B_WEAPON_RACK, 0.8f, 2f, 5f, 8f)
                row(MarkerType.B_ARMOR, 3.5f, 0.8f, 9.2f)
                put(MarkerType.B_BAR, 5f, 4f)
                put(MarkerType.B_CHEST, 8.5f, 6.5f)
                put(MarkerType.B_WEAPON, 5f, 3.8f)
                put(MarkerType.B_DUMMY, 1.5f, 6.5f)
                put(MarkerType.B_ANVIL, 3f, 6.5f)
            }
            RoomTemplate.BANK_VAULT -> build(14, 10) {
                house(LineFeatureType.DUNGEON_WALL, BiomeType.MARBLE_FLOOR, 0f, 0f, 14f, 10f, 'S', MarkerType.B_DOUBLE_DOOR)
                room(LineFeatureType.DUNGEON_WALL, 9f, 0f, 14f, 5f, gap(11.5f, 5f, MarkerType.B_IRON_DOOR))
                floor(BiomeType.METAL_FLOOR, 9f, 0f, 14f, 5f)
                row(MarkerType.B_BAR, 4f, 2f, 5f)
                row(MarkerType.B_TREASURE, 1.2f, 10f, 13f)
                row(MarkerType.B_GOLD, 3.3f, 10.5f, 12.5f)
                put(MarkerType.B_GEMS, 11.5f, 2.3f)
                put(MarkerType.B_PRESSURE_PLATE, 11.5f, 6f)
                row(MarkerType.B_PILLAR, 8f, 2f, 6f)
                put(MarkerType.B_TABLE, 7f, 2f)
            }
            RoomTemplate.TOWN_HALL -> build(16, 12) {
                house(LineFeatureType.BRICK_WALL, BiomeType.WOOD_FLOOR, 0f, 0f, 16f, 12f, 'S', MarkerType.B_DOUBLE_DOOR)
                floor(BiomeType.CARPET, 6f, 2f, 10f, 12f)
                put(MarkerType.B_THRONE, 8f, 1.5f)
                put(MarkerType.B_LONG_TABLE, 8f, 4f)
                for (y in floatArrayOf(6.5f, 8f, 9.5f)) row(MarkerType.B_BENCH, y, 3.5f, 12.5f)
                row(MarkerType.B_BANNER, 0.7f, 5f, 11f)
                row(MarkerType.B_BOOKSHELF, 1.5f, 1f, 15f)
                put(MarkerType.B_MAP_TABLE, 2f, 4.5f)
                put(MarkerType.B_CHANDELIER, 8f, 7f)
            }
            RoomTemplate.ACADEMY -> build(16, 12) {
                house(LineFeatureType.DUNGEON_WALL, BiomeType.TILE_FLOOR, 0f, 0f, 16f, 12f, 'S', MarkerType.B_DOUBLE_DOOR)
                floor(BiomeType.CARPET, 0f, 5f, 16f, 7f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 8f, 5f, gap(4f, 5f))
                room(LineFeatureType.BRICK_WALL, 8f, 0f, 16f, 5f, gap(12f, 5f))
                room(LineFeatureType.BRICK_WALL, 0f, 7f, 7f, 12f, gap(3.5f, 7f))
                for (y in floatArrayOf(2f, 3.5f)) row(MarkerType.B_BENCH, y, 2f, 4f, 6f)
                put(MarkerType.B_BOOK, 4f, 0.8f)
                put(MarkerType.B_SUMMON_CIRCLE, 12f, 2.5f)
                put(MarkerType.B_CRYSTAL, 14.5f, 1f)
                row(MarkerType.B_BOOKSHELF, 8f, 1f, 3f, 5f)
                put(MarkerType.B_ALCHEMY, 3.5f, 10.5f)
                put(MarkerType.B_STATUE, 11.5f, 9.5f)
                put(MarkerType.B_FOUNTAIN, 11.5f, 9.5f)
            }
            RoomTemplate.OBSERVATORY -> build(12, 12) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 12f, 12f)
                floorCircle(BiomeType.TILE_FLOOR, 6f, 6f, 5.5f)
                ring(LineFeatureType.DUNGEON_WALL, 6f, 6f, 5.5f, 20, gap(6f, 11.5f))
                floorCircle(BiomeType.MAGIC_CIRCLE, 6f, 5f, 2f, 16)
                put(MarkerType.B_MAP_TABLE, 6f, 5f)
                put(MarkerType.B_SPIRAL_STAIRS, 9.5f, 6f)
                row(MarkerType.B_BOOKSHELF, 2.5f, 3.5f, 8.5f)
                put(MarkerType.B_SCROLL, 3f, 7f)
                put(MarkerType.B_CANDLES, 6f, 8.5f)
                put(MarkerType.B_CRYSTAL, 2.5f, 5.5f)
            }
            RoomTemplate.MONASTERY -> build(18, 18) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 18f, 18f)
                floor(BiomeType.GRASS_GROUND, 5f, 5f, 13f, 13f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 18f, 18f, gap(9f, 18f, MarkerType.B_DOUBLE_DOOR, 2f))
                for (x in floatArrayOf(5f, 7f, 9f, 11f, 13f)) { put(MarkerType.B_PILLAR, x, 5f); put(MarkerType.B_PILLAR, x, 13f) }
                for (y in floatArrayOf(7f, 9f, 11f)) { put(MarkerType.B_PILLAR, 5f, y); put(MarkerType.B_PILLAR, 13f, y) }
                put(MarkerType.B_WELL, 9f, 9f)
                row(MarkerType.B_FLOWERS, 7f, 7f, 11f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 18f, 3f, gap(9f, 3f))
                put(MarkerType.B_ALTAR, 9f, 1.2f)
                row(MarkerType.B_BUNKS, 16f, 1.5f, 3.5f, 14.5f, 16.5f)
                put(MarkerType.B_LONG_TABLE, 16f, 9f)
                put(MarkerType.B_BELL, 1.5f, 9f)
            }
            RoomTemplate.WAYSIDE_SHRINE -> build(10, 10) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 10f, 10f)
                floor(BiomeType.DIRT_GROUND, 4f, 0f, 6f, 10f)
                floorCircle(BiomeType.HOLY_GROUND, 5f, 5f, 2f, 12)
                ring(LineFeatureType.FENCE, 5f, 5f, 3f, 10, gap(5f, 8f, null, 1.5f), gap(5f, 2f, null, 1.5f))
                put(MarkerType.B_ALTAR, 5f, 5f)
                put(MarkerType.B_STATUE, 5f, 4f)
                put(MarkerType.B_CANDLES, 4f, 5.5f)
                put(MarkerType.B_FLOWERS, 6f, 5.5f)
                row(MarkerType.B_TREE, 1.5f, 1.5f, 8.5f)
                row(MarkerType.B_BUSH, 8.5f, 1.5f, 8.5f)
            }
            RoomTemplate.CATHEDRAL -> build(18, 26) {
                floor(BiomeType.MARBLE_FLOOR, 0f, 0f, 18f, 26f)
                floor(BiomeType.CARPET, 8f, 5f, 10f, 26f)
                floor(BiomeType.HOLY_GROUND, 6f, 1f, 12f, 4f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 18f, 26f, gap(9f, 26f, MarkerType.B_DOUBLE_DOOR, 3f), gap(0f, 8f), gap(18f, 8f))
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 4f, 5f, gap(4f, 3f))
                room(LineFeatureType.BRICK_WALL, 14f, 0f, 18f, 5f, gap(14f, 3f))
                put(MarkerType.B_ALTAR, 9f, 2.5f)
                row(MarkerType.B_CANDLES, 2f, 7f, 11f)
                for (y in floatArrayOf(7f, 10f, 13f, 16f, 19f, 22f)) row(MarkerType.B_PILLAR, y, 4.5f, 13.5f)
                for (y in floatArrayOf(9f, 11f, 13f, 15f, 17f, 19f, 21f)) row(MarkerType.B_BENCH, y, 6f, 12f)
                row(MarkerType.B_CHANDELIER, 12f, 9f)
                row(MarkerType.B_CHANDELIER, 20f, 9f)
                put(MarkerType.B_TREASURE, 2f, 2f)
                put(MarkerType.B_BOOKSHELF, 16f, 1.5f)
                row(MarkerType.B_STATUE, 24.5f, 2f, 16f)
            }
            RoomTemplate.CULT_TEMPLE -> build(16, 16) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 16f, 16f)
                floorCircle(BiomeType.CURSED_GROUND, 8f, 7f, 4f)
                floorCircle(BiomeType.BLOOD_POOL, 8f, 7f, 1.2f, 12)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 16f, 16f, gap(8f, 16f, MarkerType.B_IRON_DOOR), gap(16f, 3f, MarkerType.B_SECRET_DOOR))
                put(MarkerType.B_SUMMON_CIRCLE, 8f, 7f)
                circleOf(MarkerType.B_CANDLES, 8f, 7f, 3.5f, 6)
                put(MarkerType.B_ALTAR, 8f, 2f)
                row(MarkerType.B_STATUE, 1.5f, 2f, 14f)
                row(MarkerType.B_PILLAR, 12.5f, 3f, 13f)
                put(MarkerType.B_CAGE, 2f, 12f)
                put(MarkerType.B_BONES, 14f, 12f)
                put(MarkerType.B_BOOK, 9.5f, 2f)
                put(MarkerType.B_START_ENEMIES, 8f, 4f)
            }
            RoomTemplate.NECROMANCER_LAIR -> build(16, 12) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 16f, 12f)
                floor(BiomeType.BONE_PILE, 11f, 7f, 16f, 12f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 16f, 12f, gap(0f, 6f, MarkerType.B_IRON_DOOR))
                room(LineFeatureType.DUNGEON_WALL, 11f, 7f, 16f, 12f, gap(11f, 9.5f, MarkerType.B_BARRED_GATE))
                put(MarkerType.B_ALCHEMY, 3f, 1.5f)
                row(MarkerType.B_COFFIN, 1.5f, 7f, 9f)
                put(MarkerType.B_SARCOPHAGUS, 8f, 6f)
                put(MarkerType.B_SUMMON_CIRCLE, 5f, 8f)
                row(MarkerType.B_BOOKSHELF, 0.8f, 12f, 14f)
                put(MarkerType.B_CAULDRON, 14f, 3.5f)
                scatter(MarkerType.B_BONES, 3, 11.5f, 7.5f, 15.5f, 11.5f)
                put(MarkerType.B_CANDLES, 8f, 4f)
            }
            RoomTemplate.GAMBLING_HALL -> build(14, 10) {
                house(LineFeatureType.BRICK_WALL, BiomeType.CARPET, 0f, 0f, 14f, 10f, 'S', MarkerType.B_DOUBLE_DOOR)
                room(LineFeatureType.BRICK_WALL, 10f, 0f, 14f, 4f, gap(10f, 2f, MarkerType.B_LOCKED_DOOR))
                row(MarkerType.B_ROUND_TABLE, 3f, 2.5f, 6.5f)
                row(MarkerType.B_ROUND_TABLE, 6.5f, 2.5f, 6.5f)
                row(MarkerType.B_CHAIR, 3f, 1.4f, 3.6f, 5.4f, 7.6f)
                put(MarkerType.B_BAR, 11.5f, 7f)
                put(MarkerType.B_TREASURE, 13f, 1f)
                put(MarkerType.B_GOLD, 11.5f, 2.5f)
                put(MarkerType.B_CHANDELIER, 5f, 5f)
                put(MarkerType.B_BARREL, 13.3f, 9f)
            }
            RoomTemplate.THEATER -> build(16, 16) {
                floor(BiomeType.WOOD_FLOOR, 0f, 0f, 16f, 16f)
                floor(BiomeType.OLD_WOOD_FLOOR, 2f, 1f, 14f, 5f)
                floor(BiomeType.CARPET, 7f, 5f, 9f, 16f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 16f, 16f, gap(8f, 16f, MarkerType.B_DOUBLE_DOOR, 2f), gap(0f, 3f))
                wall(LineFeatureType.CURTAIN_WALL, 2f, 5f, 6f, 5f)
                wall(LineFeatureType.CURTAIN_WALL, 10f, 5f, 14f, 5f)
                for (y in floatArrayOf(7f, 8.5f, 10f, 11.5f, 13f)) row(MarkerType.B_BENCH, y, 4f, 12f)
                put(MarkerType.B_CHANDELIER, 8f, 10f)
                row(MarkerType.B_TORCH, 1f, 2.5f, 13.5f)
                put(MarkerType.B_TRAPDOOR, 8f, 3f)
                put(MarkerType.B_CRATE_STACK, 14.5f, 2f)
            }
            RoomTemplate.BALLROOM -> build(18, 14) {
                floor(BiomeType.MARBLE_FLOOR, 0f, 0f, 18f, 14f)
                floor(BiomeType.CARPET, 0f, 0f, 18f, 2.5f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 18f, 14f, gap(9f, 14f, MarkerType.B_DOUBLE_DOOR, 2f), gap(0f, 7f), gap(18f, 7f), gap(4f, 0f, MarkerType.B_WINDOW), gap(14f, 0f, MarkerType.B_WINDOW))
                row(MarkerType.B_CHANDELIER, 7f, 5f, 9f, 13f)
                for (x in floatArrayOf(3f, 6f, 12f, 15f)) { put(MarkerType.B_PILLAR, x, 4f); put(MarkerType.B_PILLAR, x, 10f) }
                put(MarkerType.B_LONG_TABLE, 9f, 12f)
                row(MarkerType.B_FOOD, 12f, 7.5f, 10.5f)
                put(MarkerType.B_THRONE, 9f, 1.3f)
                row(MarkerType.B_FLOWERS, 1.2f, 2f, 16f)
            }
            RoomTemplate.KITCHEN -> build(12, 10) {
                house(LineFeatureType.BRICK_WALL, BiomeType.TILE_FLOOR, 0f, 0f, 12f, 10f, 'S')
                room(LineFeatureType.BRICK_WALL, 8f, 0f, 12f, 6f, gap(8f, 3f))
                floor(BiomeType.STONE_FLOOR, 8f, 0f, 12f, 6f)
                put(MarkerType.B_FIREPLACE, 1f, 1f)
                put(MarkerType.B_CAULDRON, 3f, 1.5f)
                put(MarkerType.B_LONG_TABLE, 4f, 5f)
                row(MarkerType.B_SHELF, 0.8f, 5.5f)
                row(MarkerType.B_SACKS, 1.5f, 9f, 11f)
                row(MarkerType.B_BARREL, 4f, 9f, 11f)
                put(MarkerType.B_FOOD, 4f, 4.8f)
                put(MarkerType.B_WELL, 1.5f, 8f)
                put(MarkerType.B_TRAPDOOR, 10f, 5f)
            }
            RoomTemplate.WINE_CELLAR -> build(12, 10) {
                floor(BiomeType.BRICK_FLOOR, 0f, 0f, 12f, 10f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 12f, 10f)
                put(MarkerType.B_STAIRS_UP, 1.5f, 1.5f)
                for (y in floatArrayOf(3f, 5.5f, 8f)) row(MarkerType.B_BARREL, y, 4f, 5f, 6f, 8f, 9f, 10f)
                row(MarkerType.B_SHELF, 0.8f, 6f, 10f)
                put(MarkerType.B_TABLE, 2f, 7f)
                put(MarkerType.B_CANDLES, 2f, 5f)
                put(MarkerType.B_SECRET_DOOR, 12f, 5f)
                put(MarkerType.B_STASH, 11f, 9f)
            }
            RoomTemplate.ATTIC -> build(12, 8) {
                floor(BiomeType.OLD_WOOD_FLOOR, 0f, 0f, 12f, 8f)
                room(LineFeatureType.TIMBER_WALL, 0f, 0f, 12f, 8f, gap(0f, 4f, MarkerType.B_WINDOW), gap(12f, 4f, MarkerType.B_WINDOW))
                put(MarkerType.B_TRAPDOOR, 6f, 4f)
                put(MarkerType.B_LADDER, 6f, 4.8f)
                row(MarkerType.B_PILLAR, 4f, 3f, 9f)
                scatter(MarkerType.B_CRATE, 4, 1f, 1f, 5f, 7f)
                put(MarkerType.B_WARDROBE, 10.5f, 1f)
                put(MarkerType.B_CHEST, 10.5f, 6.8f)
                put(MarkerType.B_WEB, 1f, 1f)
                put(MarkerType.B_WEB, 11f, 7f)
                put(MarkerType.B_NEST, 8f, 1.5f)
            }
            RoomTemplate.BASEMENT -> build(12, 10) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 12f, 10f)
                floor(BiomeType.SHALLOW_WATER, 8f, 6f, 12f, 10f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 12f, 10f)
                room(LineFeatureType.BRICK_WALL, 0f, 0f, 4f, 4f, gap(4f, 2f))
                put(MarkerType.B_STAIRS_UP, 1.5f, 8.5f)
                put(MarkerType.B_PILLAR, 6f, 5f)
                scatter(MarkerType.B_CRATE_STACK, 2, 5f, 1f, 11f, 4f)
                put(MarkerType.B_BARREL, 6f, 8.5f)
                put(MarkerType.B_CHEST, 1.5f, 1.5f)
                put(MarkerType.B_CAGE, 3f, 2.5f)
                put(MarkerType.B_TRAPDOOR, 10f, 2f)
            }
            RoomTemplate.TORTURE_CHAMBER -> build(10, 10) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 10f, 10f)
                floor(BiomeType.BLOOD_POOL, 4f, 4f, 6f, 6f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 10f, 10f, gap(5f, 10f, MarkerType.B_IRON_DOOR))
                put(MarkerType.B_RACK, 5f, 5f)
                put(MarkerType.B_BRAZIER, 2f, 2f)
                row(MarkerType.B_CAGE, 1.5f, 6f, 8.5f)
                put(MarkerType.B_STOCKS, 1.5f, 7f)
                put(MarkerType.B_WEAPON_RACK, 8.8f, 5f)
                put(MarkerType.B_TABLE, 8f, 8f)
                put(MarkerType.B_KEY, 8.2f, 8f)
                put(MarkerType.B_BONES, 2f, 8.8f)
            }
            RoomTemplate.TREASURE_VAULT -> build(12, 12) {
                floor(BiomeType.METAL_FLOOR, 0f, 0f, 12f, 12f)
                floor(BiomeType.MARBLE_FLOOR, 3f, 3f, 9f, 9f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 12f, 12f, gap(6f, 12f, MarkerType.B_IRON_DOOR))
                put(MarkerType.B_ARTIFACT, 6f, 6f)
                circleOf(MarkerType.B_TREASURE, 6f, 6f, 4.2f, 6)
                row(MarkerType.B_GOLD, 1.3f, 1.3f, 10.7f)
                row(MarkerType.B_GEMS, 10.7f, 1.3f, 10.7f)
                row(MarkerType.B_PRESSURE_PLATE, 9.8f, 5f, 7f)
                put(MarkerType.B_ARROW_TRAP, 0.8f, 6f)
                put(MarkerType.B_ARROW_TRAP, 11.2f, 6f)
                put(MarkerType.B_RUNE_TRAP, 6f, 8.5f)
            }
            RoomTemplate.WAR_ROOM -> build(12, 10) {
                house(LineFeatureType.DUNGEON_WALL, BiomeType.WOOD_FLOOR, 0f, 0f, 12f, 10f, 'S', MarkerType.B_DOUBLE_DOOR)
                floor(BiomeType.CARPET, 2f, 2f, 10f, 8f)
                put(MarkerType.B_MAP_TABLE, 6f, 5f)
                row(MarkerType.B_CHAIR, 3.5f, 3.5f, 5f, 6.5f, 8.5f)
                row(MarkerType.B_CHAIR, 6.5f, 3.5f, 5f, 6.5f, 8.5f)
                row(MarkerType.B_BANNER, 0.7f, 3f, 9f)
                put(MarkerType.B_WEAPON_RACK, 0.8f, 5f)
                put(MarkerType.B_BOOKSHELF, 11.2f, 5f)
                put(MarkerType.B_FIREPLACE, 6f, 0.8f)
            }
            RoomTemplate.ARMORY -> build(12, 8) {
                house(LineFeatureType.DUNGEON_WALL, BiomeType.STONE_FLOOR, 0f, 0f, 12f, 8f, 'W', MarkerType.B_IRON_DOOR)
                for (x in floatArrayOf(3f, 5.5f, 8f, 10.5f)) { put(MarkerType.B_WEAPON_RACK, x, 1f); put(MarkerType.B_ARMOR, x, 7f) }
                put(MarkerType.B_CRATE_STACK, 6.5f, 4f)
                put(MarkerType.B_BARREL, 11f, 4f)
                put(MarkerType.B_WEAPON, 3f, 4f)
            }
            RoomTemplate.BATTLEMENTS -> build(20, 10) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 20f, 10f)
                floor(BiomeType.CASTLE_FLOOR, 0f, 3f, 20f, 6f)
                floorCircle(BiomeType.CASTLE_FLOOR, 4f, 4.5f, 2.2f, 12)
                floorCircle(BiomeType.CASTLE_FLOOR, 16f, 4.5f, 2.2f, 12)
                wall(LineFeatureType.DUNGEON_WALL, 0f, 3f, 20f, 3f)
                wall(LineFeatureType.DUNGEON_WALL, 0f, 6f, 20f, 6f)
                ring(LineFeatureType.DUNGEON_WALL, 4f, 4.5f, 2.2f, 12, gap(4f, 6.7f))
                ring(LineFeatureType.DUNGEON_WALL, 16f, 4.5f, 2.2f, 12, gap(16f, 6.7f))
                row(MarkerType.B_ARROW_SLIT, 3f, 8f, 10f, 12f)
                row(MarkerType.B_SPIRAL_STAIRS, 4.5f, 4f, 16f)
                put(MarkerType.B_BRAZIER, 10f, 4.5f)
                row(MarkerType.B_BARREL, 8f, 7f, 13f)
            }
            RoomTemplate.KEEP -> build(12, 12) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 12f, 12f)
                floor(BiomeType.CASTLE_FLOOR, 1f, 1f, 11f, 11f)
                room(LineFeatureType.DUNGEON_WALL, 1f, 1f, 11f, 11f, gap(6f, 11f, MarkerType.B_IRON_DOOR), gap(1f, 6f, MarkerType.B_ARROW_SLIT, 0.8f), gap(11f, 6f, MarkerType.B_ARROW_SLIT, 0.8f))
                room(LineFeatureType.DUNGEON_WALL, 1f, 1f, 5f, 5f, gap(5f, 3f))
                put(MarkerType.B_SPIRAL_STAIRS, 3f, 3f)
                put(MarkerType.B_LONG_TABLE, 7.5f, 5f)
                put(MarkerType.B_FIREPLACE, 10.2f, 3f)
                put(MarkerType.B_WEAPON_RACK, 2f, 8f)
                put(MarkerType.B_WELL, 8.5f, 8.5f)
                put(MarkerType.B_BANNER, 8f, 1.6f)
            }
            RoomTemplate.WOODEN_FORT -> build(20, 20) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 20f, 20f)
                floor(BiomeType.DIRT_GROUND, 2f, 2f, 18f, 18f)
                room(LineFeatureType.STAKE_WALL, 2f, 2f, 18f, 18f, gap(10f, 18f, MarkerType.B_BARRED_GATE, 2f))
                house(LineFeatureType.TIMBER_WALL, BiomeType.WOOD_FLOOR, 4f, 4f, 11f, 9f, 'S')
                house(LineFeatureType.TIMBER_WALL, BiomeType.WOOD_FLOOR, 13f, 4f, 17f, 11f, 'W')
                row(MarkerType.B_LADDER, 3f, 3f, 17f)
                row(MarkerType.B_BUNKS, 5.5f, 5.5f, 7.5f, 9.5f)
                put(MarkerType.B_TABLE, 15f, 7f)
                put(MarkerType.B_CAMPFIRE, 7f, 13f)
                put(MarkerType.B_WELL, 14f, 14f)
                put(MarkerType.B_WEAPON_RACK, 4f, 15f)
                put(MarkerType.B_HAY, 16.5f, 16.5f)
            }
            RoomTemplate.DOCKS -> build(20, 14) {
                floor(BiomeType.DEEP_WATER, 0f, 0f, 20f, 14f)
                floor(BiomeType.COBBLE_FLOOR, 0f, 0f, 20f, 4f)
                floor(BiomeType.OLD_WOOD_FLOOR, 3f, 4f, 6f, 12f)
                floor(BiomeType.OLD_WOOD_FLOOR, 12f, 4f, 15f, 10f)
                wall(LineFeatureType.FENCE, 3f, 4f, 3f, 12f, 6f, 12f, 6f, 4f)
                wall(LineFeatureType.FENCE, 12f, 4f, 12f, 10f, 15f, 10f, 15f, 4f)
                house(LineFeatureType.BRICK_WALL, BiomeType.OLD_WOOD_FLOOR, 16f, 0f, 20f, 3.5f, 'W')
                scatter(MarkerType.B_CRATE, 4, 7f, 0.5f, 15f, 3.5f)
                row(MarkerType.B_BARREL, 1f, 1f, 2f)
                row(MarkerType.B_ROPE_BRIDGE, 8f, 4.5f)
                put(MarkerType.B_CART, 10f, 2f)
                put(MarkerType.B_SACKS, 13.5f, 5f)
                put(MarkerType.B_BELL, 13.5f, 9f)
            }
            RoomTemplate.FISHING_VILLAGE -> build(20, 16) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 20f, 16f)
                floorShape(BiomeType.SAND_GROUND, 0f, 10f, 20f, 9f, 20f, 16f, 0f, 16f)
                floorShape(BiomeType.SHALLOW_WATER, 0f, 13f, 20f, 12.5f, 20f, 16f, 0f, 16f)
                house(LineFeatureType.TIMBER_WALL, BiomeType.WOOD_FLOOR, 1f, 1f, 6f, 5f, 'S')
                house(LineFeatureType.TIMBER_WALL, BiomeType.WOOD_FLOOR, 8f, 1f, 12f, 5f, 'S')
                house(LineFeatureType.TIMBER_WALL, BiomeType.WOOD_FLOOR, 14f, 2f, 19f, 6f, 'S')
                floor(BiomeType.OLD_WOOD_FLOOR, 9f, 10f, 11f, 16f)
                put(MarkerType.B_CAMPFIRE, 10f, 7.5f)
                row(MarkerType.B_BARREL, 8f, 3f, 16f)
                put(MarkerType.B_NET_TRAP, 5f, 11f)
                put(MarkerType.B_LOG, 13f, 11f)
                row(MarkerType.B_BED, 2f, 2.5f, 15.5f)
                put(MarkerType.B_TABLE, 10f, 2.5f)
            }
            RoomTemplate.VILLAGE_SQUARE -> build(20, 16) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 20f, 16f)
                floorCircle(BiomeType.COBBLE_FLOOR, 10f, 8f, 4f, 16)
                floor(BiomeType.DIRT_GROUND, 9f, 0f, 11f, 16f)
                floor(BiomeType.DIRT_GROUND, 0f, 7f, 20f, 9f)
                house(LineFeatureType.TIMBER_WALL, BiomeType.WOOD_FLOOR, 1f, 1f, 6f, 5f, 'E')
                house(LineFeatureType.TIMBER_WALL, BiomeType.WOOD_FLOOR, 14f, 1f, 19f, 5f, 'W')
                house(LineFeatureType.TIMBER_WALL, BiomeType.WOOD_FLOOR, 1f, 11f, 6f, 15f, 'E')
                house(LineFeatureType.BRICK_WALL, BiomeType.WOOD_FLOOR, 14f, 11f, 19f, 15f, 'W', MarkerType.B_DOUBLE_DOOR)
                put(MarkerType.B_WELL, 10f, 8f)
                put(MarkerType.B_TREE, 12.5f, 5.5f)
                put(MarkerType.B_CART, 7f, 10.5f)
                put(MarkerType.B_BAR, 16.5f, 12f)
                row(MarkerType.B_BED, 2.5f, 2f, 17.5f)
                put(MarkerType.B_HAY, 3f, 13f)
                put(MarkerType.B_BENCH, 12.5f, 10.5f)
            }
            RoomTemplate.CITY_STREET -> build(20, 10) {
                floor(BiomeType.COBBLE_FLOOR, 0f, 0f, 20f, 10f)
                house(LineFeatureType.BRICK_WALL, BiomeType.WOOD_FLOOR, 0f, 0f, 6f, 3.5f, 'S')
                house(LineFeatureType.BRICK_WALL, BiomeType.WOOD_FLOOR, 7f, 0f, 13f, 3.5f, 'S')
                house(LineFeatureType.BRICK_WALL, BiomeType.WOOD_FLOOR, 14f, 0f, 20f, 3.5f, 'S')
                house(LineFeatureType.BRICK_WALL, BiomeType.WOOD_FLOOR, 0f, 6.5f, 8f, 10f, 'N')
                house(LineFeatureType.BRICK_WALL, BiomeType.WOOD_FLOOR, 12f, 6.5f, 20f, 10f, 'N')
                floor(BiomeType.MUD_GROUND, 8f, 6.5f, 12f, 10f)
                row(MarkerType.B_BARREL, 5f, 6.5f, 13.5f)
                put(MarkerType.B_CART, 10f, 5f)
                put(MarkerType.B_CRATE_STACK, 9f, 8.5f)
                put(MarkerType.B_TORCH, 3f, 4f)
                put(MarkerType.B_TORCH, 17f, 4f)
                row(MarkerType.B_TABLE, 1.8f, 3f, 10f, 17f)
                row(MarkerType.B_BED, 8.3f, 3f, 16f)
                put(MarkerType.B_AMBUSH, 10f, 9f)
            }
            RoomTemplate.ROOFTOPS -> build(18, 14) {
                floor(BiomeType.COBBLE_FLOOR, 0f, 0f, 18f, 14f)
                floor(BiomeType.ROOF_FLOOR, 0f, 0f, 7f, 6f)
                floor(BiomeType.ROOF_FLOOR, 9f, 0f, 18f, 5f)
                floor(BiomeType.ROOF_FLOOR, 0f, 8f, 8f, 14f)
                floor(BiomeType.ROOF_FLOOR, 10f, 7f, 18f, 14f)
                wall(LineFeatureType.LEDGE, 0f, 6f, 7f, 6f, 7f, 0f)
                wall(LineFeatureType.LEDGE, 9f, 0f, 9f, 5f, 18f, 5f)
                wall(LineFeatureType.LEDGE, 0f, 8f, 8f, 8f, 8f, 14f)
                wall(LineFeatureType.LEDGE, 18f, 7f, 10f, 7f, 10f, 14f)
                put(MarkerType.B_ROPE_BRIDGE, 8f, 3f)
                put(MarkerType.B_ROPE_BRIDGE, 9f, 11f)
                row(MarkerType.B_FIREPLACE, 3f, 3f, 13f)
                put(MarkerType.B_TRAPDOOR, 5f, 11f)
                put(MarkerType.B_TRAPDOOR, 14f, 10f)
                put(MarkerType.B_LADDER, 16f, 12.5f)
                put(MarkerType.B_START_HEROES, 2f, 2f)
                put(MarkerType.B_EXIT, 16f, 2f)
            }
            RoomTemplate.CIRCUS -> build(18, 18) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 18f, 18f)
                floorCircle(BiomeType.STRAW_FLOOR, 9f, 9f, 7.5f)
                floorCircle(BiomeType.SAND_GROUND, 9f, 9f, 3f, 16)
                ring(LineFeatureType.CURTAIN_WALL, 9f, 9f, 7.5f, 20, gap(9f, 16.5f, null, 2f), gap(9f, 1.5f, null, 1.5f))
                circleOf(MarkerType.B_BENCH, 9f, 9f, 5f, 10)
                put(MarkerType.B_PILLAR, 9f, 9f)
                row(MarkerType.B_CAGE, 0.8f, 7f, 11f)
                row(MarkerType.B_CART, 17f, 3f, 15f)
                row(MarkerType.B_TENT, 3f, 2f, 16f)
                put(MarkerType.B_BARREL, 1.5f, 15f)
            }
            RoomTemplate.CARAVAN_CAMP -> build(18, 14) {
                floor(BiomeType.SAND_GROUND, 0f, 0f, 18f, 14f)
                floorCircle(BiomeType.DIRT_GROUND, 9f, 7f, 5f, 16)
                circleOf(MarkerType.B_CART, 9f, 7f, 5f, 6)
                put(MarkerType.B_CAMPFIRE, 9f, 7f)
                row(MarkerType.B_TENT, 7f, 7f, 11f)
                row(MarkerType.B_SACKS, 10f, 8f, 10f)
                row(MarkerType.B_BARREL, 4f, 8f, 10f)
                wall(LineFeatureType.ROPE, 2f, 12.5f, 6f, 12.5f)
                put(MarkerType.B_HAY, 4f, 12f)
                scatter(MarkerType.B_ROCKS, 3, 13f, 10f, 17f, 13f)
                put(MarkerType.B_BUSH, 1.5f, 1.5f)
            }
            RoomTemplate.ROAD_AMBUSH -> build(20, 14) {
                floor(BiomeType.FOREST_GROUND, 0f, 0f, 20f, 14f)
                floorShape(BiomeType.DIRT_GROUND, 0f, 6f, 20f, 5f, 20f, 8f, 0f, 9f)
                wall(LineFeatureType.HEDGE, 0f, 4f, 7f, 3.5f)
                wall(LineFeatureType.HEDGE, 12f, 10.5f, 20f, 10f)
                scatter(MarkerType.B_TREE, 7, 1f, 0.5f, 19f, 3.5f)
                scatter(MarkerType.B_TREE, 6, 1f, 10.5f, 19f, 13.5f)
                put(MarkerType.B_FALLEN_TREE, 14f, 6.8f)
                put(MarkerType.B_CART, 6f, 7f)
                row(MarkerType.B_AMBUSH, 3f, 10f, 16f)
                row(MarkerType.B_AMBUSH, 11.5f, 5f)
                put(MarkerType.B_START_ENEMIES, 16f, 3f)
                put(MarkerType.B_START_HEROES, 3f, 7f)
                put(MarkerType.B_BOULDER, 9f, 11f)
            }
            RoomTemplate.CROSSROADS -> build(16, 16) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 16f, 16f)
                floor(BiomeType.DIRT_GROUND, 7f, 0f, 9f, 16f)
                floor(BiomeType.DIRT_GROUND, 0f, 7f, 16f, 9f)
                wall(LineFeatureType.FENCE, 1f, 3f, 5f, 3f, 5f, 6f)
                wall(LineFeatureType.FENCE, 11f, 13f, 11f, 10f, 15f, 10f)
                put(MarkerType.B_STOCKS, 10.5f, 5.5f)
                put(MarkerType.B_DEAD_TREE, 11.5f, 5f)
                put(MarkerType.B_STATUE, 6f, 6f)
                put(MarkerType.B_WELL, 5.5f, 10.5f)
                row(MarkerType.B_TREE, 1.5f, 13f, 14.5f)
                put(MarkerType.B_TREE, 1.5f, 14.5f)
                put(MarkerType.B_BUSH, 14f, 1.5f)
                put(MarkerType.B_GM_NOTE, 8f, 8f)
            }
            RoomTemplate.RIVER_FORD -> build(18, 14) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 18f, 14f)
                floorShape(BiomeType.DEEP_WATER, 6f, 0f, 11f, 0f, 12f, 14f, 7f, 14f)
                floorShape(BiomeType.SHALLOW_WATER, 6.5f, 5f, 11.5f, 5f, 11.5f, 9f, 6.5f, 9f)
                floor(BiomeType.DIRT_GROUND, 0f, 6f, 6.5f, 8f)
                floor(BiomeType.DIRT_GROUND, 11.5f, 6f, 18f, 8f)
                wall(LineFeatureType.HEDGE, 0f, 2f, 5f, 2.5f)
                wall(LineFeatureType.HEDGE, 13f, 12f, 18f, 11.5f)
                scatter(MarkerType.B_ROCKS, 4, 7f, 5.5f, 11f, 8.5f)
                scatter(MarkerType.B_REEDS, 4, 5f, 1f, 12.5f, 13f)
                row(MarkerType.B_TREE, 11f, 2f, 15.5f)
                put(MarkerType.B_BIG_TREE, 15f, 2f)
                put(MarkerType.B_BOULDER, 3f, 11f)
            }
            RoomTemplate.WATERFALL -> build(16, 16) {
                floor(BiomeType.ROCKY_GROUND, 0f, 0f, 16f, 16f)
                floor(BiomeType.ROCK_GROUND, 0f, 0f, 16f, 4f)
                blobFloor(BiomeType.DEEP_WATER, 8f, 9f, 5f, 4f)
                floorShape(BiomeType.SHALLOW_WATER, 7f, 4f, 9f, 4f, 9.5f, 6f, 6.5f, 6f)
                wall(LineFeatureType.LEDGE, 0f, 4f, 6.5f, 4f)
                wall(LineFeatureType.LEDGE, 9.5f, 4f, 16f, 4f)
                cave(BiomeType.CAVE_FLOOR, LineFeatureType.CAVE_WALL, 8f, 1.8f, 3f, 1.6f, 90f)
                put(MarkerType.B_TREASURE, 8f, 1.5f)
                scatter(MarkerType.B_ROCKS, 3, 1f, 5f, 3f, 15f)
                scatter(MarkerType.B_TREE, 3, 13f, 5f, 15f, 15f)
                put(MarkerType.B_REEDS, 12f, 12f)
                put(MarkerType.B_LOG, 4f, 14f)
            }
            RoomTemplate.SMUGGLERS_COVE -> build(20, 14) {
                floor(BiomeType.DEEP_WATER, 0f, 0f, 20f, 14f)
                floorShape(BiomeType.SAND_GROUND, 0f, 0f, 20f, 0f, 20f, 6f, 14f, 8f, 8f, 7f, 0f, 9f)
                cave(BiomeType.CAVE_FLOOR, LineFeatureType.CAVE_WALL, 4f, 3f, 3.5f, 2.5f, 60f)
                floor(BiomeType.OLD_WOOD_FLOOR, 12f, 6f, 14f, 12f)
                put(MarkerType.B_ROPE_BRIDGE, 13f, 11f)
                scatter(MarkerType.B_CRATE, 3, 2f, 2f, 6f, 4f)
                put(MarkerType.B_TREASURE, 4f, 2.5f)
                row(MarkerType.B_BARREL, 4f, 10f, 11f)
                put(MarkerType.B_CAMPFIRE, 16f, 3f)
                put(MarkerType.B_TENT, 18f, 1.5f)
                put(MarkerType.B_ROCKS, 9f, 10f)
                put(MarkerType.B_AMBUSH, 12f, 2f)
            }
            RoomTemplate.SWAMP -> build(18, 14) {
                floor(BiomeType.MUD_GROUND, 0f, 0f, 18f, 14f)
                blobFloor(BiomeType.QUAGMIRE, 5f, 5f, 4f, 3f)
                blobFloor(BiomeType.SHALLOW_WATER, 13f, 9f, 4f, 3.5f)
                blobFloor(BiomeType.MOSS_GROUND, 9f, 3f, 2.5f, 2f)
                floor(BiomeType.OLD_WOOD_FLOOR, 8f, 6f, 9f, 13f)
                wall(LineFeatureType.HEDGE, 0f, 12f, 5f, 13f)
                scatter(MarkerType.B_DEAD_TREE, 4, 1f, 1f, 17f, 13f)
                scatter(MarkerType.B_REEDS, 6, 1f, 1f, 17f, 13f)
                put(MarkerType.B_MUSHROOMS, 9f, 3f)
                put(MarkerType.B_BONES, 5f, 5f)
                put(MarkerType.B_GAS_TRAP, 13f, 9f)
                put(MarkerType.B_LOG, 3f, 10f)
            }
            RoomTemplate.WITCH_HUT -> build(14, 12) {
                floor(BiomeType.MUD_GROUND, 0f, 0f, 14f, 12f)
                blobFloor(BiomeType.SHALLOW_WATER, 11f, 9f, 2.5f, 2f)
                house(LineFeatureType.TIMBER_WALL, BiomeType.OLD_WOOD_FLOOR, 3f, 2f, 9f, 7f, 'S')
                put(MarkerType.B_CAULDRON, 6f, 4.5f)
                put(MarkerType.B_SHELF, 3.8f, 2.8f)
                put(MarkerType.B_BED, 8f, 3f)
                put(MarkerType.B_CAGE, 4f, 6f)
                put(MarkerType.B_TOTEM, 6f, 9f)
                scatter(MarkerType.B_MUSHROOMS, 3, 1f, 8f, 5f, 11f)
                row(MarkerType.B_DEAD_TREE, 1f, 1f, 12f)
                put(MarkerType.B_BONES, 10f, 5f)
                put(MarkerType.B_POTION, 5f, 3f)
            }
            RoomTemplate.DRUID_GROVE -> build(16, 16) {
                floor(BiomeType.FOREST_GROUND, 0f, 0f, 16f, 16f)
                floorCircle(BiomeType.MOSS_GROUND, 8f, 8f, 5f)
                floorCircle(BiomeType.FEY_RING, 8f, 8f, 2f, 16)
                wall(LineFeatureType.HEDGE, 1f, 1f, 5f, 0.8f)
                wall(LineFeatureType.HEDGE, 11f, 15.2f, 15f, 15f)
                put(MarkerType.B_BIG_TREE, 8f, 8f)
                circleOf(MarkerType.B_STANDING_STONES, 8f, 8f, 4f, 6)
                scatter(MarkerType.B_TREE, 8, 0.5f, 0.5f, 15.5f, 3f)
                scatter(MarkerType.B_TREE, 6, 0.5f, 13f, 15.5f, 15.5f)
                put(MarkerType.B_ALTAR, 8f, 10.5f)
                put(MarkerType.B_FLOWERS, 6f, 6f)
                put(MarkerType.B_TOTEM, 10f, 6f)
            }
            RoomTemplate.STONE_CIRCLE -> build(14, 14) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 14f, 14f)
                floorCircle(BiomeType.MAGIC_CIRCLE, 7f, 7f, 2.5f, 16)
                wall(LineFeatureType.RUBBLE_LINE, 1f, 12f, 4f, 13f)
                circleOf(MarkerType.B_STANDING_STONES, 7f, 7f, 5f, 8)
                put(MarkerType.B_ALTAR, 7f, 7f)
                put(MarkerType.B_SPELL_MARK, 7f, 5f)
                put(MarkerType.B_RUNE_TRAP, 7f, 9f)
                row(MarkerType.B_TREE, 1f, 1f, 13f)
                put(MarkerType.B_BUSH, 12.5f, 12.5f)
            }
            RoomTemplate.MOUNTAIN_PASS -> build(20, 12) {
                floor(BiomeType.ROCK_GROUND, 0f, 0f, 20f, 12f)
                floorShape(BiomeType.GRAVEL_GROUND, 0f, 4f, 7f, 5f, 13f, 4f, 20f, 5f, 20f, 8f, 13f, 7.5f, 7f, 8.5f, 0f, 8f)
                wall(LineFeatureType.LEDGE, 0f, 4f, 7f, 5f, 13f, 4f, 20f, 5f)
                wall(LineFeatureType.LEDGE, 0f, 8f, 7f, 8.5f, 13f, 7.5f, 20f, 8f)
                scatter(MarkerType.B_BOULDER, 4, 1f, 0.5f, 19f, 3f)
                scatter(MarkerType.B_ROCKS, 4, 1f, 9f, 19f, 11.5f)
                put(MarkerType.B_ROCKFALL, 10f, 5.5f)
                put(MarkerType.B_AMBUSH, 12f, 2f)
                put(MarkerType.B_ROPE_BRIDGE, 16f, 6.2f)
                put(MarkerType.B_CAMPFIRE, 3f, 6.5f)
            }
            RoomTemplate.CLIFFSIDE -> build(18, 14) {
                floor(BiomeType.DEEP_WATER, 0f, 0f, 18f, 14f)
                floorShape(BiomeType.GRASS_GROUND, 0f, 0f, 18f, 0f, 18f, 5f, 12f, 7f, 7f, 6f, 3f, 9f, 0f, 8f)
                floorShape(BiomeType.ROCKY_GROUND, 0f, 8f, 3f, 9f, 7f, 6f, 12f, 7f, 18f, 5f, 18f, 7f, 12f, 9f, 7f, 8f, 3f, 11f, 0f, 10f)
                wall(LineFeatureType.LEDGE, 0f, 8f, 3f, 9f, 7f, 6f, 12f, 7f, 18f, 5f)
                put(MarkerType.B_ROPE_BRIDGE, 5f, 8f)
                put(MarkerType.B_STATUE, 9f, 3f)
                scatter(MarkerType.B_TREE, 3, 1f, 1f, 6f, 4f)
                put(MarkerType.B_BELL, 14f, 2f)
                put(MarkerType.B_NEST, 11f, 6f)
                put(MarkerType.B_ROCKS, 15f, 9f)
            }
            RoomTemplate.OASIS -> build(18, 14) {
                floor(BiomeType.SAND_GROUND, 0f, 0f, 18f, 14f)
                floorCircle(BiomeType.GRASS_GROUND, 9f, 7f, 5f, 18)
                blobFloor(BiomeType.SHALLOW_WATER, 9f, 7f, 3f, 2.2f)
                wall(LineFeatureType.RUBBLE_LINE, 1f, 12.5f, 5f, 13f)
                circleOf(MarkerType.B_TREE, 9f, 7f, 4.2f, 7)
                put(MarkerType.B_TENT, 15f, 3f)
                put(MarkerType.B_CAMPFIRE, 15f, 5f)
                put(MarkerType.B_WELL, 3f, 3f)
                scatter(MarkerType.B_ROCKS, 3, 1f, 9f, 6f, 13f)
                put(MarkerType.B_BONES, 16f, 12f)
            }
            RoomTemplate.BATTLEFIELD -> build(20, 16) {
                floor(BiomeType.MUD_GROUND, 0f, 0f, 20f, 16f)
                blobFloor(BiomeType.ASH_GROUND, 6f, 6f, 4f, 3f)
                blobFloor(BiomeType.BLOOD_POOL, 12f, 9f, 2f, 1.5f)
                wall(LineFeatureType.STAKE_WALL, 2f, 13f, 8f, 13f)
                wall(LineFeatureType.RUBBLE_LINE, 14f, 3f, 18f, 5f)
                scatter(MarkerType.B_BONES, 5, 1f, 1f, 19f, 15f)
                scatter(MarkerType.B_WEAPON, 4, 1f, 1f, 19f, 15f)
                put(MarkerType.B_BANNER, 10f, 4f)
                put(MarkerType.B_CART, 16f, 12f)
                put(MarkerType.B_DEAD_TREE, 3f, 3f)
                put(MarkerType.B_CAMPFIRE, 17f, 1.5f)
            }
            RoomTemplate.SIEGE_CAMP -> build(20, 16) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 20f, 16f)
                floor(BiomeType.DIRT_GROUND, 2f, 2f, 18f, 14f)
                room(LineFeatureType.STAKE_WALL, 2f, 2f, 18f, 14f, gap(18f, 8f, MarkerType.B_BARRED_GATE, 2f))
                for (x in floatArrayOf(5f, 9f, 13f)) { put(MarkerType.B_TENT, x, 4.5f); put(MarkerType.B_TENT, x, 11.5f) }
                put(MarkerType.B_MAP_TABLE, 9f, 8f)
                row(MarkerType.B_BANNER, 8f, 7f, 11f)
                row(MarkerType.B_WEAPON_RACK, 8f, 4f, 15f)
                row(MarkerType.B_CART, 1f, 1f, 19f)
                put(MarkerType.B_CAMPFIRE, 15.5f, 5f)
                put(MarkerType.B_HAY, 15.5f, 12f)
            }
            RoomTemplate.ABANDONED_VILLAGE -> build(20, 16) {
                floor(BiomeType.DRY_EARTH, 0f, 0f, 20f, 16f)
                floor(BiomeType.DIRT_GROUND, 0f, 7f, 20f, 9f)
                floor(BiomeType.RUBBLE_FLOOR, 1f, 1f, 6f, 5f)
                floor(BiomeType.ASH_GROUND, 12f, 1f, 18f, 5f)
                wall(LineFeatureType.RUBBLE_LINE, 1f, 1f, 6f, 1f, 6f, 3f)
                wall(LineFeatureType.TIMBER_WALL, 12f, 1f, 18f, 1f, 18f, 5f)
                wall(LineFeatureType.RUBBLE_LINE, 12f, 5f, 15f, 5f)
                house(LineFeatureType.TIMBER_WALL, BiomeType.OLD_WOOD_FLOOR, 3f, 11f, 8f, 15f, 'N')
                wall(LineFeatureType.FENCE, 11f, 11f, 18f, 11f, 18f, 15f)
                put(MarkerType.B_WELL, 10f, 12f)
                scatter(MarkerType.B_BONES, 3, 1f, 9.5f, 19f, 15f)
                put(MarkerType.B_DEAD_TREE, 9f, 3f)
                put(MarkerType.B_CART, 15f, 8f)
                put(MarkerType.B_ROCKS, 3f, 3f)
                put(MarkerType.B_NEST, 15f, 3f)
            }
            RoomTemplate.RUINED_CASTLE -> build(20, 20) {
                floor(BiomeType.GRASS_GROUND, 0f, 0f, 20f, 20f)
                floor(BiomeType.RUBBLE_FLOOR, 2f, 2f, 18f, 18f)
                floor(BiomeType.CASTLE_FLOOR, 6f, 5f, 14f, 11f)
                wall(LineFeatureType.DUNGEON_WALL, 2f, 8f, 2f, 2f, 10f, 2f)
                wall(LineFeatureType.RUBBLE_LINE, 10f, 2f, 14f, 2f)
                wall(LineFeatureType.DUNGEON_WALL, 14f, 2f, 18f, 2f, 18f, 12f)
                wall(LineFeatureType.RUBBLE_LINE, 18f, 12f, 18f, 18f, 12f, 18f)
                wall(LineFeatureType.DUNGEON_WALL, 8f, 18f, 2f, 18f, 2f, 12f)
                wall(LineFeatureType.DUNGEON_WALL, 6f, 5f, 14f, 5f, 14f, 8f)
                wall(LineFeatureType.RUBBLE_LINE, 6f, 5f, 6f, 11f, 10f, 11f)
                put(MarkerType.B_THRONE, 10f, 6f)
                scatter(MarkerType.B_BROKEN_PILLAR, 4, 3f, 12f, 17f, 17f)
                put(MarkerType.B_SPIRAL_STAIRS, 3.5f, 3.5f)
                put(MarkerType.B_TREE, 16f, 16f)
                put(MarkerType.B_BUSH, 4f, 15f)
                put(MarkerType.B_TREASURE, 13f, 7f)
                put(MarkerType.B_NEST, 16.5f, 4f)
            }
            RoomTemplate.SHIPWRECK -> build(20, 14) {
                floor(BiomeType.SAND_GROUND, 0f, 0f, 20f, 14f)
                floorShape(BiomeType.SHALLOW_WATER, 0f, 10f, 20f, 9f, 20f, 14f, 0f, 14f)
                floorShape(BiomeType.OLD_WOOD_FLOOR, 5f, 3f, 14f, 2f, 17f, 5f, 14f, 8f, 5f, 7f)
                wall(LineFeatureType.TIMBER_WALL, 5f, 3f, 14f, 2f, 17f, 5f)
                wall(LineFeatureType.RUBBLE_LINE, 14f, 8f, 5f, 7f)
                put(MarkerType.B_BROKEN_PILLAR, 10f, 5f)
                put(MarkerType.B_TREASURE, 7f, 5f)
                scatter(MarkerType.B_CRATE, 3, 2f, 8f, 18f, 11f)
                put(MarkerType.B_BARREL, 3f, 2f)
                put(MarkerType.B_BONES, 12f, 5f)
                put(MarkerType.B_NEST, 16f, 4.5f)
                put(MarkerType.B_ROCKS, 1f, 12f)
            }
            RoomTemplate.FEY_GLADE -> build(14, 14) {
                floor(BiomeType.FOREST_GROUND, 0f, 0f, 14f, 14f)
                floorCircle(BiomeType.GRASS_GROUND, 7f, 7f, 5f)
                floorCircle(BiomeType.FEY_RING, 7f, 7f, 2.5f, 18)
                wall(LineFeatureType.HEDGE, 0.5f, 5f, 0.5f, 9f)
                circleOf(MarkerType.B_MUSHROOMS, 7f, 7f, 2.5f, 8)
                put(MarkerType.B_FOUNTAIN, 7f, 7f)
                scatter(MarkerType.B_FLOWERS, 5, 3f, 3f, 11f, 11f)
                circleOf(MarkerType.B_TREE, 7f, 7f, 6f, 10)
                put(MarkerType.B_GIANT_MUSHROOM, 10f, 4f)
                put(MarkerType.B_SPELL_MARK, 7f, 9f)
            }
            RoomTemplate.PYRAMID_TOMB -> build(16, 16) {
                floor(BiomeType.SAND_GROUND, 0f, 0f, 16f, 16f)
                floor(BiomeType.TILE_FLOOR, 2f, 2f, 14f, 14f)
                floor(BiomeType.GRAVEL_GROUND, 7f, 10f, 9f, 16f)
                room(LineFeatureType.DUNGEON_WALL, 2f, 2f, 14f, 14f, gap(8f, 14f, MarkerType.B_SECRET_DOOR))
                room(LineFeatureType.DUNGEON_WALL, 5f, 3f, 11f, 8f, gap(8f, 8f, MarkerType.B_LOCKED_DOOR))
                put(MarkerType.B_SARCOPHAGUS, 8f, 5f)
                row(MarkerType.B_STATUE, 3.5f, 5.8f, 10.2f)
                row(MarkerType.B_TREASURE, 7f, 6f, 10f)
                for (y in floatArrayOf(9.5f, 12f)) row(MarkerType.B_PILLAR, y, 4f, 12f)
                row(MarkerType.B_PRESSURE_PLATE, 11f, 7f, 9f)
                put(MarkerType.B_ARROW_TRAP, 2.8f, 11f)
                put(MarkerType.B_PIT_TRAP, 8f, 12.5f)
                put(MarkerType.B_ARTIFACT, 8f, 4f)
            }
            RoomTemplate.ICE_CAVE -> build(16, 12) {
                floor(BiomeType.SNOW_GROUND, 0f, 0f, 16f, 12f)
                cave(BiomeType.ICE_FLOOR, LineFeatureType.CAVE_WALL, 8f, 6f, 7.5f, 5.5f, 180f)
                blobFloor(BiomeType.DEEP_WATER, 11f, 7f, 2f, 1.5f)
                scatter(MarkerType.B_CRYSTAL, 4, 4f, 2f, 13f, 10f)
                row(MarkerType.B_STALAGMITE, 3f, 6f, 10f)
                put(MarkerType.B_BONES, 5f, 8f)
                put(MarkerType.B_NEST, 12f, 3f)
                put(MarkerType.B_TREASURE, 13.5f, 6f)
            }
            RoomTemplate.VOLCANO_FORGE -> build(18, 14) {
                floor(BiomeType.ROCK_GROUND, 0f, 0f, 18f, 14f)
                blobFloor(BiomeType.ASH_GROUND, 9f, 7f, 8.5f, 6.5f)
                blobFloor(BiomeType.LAVA_POOL, 10f, 7f, 4f, 3f)
                wall(LineFeatureType.LAVA_STREAM, 14f, 7f, 17f, 10f, 18f, 13f)
                wall(LineFeatureType.LEDGE, 0f, 3f, 5f, 2f, 9f, 0.5f)
                put(MarkerType.B_FORGE, 3f, 8f)
                put(MarkerType.B_ANVIL, 4.5f, 9.5f)
                put(MarkerType.B_ROPE_BRIDGE, 10f, 7f)
                put(MarkerType.B_ARTIFACT, 15f, 3f)
                scatter(MarkerType.B_ROCKS, 3, 1f, 10f, 8f, 13.5f)
                put(MarkerType.B_FIRE_TRAP, 7f, 11f)
                put(MarkerType.B_START_ENEMIES, 15f, 5f)
            }
            RoomTemplate.UNDERDARK -> build(18, 14) {
                floor(BiomeType.CAVE_FLOOR, 0f, 0f, 18f, 14f)
                blobFloor(BiomeType.MOSS_GROUND, 6f, 6f, 5f, 4f)
                blobFloor(BiomeType.DEEP_WATER, 14f, 9f, 3.5f, 3f)
                wall(LineFeatureType.CAVE_WALL, 0f, 1f, 6f, 0.5f, 12f, 1f, 18f, 0.5f)
                wall(LineFeatureType.CAVE_WALL, 0f, 13f, 8f, 13.5f)
                scatter(MarkerType.B_GIANT_MUSHROOM, 5, 2f, 2f, 10f, 11f)
                scatter(MarkerType.B_MUSHROOMS, 5, 1f, 2f, 12f, 12f)
                scatter(MarkerType.B_STALAGMITE, 3, 11f, 2f, 17f, 5f)
                put(MarkerType.B_CRYSTAL, 16f, 13f)
                put(MarkerType.B_WEB, 11f, 12f)
                put(MarkerType.B_CAMPFIRE, 7f, 7f)
            }
            RoomTemplate.GOBLIN_CAVE -> build(18, 14) {
                floor(BiomeType.ROCKY_GROUND, 0f, 0f, 18f, 14f)
                cave(BiomeType.CAVE_FLOOR, LineFeatureType.CAVE_WALL, 6f, 7f, 5.5f, 5f, 180f, 0f)
                cave(BiomeType.DIRT_GROUND, LineFeatureType.CAVE_WALL, 14f, 5f, 3.5f, 4f, 180f)
                floor(BiomeType.DIRT_GROUND, 11f, 5f, 11.5f, 7f)
                put(MarkerType.B_CAMPFIRE, 6f, 7f)
                scatter(MarkerType.B_BONES, 3, 3f, 4f, 9f, 10f)
                put(MarkerType.B_CAGE, 4f, 4f)
                put(MarkerType.B_TREASURE, 15f, 3f)
                put(MarkerType.B_THRONE, 15.5f, 6f)
                put(MarkerType.B_NET_TRAP, 1.5f, 7f)
                put(MarkerType.B_ALARM, 2.5f, 9f)
                put(MarkerType.B_START_ENEMIES, 7f, 5f)
            }
            RoomTemplate.ORC_CAMP -> build(18, 16) {
                floor(BiomeType.DRY_EARTH, 0f, 0f, 18f, 16f)
                floorCircle(BiomeType.DIRT_GROUND, 9f, 8f, 6.5f, 18)
                ring(LineFeatureType.STAKE_WALL, 9f, 8f, 7.5f, 14, gap(9f, 15.5f, null, 2f))
                put(MarkerType.B_CAMPFIRE, 9f, 8f)
                circleOf(MarkerType.B_TENT, 9f, 8f, 4.5f, 5)
                put(MarkerType.B_TOTEM, 9f, 5f)
                put(MarkerType.B_CAGE, 5f, 11f)
                put(MarkerType.B_WEAPON_RACK, 13f, 11f)
                scatter(MarkerType.B_BONES, 3, 6f, 6f, 12f, 11f)
                put(MarkerType.B_BANNER, 9f, 1.2f)
                put(MarkerType.B_START_ENEMIES, 9f, 10f)
            }
            RoomTemplate.BANDIT_HIDEOUT -> build(16, 12) {
                floor(BiomeType.FOREST_GROUND, 0f, 0f, 16f, 12f)
                cave(BiomeType.CAVE_FLOOR, LineFeatureType.CAVE_WALL, 11f, 6f, 4.5f, 5f, 180f)
                house(LineFeatureType.TIMBER_WALL, BiomeType.OLD_WOOD_FLOOR, 1f, 1f, 5f, 5f, 'S')
                put(MarkerType.B_TREASURE, 13f, 4f)
                row(MarkerType.B_BUNKS, 8f, 11f, 13f)
                put(MarkerType.B_TABLE, 11f, 6f)
                put(MarkerType.B_MAP_TABLE, 3f, 3f)
                put(MarkerType.B_CAMPFIRE, 4f, 8f)
                scatter(MarkerType.B_TREE, 3, 0.5f, 9f, 5f, 11.5f)
                put(MarkerType.B_TRIPWIRE, 6.5f, 6f)
                put(MarkerType.B_AMBUSH, 7f, 2f)
            }
            RoomTemplate.SPIDER_LAIR -> build(16, 14) {
                floor(BiomeType.ROCKY_GROUND, 0f, 0f, 16f, 14f)
                cave(BiomeType.CAVE_FLOOR, LineFeatureType.CAVE_WALL, 8f, 7f, 7.5f, 6.5f, 180f)
                blobFloor(BiomeType.WEB_FIELD, 9f, 6f, 4f, 3.5f)
                scatter(MarkerType.B_WEB, 6, 3f, 2f, 13f, 12f)
                row(MarkerType.B_NEST, 6f, 8f, 11f)
                scatter(MarkerType.B_BONES, 3, 3f, 3f, 12f, 11f)
                put(MarkerType.B_TREASURE, 12f, 10f)
                put(MarkerType.B_START_ENEMIES, 10f, 4f)
            }
            RoomTemplate.HAUNTED_HOUSE -> build(14, 12) {
                floor(BiomeType.OLD_WOOD_FLOOR, 0f, 0f, 14f, 12f)
                room(LineFeatureType.TIMBER_WALL, 0f, 0f, 14f, 12f, gap(7f, 12f, MarkerType.B_DOUBLE_DOOR, 2f))
                room(LineFeatureType.TIMBER_WALL, 0f, 0f, 6f, 5f, gap(6f, 3f))
                room(LineFeatureType.TIMBER_WALL, 8f, 0f, 14f, 5f, gap(8f, 3f, MarkerType.B_LOCKED_DOOR))
                put(MarkerType.B_STAIRS_UP, 7f, 1.5f)
                put(MarkerType.B_FIREPLACE, 1f, 8f)
                put(MarkerType.B_CHANDELIER, 7f, 8f)
                put(MarkerType.B_COFFIN, 3f, 2f)
                put(MarkerType.B_BED, 11f, 2f)
                put(MarkerType.B_WEB, 13f, 11f)
                put(MarkerType.B_DARKNESS, 11f, 8f)
                put(MarkerType.B_BOOKSHELF, 13.2f, 8f)
                put(MarkerType.B_SECRET_DOOR, 0f, 3f)
            }
            RoomTemplate.SHIP_HOLD -> build(8, 18) {
                val hull = floatArrayOf(4f, 0.5f, 7f, 3.5f, 7.5f, 9f, 7f, 16f, 5.5f, 17.5f, 2.5f, 17.5f, 1f, 16f, 0.5f, 9f, 1f, 3.5f)
                floor(BiomeType.DEEP_WATER, 0f, 0f, 8f, 18f)
                floorShape(BiomeType.OLD_WOOD_FLOOR, *hull)
                shape(LineFeatureType.TIMBER_WALL, hull)
                put(MarkerType.B_LADDER, 4f, 9f)
                for (y in floatArrayOf(4f, 6f, 12f, 14f)) row(MarkerType.B_BARREL, y, 2.5f, 5.5f)
                row(MarkerType.B_CRATE_STACK, 16f, 3f, 5f)
                put(MarkerType.B_CAGE, 4f, 2.5f)
                put(MarkerType.B_SACKS, 2.5f, 10f)
                put(MarkerType.B_TREASURE, 5.5f, 10f)
            }
            RoomTemplate.AIRSHIP -> build(10, 20) {
                val deck = floatArrayOf(5f, 0.5f, 8f, 3f, 9f, 9f, 8.5f, 17f, 6f, 19.5f, 4f, 19.5f, 1.5f, 17f, 1f, 9f, 2f, 3f)
                floorShape(BiomeType.WOOD_FLOOR, *deck)
                shape(LineFeatureType.FENCE, deck)
                floor(BiomeType.METAL_FLOOR, 3f, 14f, 7f, 18f)
                room(LineFeatureType.BRICK_WALL, 3f, 14f, 7f, 18f, gap(5f, 14f))
                put(MarkerType.B_MAP_TABLE, 5f, 16f)
                row(MarkerType.B_PILLAR, 6f, 3f, 7f)
                row(MarkerType.B_PILLAR, 11f, 3f, 7f)
                put(MarkerType.B_CRATE_STACK, 5f, 3f)
                row(MarkerType.B_BARREL, 9f, 2.5f, 7.5f)
                put(MarkerType.B_TRAPDOOR, 5f, 12f)
                put(MarkerType.B_ROPE_BRIDGE, 9f, 9f)
            }
            RoomTemplate.PORTAL_CHAMBER -> build(14, 14) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 14f, 14f)
                floorCircle(BiomeType.VOID_RIFT, 7f, 6f, 2f, 16)
                floorCircle(BiomeType.MAGIC_CIRCLE, 7f, 6f, 3.5f, 20)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 14f, 14f, gap(7f, 14f, MarkerType.B_DOUBLE_DOOR, 2f))
                put(MarkerType.B_PORTAL, 7f, 6f)
                circleOf(MarkerType.B_PILLAR, 7f, 6f, 4.5f, 6)
                row(MarkerType.B_LEVER, 12f, 2f, 12f)
                put(MarkerType.B_CRYSTAL, 1.5f, 1.5f)
                put(MarkerType.B_CRYSTAL, 12.5f, 1.5f)
                put(MarkerType.B_BOOK, 7f, 11f)
            }
            RoomTemplate.MAZE -> build(16, 16) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 16f, 16f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 16f, 16f, gap(1f, 16f, null), gap(15f, 0f, null))
                wall(LineFeatureType.DUNGEON_WALL, 2f, 14f, 2f, 2f, 6f, 2f)
                wall(LineFeatureType.DUNGEON_WALL, 8f, 0f, 8f, 4f, 4f, 4f, 4f, 12f)
                wall(LineFeatureType.DUNGEON_WALL, 2f, 14f, 10f, 14f, 10f, 10f)
                wall(LineFeatureType.DUNGEON_WALL, 6f, 6f, 6f, 12f, 8f, 12f)
                wall(LineFeatureType.DUNGEON_WALL, 8f, 6f, 12f, 6f, 12f, 12f)
                wall(LineFeatureType.DUNGEON_WALL, 10f, 2f, 14f, 2f, 14f, 14f, 12f, 14f)
                wall(LineFeatureType.DUNGEON_WALL, 10f, 4f, 12f, 4f)
                put(MarkerType.B_START_HEROES, 1f, 15f)
                put(MarkerType.B_EXIT, 15f, 1f)
                put(MarkerType.B_PIT_TRAP, 3f, 8f)
                put(MarkerType.B_TREASURE, 9f, 9f)
                put(MarkerType.B_SPIKE_TRAP, 13f, 8f)
                put(MarkerType.B_BONES, 7f, 3f)
            }
            RoomTemplate.PUZZLE_ROOM -> build(14, 14) {
                floor(BiomeType.TILE_FLOOR, 0f, 0f, 14f, 14f)
                floor(BiomeType.PIT_ABYSS, 3f, 5f, 11f, 9f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 14f, 14f, gap(7f, 14f), gap(7f, 0f, MarkerType.B_IRON_DOOR))
                for (x in floatArrayOf(4f, 6f, 8f, 10f)) put(MarkerType.B_PRESSURE_PLATE, x, 11f)
                row(MarkerType.B_STATUE, 3f, 2f, 12f)
                row(MarkerType.B_LEVER, 1f, 5f, 9f)
                put(MarkerType.B_ROPE_BRIDGE, 7f, 7f)
                row(MarkerType.B_RUNE_TRAP, 3f, 5f, 9f)
                put(MarkerType.B_GM_NOTE, 12.5f, 12.5f)
            }
            RoomTemplate.BOSS_ROOM -> build(18, 16) {
                floor(BiomeType.CASTLE_FLOOR, 0f, 0f, 18f, 16f)
                floor(BiomeType.CARPET, 8f, 3f, 10f, 16f)
                floor(BiomeType.LAVA_POOL, 0.5f, 0.5f, 3f, 15.5f)
                floor(BiomeType.LAVA_POOL, 15f, 0.5f, 17.5f, 15.5f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 18f, 16f, gap(9f, 16f, MarkerType.B_DOUBLE_DOOR, 2f))
                put(MarkerType.B_THRONE, 9f, 2f)
                row(MarkerType.B_BRAZIER, 2f, 6f, 12f)
                for (y in floatArrayOf(5f, 8f, 11f, 14f)) row(MarkerType.B_PILLAR, y, 5f, 13f)
                row(MarkerType.B_BANNER, 0.8f, 6f, 12f)
                put(MarkerType.B_SUMMON_CIRCLE, 9f, 6f)
                put(MarkerType.B_TREASURE, 12f, 2f)
                put(MarkerType.B_START_ENEMIES, 9f, 3.5f)
                put(MarkerType.B_START_HEROES, 9f, 14.5f)
            }
            RoomTemplate.GIANT_HALL -> build(20, 16) {
                floor(BiomeType.OLD_WOOD_FLOOR, 0f, 0f, 20f, 16f)
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 20f, 3f)
                room(LineFeatureType.TIMBER_WALL, 0f, 0f, 20f, 16f, gap(10f, 16f, MarkerType.B_DOUBLE_DOOR, 4f))
                put(MarkerType.B_THRONE, 10f, 1.5f)
                put(MarkerType.B_LONG_TABLE, 10f, 8f)
                put(MarkerType.B_FIREPLACE, 1f, 8f)
                row(MarkerType.B_BARREL, 14f, 16f, 18f)
                put(MarkerType.B_GIANT_MUSHROOM, 18f, 3f)
                scatter(MarkerType.B_BONES, 3, 3f, 10f, 17f, 14f)
                row(MarkerType.B_PILLAR, 5f, 5f, 15f)
                row(MarkerType.B_PILLAR, 11f, 5f, 15f)
                put(MarkerType.B_TREASURE, 2f, 2f)
            }
            RoomTemplate.DWARF_HALL -> build(20, 16) {
                floor(BiomeType.CASTLE_FLOOR, 0f, 0f, 20f, 16f)
                floor(BiomeType.METAL_FLOOR, 8f, 0f, 12f, 16f)
                floor(BiomeType.LAVA_POOL, 1f, 11f, 4f, 15f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 20f, 16f, gap(10f, 16f, MarkerType.B_IRON_DOOR, 2f), gap(0f, 5f), gap(20f, 5f))
                for (y in floatArrayOf(3f, 7f, 11f)) row(MarkerType.B_PILLAR, y, 6f, 14f)
                put(MarkerType.B_THRONE, 10f, 1.5f)
                row(MarkerType.B_STATUE, 1.5f, 4f, 16f)
                put(MarkerType.B_FORGE, 5f, 13f)
                put(MarkerType.B_ANVIL, 6.5f, 14f)
                put(MarkerType.B_LONG_TABLE, 16f, 10f)
                row(MarkerType.B_BARREL, 14.5f, 15f, 17f, 19f)
                put(MarkerType.B_TREASURE, 18.5f, 1.5f)
            }
            RoomTemplate.ELF_TREEHOUSES -> build(18, 16) {
                floor(BiomeType.FOREST_GROUND, 0f, 0f, 18f, 16f)
                floorCircle(BiomeType.WOOD_FLOOR, 5f, 5f, 3f, 12)
                floorCircle(BiomeType.WOOD_FLOOR, 13f, 5f, 3f, 12)
                floorCircle(BiomeType.WOOD_FLOOR, 9f, 12f, 3f, 12)
                ring(LineFeatureType.FENCE, 5f, 5f, 3f, 12, gap(8f, 5f, null))
                ring(LineFeatureType.FENCE, 13f, 5f, 3f, 12, gap(10f, 5f, null), gap(13f, 8f, null))
                ring(LineFeatureType.FENCE, 9f, 12f, 3f, 12, gap(9f, 9f, null))
                row(MarkerType.B_BIG_TREE, 5f, 5f, 13f)
                put(MarkerType.B_BIG_TREE, 9f, 12f)
                put(MarkerType.B_ROPE_BRIDGE, 9f, 5f)
                put(MarkerType.B_ROPE_BRIDGE, 11f, 8.5f)
                put(MarkerType.B_LADDER, 3f, 7f)
                put(MarkerType.B_BED, 3f, 3.5f)
                put(MarkerType.B_TABLE, 15f, 4f)
                put(MarkerType.B_BOOKSHELF, 11f, 13f)
                scatter(MarkerType.B_TREE, 4, 0.5f, 10f, 5f, 15.5f)
            }
            RoomTemplate.CATACOMBS -> build(18, 14) {
                floor(BiomeType.STONE_FLOOR, 0f, 0f, 18f, 14f)
                room(LineFeatureType.DUNGEON_WALL, 0f, 0f, 18f, 14f, gap(0f, 7f))
                wall(LineFeatureType.DUNGEON_WALL, 0f, 5f, 12f, 5f)
                wall(LineFeatureType.DUNGEON_WALL, 6f, 9f, 18f, 9f)
                wall(LineFeatureType.DUNGEON_WALL, 14f, 0f, 14f, 5f)
                for (x in floatArrayOf(2f, 4f, 6f, 8f, 10f)) put(MarkerType.B_COFFIN, x, 1f)
                for (x in floatArrayOf(8f, 10f, 12f, 14f, 16f)) put(MarkerType.B_COFFIN, x, 13f)
                put(MarkerType.B_SARCOPHAGUS, 16f, 2.5f)
                scatter(MarkerType.B_BONES, 4, 1f, 6f, 17f, 8f)
                put(MarkerType.B_CANDLES, 3f, 11f)
                put(MarkerType.B_STAIRS_DOWN, 2f, 12.5f)
                put(MarkerType.B_SPIKE_TRAP, 10f, 7f)
                put(MarkerType.B_WEB, 17f, 6f)
            }
            RoomTemplate.SUNKEN_TEMPLE -> build(16, 16) {
                floor(BiomeType.DEEP_WATER, 0f, 0f, 16f, 16f)
                floor(BiomeType.MARBLE_FLOOR, 3f, 2f, 13f, 13f)
                floor(BiomeType.SHALLOW_WATER, 3f, 9f, 13f, 13f)
                wall(LineFeatureType.RUBBLE_LINE, 3f, 13f, 3f, 2f, 7f, 2f)
                wall(LineFeatureType.DUNGEON_WALL, 9f, 2f, 13f, 2f, 13f, 9f)
                for (y in floatArrayOf(4f, 7f, 10f)) row(MarkerType.B_PILLAR, y, 5f, 11f)
                put(MarkerType.B_BROKEN_PILLAR, 8f, 11f)
                put(MarkerType.B_ALTAR, 8f, 3.5f)
                put(MarkerType.B_STATUE, 11f, 3.5f)
                put(MarkerType.B_TREASURE, 5f, 3.5f)
                put(MarkerType.B_REEDS, 14f, 14f)
                put(MarkerType.B_ROPE_BRIDGE, 8f, 14.5f)
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
