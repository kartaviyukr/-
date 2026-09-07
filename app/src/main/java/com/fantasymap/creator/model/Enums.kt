package com.fantasymap.creator.model

/** Как рисуется заливка природной зоны поверх базового цвета. */
enum class BiomePattern {
    NONE, TREES, CONIFERS, PALMS, DOTS, DUNES, GRASS, MOUNTAINS, HILLS,
    SWAMP, ICE, ROCKS, WAVES, CRACKS, CRYSTALS, FUNGI, LAVA, FIELDS
}

enum class BiomeGroup(val title: String) {
    WATER("Вода"),
    FOREST("Леса"),
    GRASS("Травы и поля"),
    DRY("Пустыни и сухие земли"),
    COLD("Холодные земли"),
    HIGH("Горы и возвышенности"),
    WET("Влажные земли"),
    FANTASY("Фэнтезийные земли")
}

/**
 * Природные зоны и ландшафты: реальные зоны со всех краёв мира плюс фэнтезийные.
 * color — базовая заливка (ARGB), pattern — рисунок поверх.
 */
enum class BiomeType(
    val title: String,
    val group: BiomeGroup,
    val color: Int,
    val pattern: BiomePattern
) {
    // Вода
    SHALLOW_SEA("Мелководье", BiomeGroup.WATER, 0xFF6FA6C9.toInt(), BiomePattern.WAVES),
    DEEP_SEA("Глубокое море", BiomeGroup.WATER, 0xFF3A6E96.toInt(), BiomePattern.WAVES),
    ABYSS("Океанская бездна", BiomeGroup.WATER, 0xFF27506F.toInt(), BiomePattern.WAVES),
    CORAL_REEF("Коралловый риф", BiomeGroup.WATER, 0xFF74C3B4.toInt(), BiomePattern.DOTS),
    KELP_FOREST("Ламинариевый лес", BiomeGroup.WATER, 0xFF4C8478.toInt(), BiomePattern.WAVES),
    ICE_SEA("Ледяное море", BiomeGroup.WATER, 0xFFA9C9D9.toInt(), BiomePattern.ICE),

    // Леса
    TAIGA("Тайга", BiomeGroup.FOREST, 0xFF3B6349.toInt(), BiomePattern.CONIFERS),
    CONIFER_FOREST("Хвойный лес", BiomeGroup.FOREST, 0xFF41704F.toInt(), BiomePattern.CONIFERS),
    MIXED_FOREST("Смешанный лес", BiomeGroup.FOREST, 0xFF56814F.toInt(), BiomePattern.TREES),
    BROADLEAF_FOREST("Широколиственный лес", BiomeGroup.FOREST, 0xFF66914D.toInt(), BiomePattern.TREES),
    RAINFOREST("Тропический дождевой лес", BiomeGroup.FOREST, 0xFF36763F.toInt(), BiomePattern.PALMS),
    MONSOON_FOREST("Муссонный лес", BiomeGroup.FOREST, 0xFF4E8A45.toInt(), BiomePattern.PALMS),
    CLOUD_FOREST("Туманный лес", BiomeGroup.FOREST, 0xFF63947A.toInt(), BiomePattern.TREES),
    BAMBOO_FOREST("Бамбуковый лес", BiomeGroup.FOREST, 0xFF87A960.toInt(), BiomePattern.GRASS),
    MANGROVE("Мангровые заросли", BiomeGroup.FOREST, 0xFF477355.toInt(), BiomePattern.SWAMP),
    WOODLAND("Редколесье", BiomeGroup.FOREST, 0xFF849A5C.toInt(), BiomePattern.TREES),
    OLIVE_GROVE("Оливковые рощи", BiomeGroup.FOREST, 0xFF8A9A63.toInt(), BiomePattern.TREES),

    // Травы и поля
    STEPPE("Степь", BiomeGroup.GRASS, 0xFFC6B76B.toInt(), BiomePattern.GRASS),
    PRAIRIE("Прерия", BiomeGroup.GRASS, 0xFFCCC074.toInt(), BiomePattern.GRASS),
    SAVANNA("Саванна", BiomeGroup.GRASS, 0xFFCDAE55.toInt(), BiomePattern.GRASS),
    MEADOW("Луга", BiomeGroup.GRASS, 0xFFA1BD70.toInt(), BiomePattern.GRASS),
    FARMLAND("Пашни и поля", BiomeGroup.GRASS, 0xFFBFAF67.toInt(), BiomePattern.FIELDS),
    SHRUBLAND("Кустарниковые пустоши", BiomeGroup.GRASS, 0xFF9FA064.toInt(), BiomePattern.DOTS),
    MAQUIS("Маквис (средиземноморье)", BiomeGroup.GRASS, 0xFFA8AE62.toInt(), BiomePattern.DOTS),
    TUSSOCK("Кочкарник", BiomeGroup.GRASS, 0xFFB2B071.toInt(), BiomePattern.GRASS),

    // Сухие земли
    SAND_DESERT("Песчаная пустыня", BiomeGroup.DRY, 0xFFE3CF93.toInt(), BiomePattern.DUNES),
    ROCK_DESERT("Каменистая пустыня (хамада)", BiomeGroup.DRY, 0xFFCCBD91.toInt(), BiomePattern.ROCKS),
    SEMI_DESERT("Полупустыня", BiomeGroup.DRY, 0xFFD7C991.toInt(), BiomePattern.DOTS),
    SALT_FLAT("Солончак", BiomeGroup.DRY, 0xFFE9E4D6.toInt(), BiomePattern.CRACKS),
    BADLANDS("Бэдленды", BiomeGroup.DRY, 0xFFBD936E.toInt(), BiomePattern.ROCKS),
    OASIS("Оазис", BiomeGroup.DRY, 0xFF74A45E.toInt(), BiomePattern.PALMS),
    COASTAL_DUNES("Прибрежные дюны", BiomeGroup.DRY, 0xFFE8D9AA.toInt(), BiomePattern.DUNES),
    VOLCANIC_FIELD("Вулканические поля", BiomeGroup.DRY, 0xFF73635C.toInt(), BiomePattern.ROCKS),
    LAVA_FIELD("Лавовые поля", BiomeGroup.DRY, 0xFF4F3E3C.toInt(), BiomePattern.LAVA),

    // Холодные земли
    TUNDRA("Тундра", BiomeGroup.COLD, 0xFFAEB99E.toInt(), BiomePattern.DOTS),
    FOREST_TUNDRA("Лесотундра", BiomeGroup.COLD, 0xFF94A887.toInt(), BiomePattern.CONIFERS),
    POLAR_DESERT("Полярная пустыня", BiomeGroup.COLD, 0xFFDDE7ED.toInt(), BiomePattern.ICE),
    ICE_SHEET("Ледниковый щит", BiomeGroup.COLD, 0xFFEAF2F7.toInt(), BiomePattern.ICE),
    GLACIER("Ледник", BiomeGroup.COLD, 0xFFD2E5F0.toInt(), BiomePattern.ICE),
    PERMAFROST("Вечная мерзлота", BiomeGroup.COLD, 0xFFBECAC6.toInt(), BiomePattern.CRACKS),
    SNOWFIELD("Снежники", BiomeGroup.COLD, 0xFFEFF4F8.toInt(), BiomePattern.ICE),

    // Горы и возвышенности
    HIGH_MOUNTAINS("Высокие горы", BiomeGroup.HIGH, 0xFF90857D.toInt(), BiomePattern.MOUNTAINS),
    MOUNTAINS("Горы", BiomeGroup.HIGH, 0xFF9E9389.toInt(), BiomePattern.MOUNTAINS),
    HILLS("Холмы", BiomeGroup.HIGH, 0xFFACA57C.toInt(), BiomePattern.HILLS),
    FOOTHILLS("Предгорья", BiomeGroup.HIGH, 0xFFB4AC87.toInt(), BiomePattern.HILLS),
    PLATEAU("Плоскогорье", BiomeGroup.HIGH, 0xFFB9AD91.toInt(), BiomePattern.ROCKS),
    ALPINE_MEADOW("Альпийские луга", BiomeGroup.HIGH, 0xFF93B47E.toInt(), BiomePattern.GRASS),
    KARST("Карстовые земли", BiomeGroup.HIGH, 0xFFBBB6AA.toInt(), BiomePattern.ROCKS),
    CANYONS("Каньоны", BiomeGroup.HIGH, 0xFFB48868.toInt(), BiomePattern.ROCKS),
    FJORDS("Фьорды", BiomeGroup.HIGH, 0xFF839EAA.toInt(), BiomePattern.ROCKS),
    CLIFF_COAST("Скалистое побережье", BiomeGroup.HIGH, 0xFFA5A398.toInt(), BiomePattern.ROCKS),

    // Влажные земли
    SWAMP("Болото", BiomeGroup.WET, 0xFF6F7E59.toInt(), BiomePattern.SWAMP),
    MARSH("Топи", BiomeGroup.WET, 0xFF798965.toInt(), BiomePattern.SWAMP),
    PEAT_BOG("Торфяник", BiomeGroup.WET, 0xFF6E6E52.toInt(), BiomePattern.SWAMP),
    FLOODPLAIN("Пойма", BiomeGroup.WET, 0xFF94AB6F.toInt(), BiomePattern.GRASS),
    DELTA("Дельта реки", BiomeGroup.WET, 0xFF83A47E.toInt(), BiomePattern.SWAMP),
    RIVER_VALLEY("Речная долина", BiomeGroup.WET, 0xFF97B272.toInt(), BiomePattern.GRASS),
    LAKELAND("Озёрный край", BiomeGroup.WET, 0xFF83A7AC.toInt(), BiomePattern.WAVES),
    RICE_TERRACES("Рисовые террасы", BiomeGroup.WET, 0xFF9DB86C.toInt(), BiomePattern.FIELDS),

    // Фэнтези
    ENCHANTED_FOREST("Зачарованный лес", BiomeGroup.FANTASY, 0xFF44707E.toInt(), BiomePattern.TREES),
    DARK_FOREST("Тёмный лес", BiomeGroup.FANTASY, 0xFF2F4037.toInt(), BiomePattern.CONIFERS),
    FUNGAL_FOREST("Грибной лес", BiomeGroup.FANTASY, 0xFF6F527E.toInt(), BiomePattern.FUNGI),
    CURSED_WASTE("Проклятая пустошь", BiomeGroup.FANTASY, 0xFF5E4E59.toInt(), BiomePattern.CRACKS),
    ASHLANDS("Пепельные земли", BiomeGroup.FANTASY, 0xFF6F6964.toInt(), BiomePattern.DOTS),
    BLIGHTED_LAND("Выжженные земли", BiomeGroup.FANTASY, 0xFF7E5F4E.toInt(), BiomePattern.CRACKS),
    CRYSTAL_FIELDS("Кристальные поля", BiomeGroup.FANTASY, 0xFF92A0CD.toInt(), BiomePattern.CRYSTALS),
    FEY_GLADE("Поляны фей", BiomeGroup.FANTASY, 0xFF83CDA9.toInt(), BiomePattern.GRASS),
    SHADOW_MARSH("Сумрачные топи", BiomeGroup.FANTASY, 0xFF4E4E62.toInt(), BiomePattern.SWAMP),
    DRAGON_WASTES("Драконьи пустоши", BiomeGroup.FANTASY, 0xFF8E5E4E.toInt(), BiomePattern.ROCKS),
    FLOATING_ISLES("Парящие острова", BiomeGroup.FANTASY, 0xFFADC8DA.toInt(), BiomePattern.CRYSTALS),
    MAGMA_WASTE("Огненные пустоши", BiomeGroup.FANTASY, 0xFF7E3F32.toInt(), BiomePattern.LAVA),
    FROZEN_WASTE("Ледяные пустоши", BiomeGroup.FANTASY, 0xFFCADCE8.toInt(), BiomePattern.ICE),
    HOLY_LAND("Священные земли", BiomeGroup.FANTASY, 0xFFDDCD93.toInt(), BiomePattern.DOTS),
    BATTLE_SCARRED("Земли вечной войны", BiomeGroup.FANTASY, 0xFF8B7A63.toInt(), BiomePattern.CRACKS);

    companion object {
        fun byGroup(group: BiomeGroup): List<BiomeType> = BiomeType.entries.filter { it.group == group }
    }
}

/** Форма значка объекта на карте. */
enum class Glyph {
    CAPITAL, CITY, TOWN, VILLAGE, CASTLE, TOWER, GATE, WALL, CAMP, BATTLE, ARENA,
    ANCHOR, SHIP, LIGHTHOUSE, BRIDGE, MARKET, TEMPLE, OBELISK, GRAVE, PORTAL,
    STONE_CIRCLE, RUINS, DUNGEON, CAVE, MOUNTAIN, VOLCANO, WATERFALL, GEYSER,
    WHIRLPOOL, ROCK, TREE, MINE, FARM, MILL, FORGE, MONSTER, TREASURE, ANOMALY
}

enum class MarkerGroup(val title: String) {
    SETTLEMENT("Поселения"),
    MILITARY("Крепости и война"),
    PORT("Порты и переправы"),
    TRADE("Торговля и ремёсла"),
    RELIGION("Храмы и погребения"),
    MAGIC("Магия"),
    RUIN("Руины и подземелья"),
    NATURE("Природные объекты"),
    RESOURCE("Ресурсы"),
    DANGER("Опасности и легенды")
}

/** Все виды объектов фэнтезийного мира, которые можно поставить на карту. */
enum class MarkerType(
    val title: String,
    val group: MarkerGroup,
    val glyph: Glyph,
    val defaultScale: Float = 1f
) {
    // Поселения
    CAPITAL("Столица", MarkerGroup.SETTLEMENT, Glyph.CAPITAL, 1.5f),
    METROPOLIS("Великий город", MarkerGroup.SETTLEMENT, Glyph.CITY, 1.3f),
    CITY("Город", MarkerGroup.SETTLEMENT, Glyph.CITY, 1.1f),
    TOWN("Городок", MarkerGroup.SETTLEMENT, Glyph.TOWN),
    VILLAGE("Деревня", MarkerGroup.SETTLEMENT, Glyph.VILLAGE, 0.85f),
    HAMLET("Хутор", MarkerGroup.SETTLEMENT, Glyph.VILLAGE, 0.7f),
    FISHING_VILLAGE("Рыбацкая деревня", MarkerGroup.SETTLEMENT, Glyph.VILLAGE, 0.85f),
    NOMAD_CAMP("Стойбище кочевников", MarkerGroup.SETTLEMENT, Glyph.CAMP),
    ELVEN_SETTLEMENT("Эльфийское поселение", MarkerGroup.SETTLEMENT, Glyph.TREE, 1.1f),
    DWARVEN_HOLD("Гномий чертог", MarkerGroup.SETTLEMENT, Glyph.CAVE, 1.1f),
    ORC_STRONGHOLD("Орочья твердыня", MarkerGroup.SETTLEMENT, Glyph.CASTLE, 1.1f),
    UNDERGROUND_CITY("Подземный город", MarkerGroup.SETTLEMENT, Glyph.CAVE, 1.2f),
    FLOATING_CITY("Парящий город", MarkerGroup.SETTLEMENT, Glyph.TOWER, 1.2f),

    // Крепости и война
    FORTRESS("Крепость", MarkerGroup.MILITARY, Glyph.CASTLE, 1.1f),
    CASTLE("Замок", MarkerGroup.MILITARY, Glyph.CASTLE),
    CITADEL("Цитадель", MarkerGroup.MILITARY, Glyph.CASTLE, 1.3f),
    WATCHTOWER("Сторожевая башня", MarkerGroup.MILITARY, Glyph.TOWER, 0.85f),
    BARRACKS("Казармы", MarkerGroup.MILITARY, Glyph.TOWN, 0.85f),
    OUTPOST("Аванпост", MarkerGroup.MILITARY, Glyph.CAMP, 0.85f),
    BORDER_POST("Пограничная застава", MarkerGroup.MILITARY, Glyph.GATE, 0.85f),
    GREAT_GATE("Великие врата", MarkerGroup.MILITARY, Glyph.GATE, 1.2f),
    FORTIFICATION("Укрепления", MarkerGroup.MILITARY, Glyph.WALL),
    SIEGE_CAMP("Осадный лагерь", MarkerGroup.MILITARY, Glyph.CAMP),
    BATTLEFIELD("Поле битвы", MarkerGroup.MILITARY, Glyph.BATTLE),
    ARENA("Арена", MarkerGroup.MILITARY, Glyph.ARENA),
    PRISON("Темница", MarkerGroup.MILITARY, Glyph.CASTLE, 0.9f),

    // Порты и переправы
    PORT("Порт", MarkerGroup.PORT, Glyph.ANCHOR, 1.1f),
    HARBOR("Гавань", MarkerGroup.PORT, Glyph.ANCHOR),
    SHIPYARD("Верфь", MarkerGroup.PORT, Glyph.SHIP),
    LIGHTHOUSE("Маяк", MarkerGroup.PORT, Glyph.LIGHTHOUSE),
    FERRY("Переправа", MarkerGroup.PORT, Glyph.SHIP, 0.85f),
    BRIDGE("Мост", MarkerGroup.PORT, Glyph.BRIDGE),
    FORD("Брод", MarkerGroup.PORT, Glyph.BRIDGE, 0.8f),
    TOLL_GATE("Таможенная застава", MarkerGroup.PORT, Glyph.GATE, 0.85f),

    // Торговля и ремёсла
    MARKET("Рынок", MarkerGroup.TRADE, Glyph.MARKET),
    TRADING_POST("Торговый пост", MarkerGroup.TRADE, Glyph.MARKET, 0.9f),
    CARAVANSERAI("Караван-сарай", MarkerGroup.TRADE, Glyph.MARKET),
    INN("Трактир", MarkerGroup.TRADE, Glyph.VILLAGE, 0.75f),
    GUILD_HALL("Дом гильдии", MarkerGroup.TRADE, Glyph.MARKET),
    MINT("Монетный двор", MarkerGroup.TRADE, Glyph.MARKET),
    LIBRARY("Великая библиотека", MarkerGroup.TRADE, Glyph.TEMPLE),
    UNIVERSITY("Университет", MarkerGroup.TRADE, Glyph.TEMPLE),

    // Храмы и погребения
    TEMPLE("Храм", MarkerGroup.RELIGION, Glyph.TEMPLE),
    GRAND_TEMPLE("Великий храм", MarkerGroup.RELIGION, Glyph.TEMPLE, 1.3f),
    CATHEDRAL("Собор", MarkerGroup.RELIGION, Glyph.TEMPLE, 1.2f),
    MONASTERY("Монастырь", MarkerGroup.RELIGION, Glyph.TEMPLE, 0.95f),
    SHRINE("Святилище", MarkerGroup.RELIGION, Glyph.OBELISK, 0.8f),
    ORACLE("Оракул", MarkerGroup.RELIGION, Glyph.OBELISK),
    ALTAR("Алтарь", MarkerGroup.RELIGION, Glyph.OBELISK, 0.8f),
    SACRED_GROVE("Священная роща", MarkerGroup.RELIGION, Glyph.TREE),
    NECROPOLIS("Некрополь", MarkerGroup.RELIGION, Glyph.GRAVE, 1.1f),
    GRAVEYARD("Кладбище", MarkerGroup.RELIGION, Glyph.GRAVE, 0.85f),
    TOMB("Гробница", MarkerGroup.RELIGION, Glyph.GRAVE),

    // Магия
    WIZARD_TOWER("Башня волшебника", MarkerGroup.MAGIC, Glyph.TOWER, 1.1f),
    MAGE_ACADEMY("Академия магии", MarkerGroup.MAGIC, Glyph.TOWER, 1.25f),
    PORTAL("Портал", MarkerGroup.MAGIC, Glyph.PORTAL),
    LEY_NEXUS("Узел силы", MarkerGroup.MAGIC, Glyph.PORTAL),
    STONE_CIRCLE("Каменный круг", MarkerGroup.MAGIC, Glyph.STONE_CIRCLE),
    OBELISK("Обелиск", MarkerGroup.MAGIC, Glyph.OBELISK),
    ARCANE_RUINS("Магические руины", MarkerGroup.MAGIC, Glyph.RUINS),
    WITCH_HUT("Хижина ведьмы", MarkerGroup.MAGIC, Glyph.VILLAGE, 0.75f),
    ALCHEMY_LAB("Алхимическая лаборатория", MarkerGroup.MAGIC, Glyph.TOWER, 0.9f),
    ANOMALY("Магическая аномалия", MarkerGroup.MAGIC, Glyph.ANOMALY),

    // Руины и подземелья
    RUINS("Руины", MarkerGroup.RUIN, Glyph.RUINS),
    ANCIENT_RUINS("Древние руины", MarkerGroup.RUIN, Glyph.RUINS, 1.15f),
    LOST_CITY("Затерянный город", MarkerGroup.RUIN, Glyph.RUINS, 1.25f),
    ABANDONED_FORT("Заброшенный форт", MarkerGroup.RUIN, Glyph.RUINS),
    SHIPWRECK("Затонувший корабль", MarkerGroup.RUIN, Glyph.SHIP, 0.9f),
    DUNGEON("Подземелье", MarkerGroup.RUIN, Glyph.DUNGEON),
    CATACOMBS("Катакомбы", MarkerGroup.RUIN, Glyph.DUNGEON),
    CRYPT("Склеп", MarkerGroup.RUIN, Glyph.GRAVE, 0.85f),

    // Природа
    MOUNTAIN_PEAK("Горная вершина", MarkerGroup.NATURE, Glyph.MOUNTAIN),
    VOLCANO("Вулкан", MarkerGroup.NATURE, Glyph.VOLCANO, 1.2f),
    MOUNTAIN_PASS("Перевал", MarkerGroup.NATURE, Glyph.MOUNTAIN, 0.85f),
    CAVE("Пещера", MarkerGroup.NATURE, Glyph.CAVE),
    WATERFALL("Водопад", MarkerGroup.NATURE, Glyph.WATERFALL),
    GEYSER("Гейзер", MarkerGroup.NATURE, Glyph.GEYSER),
    HOT_SPRING("Горячий источник", MarkerGroup.NATURE, Glyph.GEYSER, 0.85f),
    SPRING("Родник", MarkerGroup.NATURE, Glyph.GEYSER, 0.75f),
    OASIS_POINT("Оазис", MarkerGroup.NATURE, Glyph.TREE),
    ANCIENT_TREE("Древо-исполин", MarkerGroup.NATURE, Glyph.TREE, 1.2f),
    GROVE("Роща", MarkerGroup.NATURE, Glyph.TREE, 0.85f),
    WHIRLPOOL("Водоворот", MarkerGroup.NATURE, Glyph.WHIRLPOOL),
    REEF("Риф", MarkerGroup.NATURE, Glyph.ROCK, 0.85f),
    ISLET("Островок", MarkerGroup.NATURE, Glyph.ROCK, 0.8f),
    CRATER("Кратер", MarkerGroup.NATURE, Glyph.ROCK),
    GLACIER_TONGUE("Язык ледника", MarkerGroup.NATURE, Glyph.MOUNTAIN),

    // Ресурсы
    MINE("Шахта", MarkerGroup.RESOURCE, Glyph.MINE),
    GOLD_MINE("Золотой рудник", MarkerGroup.RESOURCE, Glyph.MINE),
    IRON_MINE("Железный рудник", MarkerGroup.RESOURCE, Glyph.MINE),
    GEM_MINE("Самоцветные копи", MarkerGroup.RESOURCE, Glyph.MINE),
    QUARRY("Каменоломня", MarkerGroup.RESOURCE, Glyph.MINE, 0.9f),
    SALT_MINE("Соляные копи", MarkerGroup.RESOURCE, Glyph.MINE, 0.9f),
    LUMBER_CAMP("Лесопилка", MarkerGroup.RESOURCE, Glyph.TREE, 0.85f),
    FARMSTEAD("Ферма", MarkerGroup.RESOURCE, Glyph.FARM, 0.85f),
    MILL("Мельница", MarkerGroup.RESOURCE, Glyph.MILL, 0.85f),
    FORGE("Кузница", MarkerGroup.RESOURCE, Glyph.FORGE, 0.9f),
    FISHERY("Рыбные промыслы", MarkerGroup.RESOURCE, Glyph.ANCHOR, 0.8f),
    VINEYARD("Виноградник", MarkerGroup.RESOURCE, Glyph.FARM, 0.85f),
    HERB_GARDEN("Травяные сады", MarkerGroup.RESOURCE, Glyph.FARM, 0.8f),

    // Опасности и легенды
    DRAGON_LAIR("Логово дракона", MarkerGroup.DANGER, Glyph.MONSTER, 1.2f),
    BEAST_LAIR("Логово зверя", MarkerGroup.DANGER, Glyph.MONSTER),
    MONSTER_NEST("Гнездо чудовищ", MarkerGroup.DANGER, Glyph.MONSTER),
    BANDIT_CAMP("Лагерь разбойников", MarkerGroup.DANGER, Glyph.CAMP, 0.85f),
    CURSED_PLACE("Проклятое место", MarkerGroup.DANGER, Glyph.ANOMALY),
    HAUNTED_SITE("Призрачное место", MarkerGroup.DANGER, Glyph.GRAVE),
    DEMON_GATE("Врата демонов", MarkerGroup.DANGER, Glyph.PORTAL, 1.15f),
    TREASURE("Сокровище", MarkerGroup.DANGER, Glyph.TREASURE),
    LEGEND_SITE("Легендарное место", MarkerGroup.DANGER, Glyph.ANOMALY);

    val isSettlement: Boolean
        get() = group == MarkerGroup.SETTLEMENT

    companion object {
        fun byGroup(group: MarkerGroup): List<MarkerType> = MarkerType.entries.filter { it.group == group }
    }
}

/** Линейные природные объекты. */
enum class LineFeatureType(
    val title: String,
    val color: Int,
    val defaultWidth: Float,
    val closedLoop: Boolean = false
) {
    RIVER("Река", 0xFF4C86AE.toInt(), 3.5f),
    BIG_RIVER("Великая река", 0xFF3F7BA4.toInt(), 6f),
    STREAM("Ручей", 0xFF5E97BD.toInt(), 2f),
    CANAL("Канал", 0xFF4E8FB0.toInt(), 3f),
    MOUNTAIN_RANGE("Горный хребет", 0xFF7C7168.toInt(), 10f),
    HILL_RANGE("Гряда холмов", 0xFF97906E.toInt(), 8f),
    FOREST_BELT("Лесная полоса", 0xFF3F6B45.toInt(), 10f),
    CLIFF("Обрыв", 0xFF6E665E.toInt(), 4f),
    CANYON("Каньон", 0xFF9A7455.toInt(), 6f),
    REEF_LINE("Гряда рифов", 0xFF56A79A.toInt(), 4f),
    GREAT_WALL("Великая стена", 0xFF5A5048.toInt(), 5f),
    ICE_WALL("Ледяная стена", 0xFF9EC3D8.toInt(), 5f),
    LAVA_FLOW("Лавовый поток", 0xFFB4502E.toInt(), 4f)
}

/** Пути сообщения. */
enum class RoadType(
    val title: String,
    val color: Int,
    val width: Float,
    val dashed: Boolean
) {
    HIGHWAY("Тракт", 0xFF7A5E3A.toInt(), 3.5f, false),
    ROAD("Дорога", 0xFF8B6B45.toInt(), 2.4f, false),
    TRAIL("Тропа", 0xFF937A55.toInt(), 1.6f, true),
    CARAVAN_ROUTE("Караванный путь", 0xFFA07C48.toInt(), 2.2f, true),
    SEA_ROUTE("Морской путь", 0xFF3D6E8E.toInt(), 2f, true),
    SECRET_PATH("Тайная тропа", 0xFF6B5A6E.toInt(), 1.6f, true)
}

/** Виды суши. */
enum class LandKind(val title: String) {
    CONTINENT("Континент"),
    ISLAND("Остров"),
    PENINSULA("Полуостров")
}

/** Виды внутренних водоёмов. */
enum class WaterKind(val title: String, val color: Int) {
    LAKE("Озеро", 0xFF5E9AC0.toInt()),
    INNER_SEA("Внутреннее море", 0xFF4A85AE.toInt()),
    SALT_LAKE("Солёное озеро", 0xFF8FB0BE.toInt()),
    BAY("Залив", 0xFF5A94BC.toInt())
}

/** Стиль подписи на карте. */
enum class LabelStyle(val title: String, val size: Float, val color: Int, val italic: Boolean) {
    TITLE("Название мира", 34f, 0xFF3A2E22.toInt(), false),
    REGION("Область", 22f, 0xFF4A3B2C.toInt(), false),
    WATER("Море и вода", 20f, 0xFF2F5F7E.toInt(), true),
    MOUNTAIN("Горы", 18f, 0xFF564E45.toInt(), false),
    SMALL("Мелкая подпись", 14f, 0xFF4A3B2C.toInt(), true)
}

/** Этапы создания карты — порядок работы над миром. */
enum class Stage(val number: Int, val title: String, val hint: String) {
    CONTINENTS(1, "Континенты", "Обведите пальцем очертания континентов и островов. Всё, что не суша — океан."),
    BIOMES(2, "Природные зоны", "Закрасьте области ландшафтов: леса, степи, пустыни, горы, льды."),
    NATURE(3, "Природные объекты", "Проведите реки и хребты, отметьте вершины, пещеры, водопады."),
    SETTLEMENTS(4, "Города и дороги", "Расставьте города, деревни, порты и соедините их дорогами."),
    CAPITALS(5, "Столицы", "Отметьте столицы государств — коснитесь города, чтобы сделать его столицей."),
    SPECIAL(6, "Особые строения", "Храмы, башни магов, руины, шахты, порталы и логова чудовищ."),
    BORDERS(7, "Границы стран", "Обведите территории государств. Граница может проходить и по воде."),
    COUNTRIES(8, "Информация о странах", "Заполните описание каждой страны: правитель, народы, вера, армия.");

    companion object {
        fun byNumber(number: Int): Stage = Stage.entries.firstOrNull { it.number == number } ?: CONTINENTS
    }
}

/** Инструмент рисования. */
enum class Tool(val title: String, val icon: String) {
    PAN("Перемещение", "✋"),
    SELECT("Выбрать", "☝"),
    LAND("Континент", "🗺"),
    ISLAND("Остров", "🏝"),
    WATER("Озеро / море", "💧"),
    BIOME("Ландшафт", "🖌"),
    LINE("Река / хребет", "〰"),
    ROAD("Дорога", "🛣"),
    MARKER("Объект", "📍"),
    COUNTRY("Территория страны", "🚩"),
    LABEL("Подпись", "🔤"),
    ERASER("Стереть", "🧽")
}
