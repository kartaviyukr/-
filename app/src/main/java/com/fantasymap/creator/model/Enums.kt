package com.fantasymap.creator.model

/** Как рисуется заливка природной зоны поверх базового цвета. */
enum class BiomePattern {
    NONE, TREES, CONIFERS, PALMS, DOTS, DUNES, GRASS, MOUNTAINS, HILLS,
    SWAMP, ICE, ROCKS, WAVES, CRACKS, CRYSTALS, FUNGI, LAVA, FIELDS,
    RUNES, BONES, STARS, SPIRES, EYES, FEATHERS
}

enum class BiomeGroup(val title: String) {
    CITY("Городские зоны"),
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
    BATTLE_SCARRED("Земли вечной войны", BiomeGroup.FANTASY, 0xFF8B7A63.toInt(), BiomePattern.CRACKS),
    SPIRIT_WOODS("Лес духов", BiomeGroup.FANTASY, 0xFF4A6E74.toInt(), BiomePattern.EYES),
    TWILIGHT_FOREST("Сумеречный лес", BiomeGroup.FANTASY, 0xFF3A4458.toInt(), BiomePattern.CONIFERS),
    AMBER_FOREST("Янтарный лес", BiomeGroup.FANTASY, 0xFFB08A47.toInt(), BiomePattern.TREES),
    SILVER_TAIGA("Серебряная тайга", BiomeGroup.FANTASY, 0xFF7E9099.toInt(), BiomePattern.CONIFERS),
    GIANT_FUNGI_PLAIN("Равнина грибов-исполинов", BiomeGroup.FANTASY, 0xFF7A5E86.toInt(), BiomePattern.FUNGI),
    SINGING_DUNES("Поющие пески", BiomeGroup.FANTASY, 0xFFE0C79B.toInt(), BiomePattern.DUNES),
    GLASS_DESERT("Стеклянная пустыня", BiomeGroup.FANTASY, 0xFFC8D4DA.toInt(), BiomePattern.CRYSTALS),
    MIRROR_FLATS("Зеркальная равнина", BiomeGroup.FANTASY, 0xFFD3DCE2.toInt(), BiomePattern.CRACKS),
    STORM_STEPPE("Грозовая степь", BiomeGroup.FANTASY, 0xFF8E9483.toInt(), BiomePattern.GRASS),
    WHISPER_PLAINS("Шепчущие равнины", BiomeGroup.FANTASY, 0xFFAFB48C.toInt(), BiomePattern.GRASS),
    BLOOD_MARSH("Кровавые топи", BiomeGroup.FANTASY, 0xFF6E3F44.toInt(), BiomePattern.SWAMP),
    BONE_FIELDS("Костяные поля", BiomeGroup.FANTASY, 0xFFC4BCA6.toInt(), BiomePattern.BONES),
    RUNE_WASTES("Рунные пустоши", BiomeGroup.FANTASY, 0xFF8A8478.toInt(), BiomePattern.RUNES),
    STARFALL_FIELD("Поле звездопада", BiomeGroup.FANTASY, 0xFF4E5578.toInt(), BiomePattern.STARS),
    SKY_MEADOWS("Небесные луга", BiomeGroup.FANTASY, 0xFFA9CBB0.toInt(), BiomePattern.FEATHERS),
    MIST_VALE("Туманный дол", BiomeGroup.FANTASY, 0xFFAEB8B4.toInt(), BiomePattern.SWAMP),
    EMBER_HILLS("Тлеющие холмы", BiomeGroup.FANTASY, 0xFF8E5C46.toInt(), BiomePattern.HILLS),
    LIVING_STONE("Живой камень", BiomeGroup.FANTASY, 0xFF7E7468.toInt(), BiomePattern.EYES),
    VOID_SCAR("Провал пустоты", BiomeGroup.FANTASY, 0xFF35303E.toInt(), BiomePattern.STARS),
    SUNKEN_LANDS("Затонувшие земли", BiomeGroup.FANTASY, 0xFF5E7E86.toInt(), BiomePattern.WAVES),
    CORAL_JUNGLE("Коралловые джунгли", BiomeGroup.FANTASY, 0xFF6EA8A0.toInt(), BiomePattern.SPIRES),
    SPIRE_FOREST("Лес каменных шпилей", BiomeGroup.FANTASY, 0xFFA3998A.toInt(), BiomePattern.SPIRES),
    ETERNAL_NIGHT("Земли вечной ночи", BiomeGroup.FANTASY, 0xFF3B3A4E.toInt(), BiomePattern.STARS),
    DREAM_MEADOW("Сонные луга", BiomeGroup.FANTASY, 0xFFB7A9D6.toInt(), BiomePattern.GRASS),

    // Городские зоны — для карты города
    CITY_PARK("Парк", BiomeGroup.CITY, 0xFF8FBE79.toInt(), BiomePattern.TREES),
    CITY_GARDEN("Сады", BiomeGroup.CITY, 0xFFA8C97F.toInt(), BiomePattern.TREES),
    CITY_ORCHARD("Плодовый сад", BiomeGroup.CITY, 0xFFB2CC84.toInt(), BiomePattern.TREES),
    CITY_VEGETABLE("Огороды", BiomeGroup.CITY, 0xFFBCC182.toInt(), BiomePattern.FIELDS),
    CITY_GRAVEYARD("Кладбище", BiomeGroup.CITY, 0xFF9DA592.toInt(), BiomePattern.BONES),
    CITY_SQUARE("Мощёная площадь", BiomeGroup.CITY, 0xFFC9BFA6.toInt(), BiomePattern.FIELDS),
    CITY_MARKET_SQUARE("Рыночная площадь", BiomeGroup.CITY, 0xFFD3BE8E.toInt(), BiomePattern.DOTS),
    CITY_YARD("Дворы", BiomeGroup.CITY, 0xFFC4B69A.toInt(), BiomePattern.DOTS),
    CITY_MUD("Грязные пустыри", BiomeGroup.CITY, 0xFFA89A82.toInt(), BiomePattern.CRACKS),
    CITY_RUBBLE("Пожарище", BiomeGroup.CITY, 0xFF8E8478.toInt(), BiomePattern.ROCKS),
    CITY_POND("Городской пруд", BiomeGroup.CITY, 0xFF7FA8C0.toInt(), BiomePattern.WAVES),
    CITY_DRILL_YARD("Плац", BiomeGroup.CITY, 0xFFBFB396.toInt(), BiomePattern.DOTS),
    CITY_FAIR("Ярмарочное поле", BiomeGroup.CITY, 0xFFCDBE96.toInt(), BiomePattern.DOTS),
    CITY_GROVE("Роща у стен", BiomeGroup.CITY, 0xFF86A96C.toInt(), BiomePattern.TREES);

    companion object {
        fun byGroup(group: BiomeGroup): List<BiomeType> = BiomeType.entries.filter { it.group == group }
    }
}

/** Форма значка объекта на карте. */
enum class Glyph {
    CAPITAL, CITY, TOWN, VILLAGE, CASTLE, TOWER, GATE, WALL, CAMP, BATTLE, ARENA,
    ANCHOR, SHIP, LIGHTHOUSE, BRIDGE, MARKET, TEMPLE, OBELISK, GRAVE, PORTAL,
    STONE_CIRCLE, RUINS, DUNGEON, CAVE, MOUNTAIN, VOLCANO, WATERFALL, GEYSER,
    WHIRLPOOL, ROCK, TREE, MINE, FARM, MILL, FORGE, MONSTER, TREASURE, ANOMALY,
    DRAGON, BEAST, NEST, CRYSTAL, MUSHROOM, DEAD_TREE, FLOWER, BEACON, STATUE,
    WELL, AIRSHIP, EYE, SPIRE, BANNER, SWORD, BOOK, POTION, CROWN, OBSERVATORY,
    ARCH, CAULDRON, TOTEM, BONES, FLOATING_ROCK, RUNE_STONE,
    INGOT, COINS, GEM, SALT, COAL, MARBLE, TIMBER, WHEAT, FISH, GRAPES, WOOL,
    HORSESHOE, CATTLE, SPICE, SILK, FUR, AMBER, PEARL, OIL, SULFUR, HERBS,
    STONE_BLOCKS, MITHRIL, INN_SIGN,
    FOUNTAIN, LANTERN, SIGNPOST, CART, STALL, BARREL, PILLORY, GALLOWS,
    CLOCK, BELL, HATCH, PLAQUE, DRAWBRIDGE_GLYPH, PORTCULLIS
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
    DANGER("Опасности и легенды"),
    WONDER("Чудеса света"),
    FAUNA("Звери и гнёзда"),
    GOODS("Ресурсы и товары"),
    ATLAS("Переходы между картами"),
    CITY_WALLS("Ворота, башни, стены"),
    CITY_STREET("Улица и площадь"),
    CITY_SERVICE("Городские службы"),
    CITY_SPECIAL("Особые места города")
}

/** Все виды объектов фэнтезийного мира, которые можно поставить на карту. */
/** Где уместен объект: на карте мира, на карте города или везде. */
enum class MarkerScope { WORLD, CITY, BOTH }

enum class MarkerType(
    val title: String,
    val group: MarkerGroup,
    val glyph: Glyph,
    val defaultScale: Float = 1f,
    val scope: MarkerScope = MarkerScope.WORLD
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
    BRIDGE("Мост", MarkerGroup.PORT, Glyph.BRIDGE, 1f, MarkerScope.BOTH),
    FORD("Брод", MarkerGroup.PORT, Glyph.BRIDGE, 0.8f, MarkerScope.BOTH),
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
    LEGEND_SITE("Легендарное место", MarkerGroup.DANGER, Glyph.ANOMALY),

    // Поселения — продолжение
    TRIBAL_CAMP("Племенное стойбище", MarkerGroup.SETTLEMENT, Glyph.TOTEM),
    HALFLING_SHIRE("Край полуросликов", MarkerGroup.SETTLEMENT, Glyph.VILLAGE, 0.9f),
    GNOME_WORKSHOP("Гномья мастерская", MarkerGroup.SETTLEMENT, Glyph.FORGE, 0.95f),
    MERFOLK_CITY("Город морского народа", MarkerGroup.SETTLEMENT, Glyph.SPIRE, 1.1f),
    SKY_CITY("Небесный город", MarkerGroup.SETTLEMENT, Glyph.AIRSHIP, 1.2f),
    CARAVAN_CITY("Караванный город", MarkerGroup.SETTLEMENT, Glyph.CITY, 1.1f),
    MONASTIC_TOWN("Монашеский город", MarkerGroup.SETTLEMENT, Glyph.TEMPLE),
    REFUGEE_CAMP("Лагерь беженцев", MarkerGroup.SETTLEMENT, Glyph.CAMP, 0.85f),

    // Крепости и война — продолжение
    SIGNAL_BEACON("Сигнальный костёр", MarkerGroup.MILITARY, Glyph.BEACON, 0.9f),
    WAR_BANNER("Ставка войска", MarkerGroup.MILITARY, Glyph.BANNER),
    DUEL_GROUND("Место поединков", MarkerGroup.MILITARY, Glyph.SWORD, 0.9f),
    TRAINING_GROUND("Ристалище", MarkerGroup.MILITARY, Glyph.ARENA, 0.95f),
    NAVAL_BASE("Военная гавань", MarkerGroup.MILITARY, Glyph.ANCHOR, 1.1f),
    DRAGON_GATE("Драконьи врата", MarkerGroup.MILITARY, Glyph.GATE, 1.15f),
    LAST_STAND("Место последней битвы", MarkerGroup.MILITARY, Glyph.SWORD),

    // Порты и переправы — продолжение
    SKY_DOCK("Причал воздушных кораблей", MarkerGroup.PORT, Glyph.AIRSHIP, 1.05f),
    UNDERWATER_GATE("Подводные врата", MarkerGroup.PORT, Glyph.SPIRE),
    RIVER_LOCK("Шлюз", MarkerGroup.PORT, Glyph.GATE, 0.85f),
    ROPE_BRIDGE("Верёвочный мост", MarkerGroup.PORT, Glyph.BRIDGE, 0.85f),

    // Торговля и знание — продолжение
    ARCHIVE("Архив", MarkerGroup.TRADE, Glyph.BOOK),
    SCRIPTORIUM("Скрипторий", MarkerGroup.TRADE, Glyph.BOOK, 0.9f),
    OBSERVATORY("Обсерватория", MarkerGroup.TRADE, Glyph.OBSERVATORY, 1.05f),
    BATH_HOUSE("Термы", MarkerGroup.TRADE, Glyph.WELL, 0.9f),
    FOUNTAIN_SQUARE("Площадь с фонтаном", MarkerGroup.TRADE, Glyph.WELL, 0.9f),
    THEATRE("Театр", MarkerGroup.TRADE, Glyph.ARENA, 0.95f),
    HERBALIST("Лавка травника", MarkerGroup.TRADE, Glyph.POTION, 0.8f),
    ROYAL_COURT("Королевский двор", MarkerGroup.TRADE, Glyph.CROWN, 1.15f),

    // Храмы и погребения — продолжение
    HERO_TOMB("Гробница героя", MarkerGroup.RELIGION, Glyph.STATUE),
    IDOL("Идол", MarkerGroup.RELIGION, Glyph.TOTEM, 0.9f),
    SACRED_SPRING("Священный источник", MarkerGroup.RELIGION, Glyph.WELL, 0.9f),
    BONE_FIELD("Костище", MarkerGroup.RELIGION, Glyph.BONES),
    COLOSSUS("Колосс", MarkerGroup.RELIGION, Glyph.STATUE, 1.3f),
    TRIUMPHAL_ARCH("Триумфальная арка", MarkerGroup.RELIGION, Glyph.ARCH, 1.05f),
    SKY_BURIAL("Небесное погребение", MarkerGroup.RELIGION, Glyph.BONES, 0.9f),

    // Магия — продолжение
    RUNE_STONE("Рунный камень", MarkerGroup.MAGIC, Glyph.RUNE_STONE, 0.9f),
    WITCH_CAULDRON("Ведьмин котёл", MarkerGroup.MAGIC, Glyph.CAULDRON, 0.9f),
    ALL_SEEING_EYE("Всевидящее око", MarkerGroup.MAGIC, Glyph.EYE),
    CRYSTAL_SPIRE("Кристальный шпиль", MarkerGroup.MAGIC, Glyph.CRYSTAL, 1.1f),
    FLOATING_ROCK("Парящая скала", MarkerGroup.MAGIC, Glyph.FLOATING_ROCK),
    SUMMONING_CIRCLE("Круг призыва", MarkerGroup.MAGIC, Glyph.STONE_CIRCLE),
    WISHING_WELL("Колодец желаний", MarkerGroup.MAGIC, Glyph.WELL, 0.85f),
    TIME_RIFT("Разлом времени", MarkerGroup.MAGIC, Glyph.ANOMALY, 1.1f),
    MANA_GEYSER("Гейзер силы", MarkerGroup.MAGIC, Glyph.GEYSER),
    ELEMENTAL_NODE("Узел стихий", MarkerGroup.MAGIC, Glyph.CRYSTAL),

    // Руины — продолжение
    FALLEN_TOWER("Павшая башня", MarkerGroup.RUIN, Glyph.RUINS),
    BURIED_CITY("Погребённый город", MarkerGroup.RUIN, Glyph.RUINS, 1.2f),
    GIANT_BONES("Кости исполина", MarkerGroup.RUIN, Glyph.BONES, 1.1f),
    DROWNED_TEMPLE("Затопленный храм", MarkerGroup.RUIN, Glyph.TEMPLE),
    FORGOTTEN_VAULT("Забытое хранилище", MarkerGroup.RUIN, Glyph.DUNGEON),
    BROKEN_ARCH("Разрушенная арка", MarkerGroup.RUIN, Glyph.ARCH, 0.95f),

    // Природа — продолжение
    GIANT_MUSHROOM("Гриб-исполин", MarkerGroup.NATURE, Glyph.MUSHROOM),
    DEAD_GROVE("Мёртвая роща", MarkerGroup.NATURE, Glyph.DEAD_TREE),
    FLOWER_FIELD("Цветочные поля", MarkerGroup.NATURE, Glyph.FLOWER, 0.9f),
    CRYSTAL_CAVE("Кристальная пещера", MarkerGroup.NATURE, Glyph.CRYSTAL),
    ICE_SPIRE("Ледяной шпиль", MarkerGroup.NATURE, Glyph.SPIRE),
    RAINBOW_FALLS("Радужный водопад", MarkerGroup.NATURE, Glyph.WATERFALL),
    SINGING_STONES("Поющие камни", MarkerGroup.NATURE, Glyph.STONE_CIRCLE),
    TAR_PIT("Смоляная яма", MarkerGroup.NATURE, Glyph.ROCK, 0.9f),
    MIRAGE("Мираж", MarkerGroup.NATURE, Glyph.ANOMALY, 0.9f),
    GREAT_GEODE("Великая жеода", MarkerGroup.NATURE, Glyph.CRYSTAL),

    // Ресурсы — продолжение
    MITHRIL_MINE("Мифриловые копи", MarkerGroup.RESOURCE, Glyph.MINE),
    CRYSTAL_QUARRY("Кристальная выработка", MarkerGroup.RESOURCE, Glyph.CRYSTAL, 0.95f),
    AMBER_SHORE("Янтарный берег", MarkerGroup.RESOURCE, Glyph.ROCK, 0.9f),
    PEARL_BEDS("Жемчужные отмели", MarkerGroup.RESOURCE, Glyph.ANCHOR, 0.85f),
    SPICE_GARDEN("Пряные сады", MarkerGroup.RESOURCE, Glyph.FLOWER, 0.85f),
    DYE_WORKS("Красильни", MarkerGroup.RESOURCE, Glyph.POTION, 0.85f),
    GLASSWORKS("Стеклодувня", MarkerGroup.RESOURCE, Glyph.POTION, 0.85f),
    PAPER_MILL("Бумажная мельница", MarkerGroup.RESOURCE, Glyph.MILL, 0.85f),
    STUD_FARM("Конный завод", MarkerGroup.RESOURCE, Glyph.BEAST, 0.9f),

    // Опасности — продолжение
    SPIDER_NEST("Паучье гнездо", MarkerGroup.DANGER, Glyph.NEST),
    WYVERN_ROOST("Логово виверн", MarkerGroup.DANGER, Glyph.DRAGON, 1.1f),
    KRAKEN_WATERS("Воды кракена", MarkerGroup.DANGER, Glyph.WHIRLPOOL, 1.1f),
    GIANT_LAIR("Логово великана", MarkerGroup.DANGER, Glyph.BONES, 1.1f),
    NECROMANCER_TOWER("Башня некроманта", MarkerGroup.DANGER, Glyph.TOWER, 1.1f),
    PLAGUE_VILLAGE("Чумная деревня", MarkerGroup.DANGER, Glyph.VILLAGE, 0.9f),
    CURSED_WELL("Проклятый колодец", MarkerGroup.DANGER, Glyph.WELL, 0.85f),
    SLAVER_PORT("Невольничий порт", MarkerGroup.DANGER, Glyph.ANCHOR),

    // Чудеса света
    WORLD_TREE("Мировое древо", MarkerGroup.WONDER, Glyph.TREE, 1.5f),
    GREAT_COLOSSUS("Великий колосс", MarkerGroup.WONDER, Glyph.STATUE, 1.4f),
    FLOATING_ISLAND("Парящий остров", MarkerGroup.WONDER, Glyph.FLOATING_ROCK, 1.3f),
    ETERNAL_FLAME("Вечный огонь", MarkerGroup.WONDER, Glyph.BEACON, 1.2f),
    SKY_BRIDGE("Небесный мост", MarkerGroup.WONDER, Glyph.BRIDGE, 1.25f),
    WORLD_GATE("Врата миров", MarkerGroup.WONDER, Glyph.PORTAL, 1.35f),
    CRYSTAL_THRONE("Кристальный трон", MarkerGroup.WONDER, Glyph.CROWN, 1.3f),
    STAR_OBSERVATORY("Звёздная обсерватория", MarkerGroup.WONDER, Glyph.OBSERVATORY, 1.25f),
    TITAN_REMAINS("Останки титана", MarkerGroup.WONDER, Glyph.BONES, 1.35f),
    ENDLESS_STAIR("Бесконечная лестница", MarkerGroup.WONDER, Glyph.SPIRE, 1.25f),
    GREAT_MAELSTROM("Великий мальстрём", MarkerGroup.WONDER, Glyph.WHIRLPOOL, 1.3f),

    // Звери и гнёзда
    DRAGON_ROOST("Драконье гнездовье", MarkerGroup.FAUNA, Glyph.DRAGON, 1.2f),
    GRIFFIN_CLIFFS("Утёсы грифонов", MarkerGroup.FAUNA, Glyph.NEST),
    PHOENIX_NEST("Гнездо феникса", MarkerGroup.FAUNA, Glyph.BEACON),
    UNICORN_GLADE("Поляна единорогов", MarkerGroup.FAUNA, Glyph.FLOWER),
    TREANT_GROVE("Роща древней", MarkerGroup.FAUNA, Glyph.TREE, 1.1f),
    FAIRY_RING("Круг фей", MarkerGroup.FAUNA, Glyph.MUSHROOM, 0.9f),
    WOLF_LANDS("Волчьи угодья", MarkerGroup.FAUNA, Glyph.BEAST),
    MAMMOTH_RANGE("Земли мамонтов", MarkerGroup.FAUNA, Glyph.BEAST, 1.1f),
    SEA_SERPENT("Воды морского змея", MarkerGroup.FAUNA, Glyph.WHIRLPOOL),
    GIANT_EAGLE_NEST("Гнездо орлов-исполинов", MarkerGroup.FAUNA, Glyph.NEST),
    HERD_GROUNDS("Пастбища диких стад", MarkerGroup.FAUNA, Glyph.BEAST, 0.95f),
    SPIRIT_BEAST("Обитель духа-зверя", MarkerGroup.FAUNA, Glyph.EYE, 1.1f),

    // Ресурсы и товары — знаки того, что здесь добывают
    RES_IRON("Железо", MarkerGroup.GOODS, Glyph.INGOT, 0.85f),
    RES_COPPER("Медь", MarkerGroup.GOODS, Glyph.INGOT, 0.85f),
    RES_TIN("Олово", MarkerGroup.GOODS, Glyph.INGOT, 0.85f),
    RES_GOLD("Золото", MarkerGroup.GOODS, Glyph.COINS, 0.85f),
    RES_SILVER("Серебро", MarkerGroup.GOODS, Glyph.COINS, 0.85f),
    RES_MITHRIL("Мифрил", MarkerGroup.GOODS, Glyph.MITHRIL, 0.9f),
    RES_GEMS("Самоцветы", MarkerGroup.GOODS, Glyph.GEM, 0.85f),
    RES_CRYSTALS("Кристаллы силы", MarkerGroup.GOODS, Glyph.GEM, 0.85f),
    RES_SALT("Соль", MarkerGroup.GOODS, Glyph.SALT, 0.85f),
    RES_COAL("Уголь", MarkerGroup.GOODS, Glyph.COAL, 0.85f),
    RES_SULFUR("Сера", MarkerGroup.GOODS, Glyph.SULFUR, 0.85f),
    RES_OBSIDIAN("Обсидиан", MarkerGroup.GOODS, Glyph.COAL, 0.85f),
    RES_MARBLE("Мрамор", MarkerGroup.GOODS, Glyph.MARBLE, 0.85f),
    RES_GRANITE("Гранит", MarkerGroup.GOODS, Glyph.STONE_BLOCKS, 0.85f),
    RES_CLAY("Глина", MarkerGroup.GOODS, Glyph.STONE_BLOCKS, 0.85f),
    RES_GLASS_SAND("Стекольный песок", MarkerGroup.GOODS, Glyph.SALT, 0.85f),
    RES_TIMBER("Корабельный лес", MarkerGroup.GOODS, Glyph.TIMBER, 0.85f),
    RES_GRAIN("Зерно", MarkerGroup.GOODS, Glyph.WHEAT, 0.85f),
    RES_RICE("Рис", MarkerGroup.GOODS, Glyph.WHEAT, 0.85f),
    RES_FRUIT("Фрукты", MarkerGroup.GOODS, Glyph.GRAPES, 0.85f),
    RES_WINE("Вино", MarkerGroup.GOODS, Glyph.GRAPES, 0.85f),
    RES_OLIVES("Оливки", MarkerGroup.GOODS, Glyph.GRAPES, 0.85f),
    RES_HONEY("Мёд", MarkerGroup.GOODS, Glyph.HERBS, 0.85f),
    RES_FISH("Рыба", MarkerGroup.GOODS, Glyph.FISH, 0.85f),
    RES_WHALE("Китовый промысел", MarkerGroup.GOODS, Glyph.FISH, 0.9f),
    RES_PEARLS("Жемчуг", MarkerGroup.GOODS, Glyph.PEARL, 0.85f),
    RES_AMBER("Янтарь", MarkerGroup.GOODS, Glyph.AMBER, 0.85f),
    RES_FURS("Пушнина", MarkerGroup.GOODS, Glyph.FUR, 0.85f),
    RES_LEATHER("Кожа", MarkerGroup.GOODS, Glyph.FUR, 0.85f),
    RES_WOOL("Шерсть", MarkerGroup.GOODS, Glyph.WOOL, 0.85f),
    RES_COTTON("Хлопок", MarkerGroup.GOODS, Glyph.WOOL, 0.85f),
    RES_SILK("Шёлк", MarkerGroup.GOODS, Glyph.SILK, 0.85f),
    RES_DYES("Красители", MarkerGroup.GOODS, Glyph.SPICE, 0.85f),
    RES_SPICES("Пряности", MarkerGroup.GOODS, Glyph.SPICE, 0.85f),
    RES_INCENSE("Благовония", MarkerGroup.GOODS, Glyph.SPICE, 0.85f),
    RES_HERBS("Целебные травы", MarkerGroup.GOODS, Glyph.HERBS, 0.85f),
    RES_REAGENTS("Магические реагенты", MarkerGroup.GOODS, Glyph.HERBS, 0.9f),
    RES_HORSES("Кони", MarkerGroup.GOODS, Glyph.HORSESHOE, 0.85f),
    RES_CATTLE("Скот", MarkerGroup.GOODS, Glyph.CATTLE, 0.85f),
    RES_IVORY("Бивни", MarkerGroup.GOODS, Glyph.CATTLE, 0.85f),
    RES_TAR("Смола", MarkerGroup.GOODS, Glyph.OIL, 0.85f),
    RES_OIL("Масло", MarkerGroup.GOODS, Glyph.OIL, 0.85f),

    // Переход на другую карту
    MAP_LINK("Подробная карта", MarkerGroup.ATLAS, Glyph.BOOK, 1.05f, MarkerScope.BOTH),

    // Ворота, башни и укрепления города
    CITY_MAIN_GATE("Главные ворота", MarkerGroup.CITY_WALLS, Glyph.GATE, 1.25f, MarkerScope.CITY),
    CITY_SIDE_GATE("Боковые ворота", MarkerGroup.CITY_WALLS, Glyph.GATE, 1f, MarkerScope.CITY),
    CITY_WATER_GATE("Водяные ворота", MarkerGroup.CITY_WALLS, Glyph.GATE, 1.05f, MarkerScope.CITY),
    CITY_POSTERN("Потайная калитка", MarkerGroup.CITY_WALLS, Glyph.GATE, 0.75f, MarkerScope.CITY),
    CITY_TRAITOR_GATE("Ворота изменников", MarkerGroup.CITY_WALLS, Glyph.GATE, 0.9f, MarkerScope.CITY),
    CITY_TRIUMPH_GATE("Триумфальные ворота", MarkerGroup.CITY_WALLS, Glyph.ARCH, 1.2f, MarkerScope.CITY),
    CITY_DRAWBRIDGE("Подъёмный мост", MarkerGroup.CITY_WALLS, Glyph.DRAWBRIDGE_GLYPH, 1.05f, MarkerScope.CITY),
    CITY_PORTCULLIS("Опускная решётка", MarkerGroup.CITY_WALLS, Glyph.PORTCULLIS, 1f, MarkerScope.CITY),
    CITY_BARBICAN("Барбакан", MarkerGroup.CITY_WALLS, Glyph.CASTLE, 1.2f, MarkerScope.CITY),
    CITY_CORNER_TOWER("Угловая башня", MarkerGroup.CITY_WALLS, Glyph.TOWER, 1.05f, MarkerScope.CITY),
    CITY_ROUND_TOWER("Круглая башня", MarkerGroup.CITY_WALLS, Glyph.TOWER, 1f, MarkerScope.CITY),
    CITY_SQUARE_TOWER("Квадратная башня", MarkerGroup.CITY_WALLS, Glyph.TOWER, 1f, MarkerScope.CITY),
    CITY_WATCH_TOWER("Дозорная вышка", MarkerGroup.CITY_WALLS, Glyph.TOWER, 0.9f, MarkerScope.CITY),
    CITY_SIGNAL_TOWER("Сигнальная башня", MarkerGroup.CITY_WALLS, Glyph.BEACON, 1f, MarkerScope.CITY),
    CITY_BASTION("Бастион", MarkerGroup.CITY_WALLS, Glyph.CASTLE, 1.15f, MarkerScope.CITY),
    CITY_WALL_BREACH("Пролом в стене", MarkerGroup.CITY_WALLS, Glyph.RUINS, 1f, MarkerScope.CITY),
    CITY_GUARD_POST("Пост стражи", MarkerGroup.CITY_WALLS, Glyph.SWORD, 0.85f, MarkerScope.CITY),
    CITY_TOLL_BAR("Шлагбаум", MarkerGroup.CITY_WALLS, Glyph.GATE, 0.8f, MarkerScope.CITY),

    // Улица и площадь
    CITY_WELL("Колодец", MarkerGroup.CITY_STREET, Glyph.WELL, 0.9f, MarkerScope.CITY),
    CITY_FOUNTAIN("Фонтан", MarkerGroup.CITY_STREET, Glyph.FOUNTAIN, 1f, MarkerScope.CITY),
    CITY_PUMP("Водоразборная колонка", MarkerGroup.CITY_STREET, Glyph.WELL, 0.75f, MarkerScope.CITY),
    CITY_STATUE("Статуя", MarkerGroup.CITY_STREET, Glyph.STATUE, 1f, MarkerScope.CITY),
    CITY_HORSE_STATUE("Конная статуя", MarkerGroup.CITY_STREET, Glyph.STATUE, 1.1f, MarkerScope.CITY),
    CITY_MONUMENT("Памятная стела", MarkerGroup.CITY_STREET, Glyph.OBELISK, 1f, MarkerScope.CITY),
    CITY_LANTERN("Фонарный столб", MarkerGroup.CITY_STREET, Glyph.LANTERN, 0.7f, MarkerScope.CITY),
    CITY_SIGNPOST("Указатель", MarkerGroup.CITY_STREET, Glyph.SIGNPOST, 0.75f, MarkerScope.CITY),
    CITY_CLOCK("Городские часы", MarkerGroup.CITY_STREET, Glyph.CLOCK, 0.95f, MarkerScope.CITY),
    CITY_SUNDIAL("Солнечные часы", MarkerGroup.CITY_STREET, Glyph.CLOCK, 0.8f, MarkerScope.CITY),
    CITY_BELL("Колокол", MarkerGroup.CITY_STREET, Glyph.BELL, 0.9f, MarkerScope.CITY),
    CITY_STALL("Торговый лоток", MarkerGroup.CITY_STREET, Glyph.STALL, 0.85f, MarkerScope.CITY),
    CITY_TENT("Торговый шатёр", MarkerGroup.CITY_STREET, Glyph.CAMP, 0.9f, MarkerScope.CITY),
    CITY_CART("Телега", MarkerGroup.CITY_STREET, Glyph.CART, 0.8f, MarkerScope.CITY),
    CITY_BARRELS("Бочки", MarkerGroup.CITY_STREET, Glyph.BARREL, 0.75f, MarkerScope.CITY),
    CITY_WOODPILE("Поленница", MarkerGroup.CITY_STREET, Glyph.TIMBER, 0.75f, MarkerScope.CITY),
    CITY_BONFIRE("Костёр", MarkerGroup.CITY_STREET, Glyph.BEACON, 0.8f, MarkerScope.CITY),
    CITY_TREE("Дерево на площади", MarkerGroup.CITY_STREET, Glyph.TREE, 0.95f, MarkerScope.CITY),
    CITY_FLOWERBED("Клумба", MarkerGroup.CITY_STREET, Glyph.FLOWER, 0.75f, MarkerScope.CITY),
    CITY_HITCHING("Коновязь", MarkerGroup.CITY_STREET, Glyph.HORSESHOE, 0.75f, MarkerScope.CITY),
    CITY_NOTICE("Доска объявлений", MarkerGroup.CITY_STREET, Glyph.PLAQUE, 0.8f, MarkerScope.CITY),

    // Городские службы
    CITY_PILLORY("Позорный столб", MarkerGroup.CITY_SERVICE, Glyph.PILLORY, 0.85f, MarkerScope.CITY),
    CITY_GALLOWS("Виселица", MarkerGroup.CITY_SERVICE, Glyph.GALLOWS, 0.95f, MarkerScope.CITY),
    CITY_BLOCK("Плаха", MarkerGroup.CITY_SERVICE, Glyph.STONE_BLOCKS, 0.8f, MarkerScope.CITY),
    CITY_STOCKS("Колодки", MarkerGroup.CITY_SERVICE, Glyph.PILLORY, 0.75f, MarkerScope.CITY),
    CITY_HERALD("Помост глашатая", MarkerGroup.CITY_SERVICE, Glyph.PLAQUE, 0.85f, MarkerScope.CITY),
    CITY_DECREE("Доска указов", MarkerGroup.CITY_SERVICE, Glyph.PLAQUE, 0.8f, MarkerScope.CITY),
    CITY_WEIGH("Городские весы", MarkerGroup.CITY_SERVICE, Glyph.MARKET, 0.85f, MarkerScope.CITY),
    CITY_SEWER("Сточная канава", MarkerGroup.CITY_SERVICE, Glyph.HATCH, 0.8f, MarkerScope.CITY),
    CITY_SEWER_HATCH("Люк в подземелье", MarkerGroup.CITY_SERVICE, Glyph.HATCH, 0.8f, MarkerScope.CITY),
    CITY_DUMP("Мусорная куча", MarkerGroup.CITY_SERVICE, Glyph.ROCK, 0.8f, MarkerScope.CITY),
    CITY_PIER("Причал", MarkerGroup.CITY_SERVICE, Glyph.ANCHOR, 0.9f, MarkerScope.CITY),
    CITY_BOAT("Лодки", MarkerGroup.CITY_SERVICE, Glyph.SHIP, 0.8f, MarkerScope.CITY),
    CITY_FERRY("Городская переправа", MarkerGroup.CITY_SERVICE, Glyph.SHIP, 0.85f, MarkerScope.CITY),
    CITY_FOOTBRIDGE("Мостки", MarkerGroup.CITY_SERVICE, Glyph.BRIDGE, 0.8f, MarkerScope.CITY),
    CITY_MILESTONE("Верстовой столб", MarkerGroup.CITY_SERVICE, Glyph.OBELISK, 0.7f, MarkerScope.CITY),
    CITY_DOGHOUSE("Псарня", MarkerGroup.CITY_SERVICE, Glyph.BEAST, 0.75f, MarkerScope.CITY),
    CITY_COOP("Курятник", MarkerGroup.CITY_SERVICE, Glyph.NEST, 0.7f, MarkerScope.CITY),
    CITY_PIGSTY("Свинарник", MarkerGroup.CITY_SERVICE, Glyph.CATTLE, 0.75f, MarkerScope.CITY),
    CITY_GARDEN_PLOT("Огород", MarkerGroup.CITY_SERVICE, Glyph.FARM, 0.8f, MarkerScope.CITY),

    // Особые места города
    CITY_FOUNDER("Статуя основателя", MarkerGroup.CITY_SPECIAL, Glyph.STATUE, 1.2f, MarkerScope.CITY),
    CITY_DRAGON_FOUNTAIN("Фонтан с драконом", MarkerGroup.CITY_SPECIAL, Glyph.FOUNTAIN, 1.15f, MarkerScope.CITY),
    CITY_WISH_STONE("Камень желаний", MarkerGroup.CITY_SPECIAL, Glyph.RUNE_STONE, 0.9f, MarkerScope.CITY),
    CITY_HOLY_SPRING("Чудотворный источник", MarkerGroup.CITY_SPECIAL, Glyph.WELL, 0.95f, MarkerScope.CITY),
    CITY_CROSSROAD_ALTAR("Алтарь на перекрёстке", MarkerGroup.CITY_SPECIAL, Glyph.OBELISK, 0.85f, MarkerScope.CITY),
    CITY_MEMORIAL("Поминальный камень", MarkerGroup.CITY_SPECIAL, Glyph.GRAVE, 0.85f, MarkerScope.CITY),
    CITY_PLAGUE_PIT("Чумная яма", MarkerGroup.CITY_SPECIAL, Glyph.BONES, 1f, MarkerScope.CITY),
    CITY_HANGING_TREE("Дерево висельников", MarkerGroup.CITY_SPECIAL, Glyph.DEAD_TREE, 1f, MarkerScope.CITY),
    CITY_DUEL_GROUND("Площадка для поединков", MarkerGroup.CITY_SPECIAL, Glyph.SWORD, 0.95f, MarkerScope.CITY),
    CITY_CATACOMB_ENTRY("Вход в катакомбы", MarkerGroup.CITY_SPECIAL, Glyph.DUNGEON, 1f, MarkerScope.CITY),
    CITY_SECRET_WAY("Тайный лаз", MarkerGroup.CITY_SPECIAL, Glyph.HATCH, 0.85f, MarkerScope.CITY),
    CITY_GHOST_GATE("Ворота призраков", MarkerGroup.CITY_SPECIAL, Glyph.PORTAL, 1.1f, MarkerScope.CITY),
    CITY_CURSED_HOUSE("Проклятый дом", MarkerGroup.CITY_SPECIAL, Glyph.ANOMALY, 1f, MarkerScope.CITY),
    CITY_THIEVES_MARK("Метка воров", MarkerGroup.CITY_SPECIAL, Glyph.EYE, 0.8f, MarkerScope.CITY),
    CITY_GUILD_SIGN("Знак гильдии", MarkerGroup.CITY_SPECIAL, Glyph.BANNER, 0.85f, MarkerScope.CITY),
    CITY_LEY_STONE("Камень силы", MarkerGroup.CITY_SPECIAL, Glyph.CRYSTAL, 0.95f, MarkerScope.CITY),
    CITY_SLAVE_BLOCK("Помост работорговца", MarkerGroup.CITY_SPECIAL, Glyph.PILLORY, 0.9f, MarkerScope.CITY),
    CITY_FAIR_GROUND("Ярмарочный помост", MarkerGroup.CITY_SPECIAL, Glyph.STALL, 0.95f, MarkerScope.CITY),
    CITY_BEAR_PIT("Медвежья яма", MarkerGroup.CITY_SPECIAL, Glyph.MONSTER, 1f, MarkerScope.CITY),
    CITY_PUPPET_STAGE("Балаган", MarkerGroup.CITY_SPECIAL, Glyph.ARENA, 0.9f, MarkerScope.CITY);

    val isSettlement: Boolean
        get() = group == MarkerGroup.SETTLEMENT

    /** Уместен ли объект на карте такого вида. */
    fun fits(kind: MapKind): Boolean = when (scope) {
        MarkerScope.BOTH -> true
        MarkerScope.CITY -> kind == MapKind.CITY
        MarkerScope.WORLD -> kind == MapKind.WORLD
    }

    companion object {
        fun byGroup(group: MarkerGroup): List<MarkerType> = MarkerType.entries.filter { it.group == group }

        /** Объекты этой группы, уместные на карте такого вида. */
        fun byGroup(group: MarkerGroup, kind: MapKind): List<MarkerType> =
            MarkerType.entries.filter { it.group == group && it.fits(kind) }

        /** Группы, в которых есть хоть один объект для такой карты. */
        fun groupsFor(kind: MapKind): List<MarkerGroup> =
            MarkerGroup.entries.filter { group -> MarkerType.entries.any { it.group == group && it.fits(kind) } }
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
    LAVA_FLOW("Лавовый поток", 0xFFB4502E.toInt(), 4f),
    LEY_LINE("Линия силы", 0xFF7B62A8.toInt(), 3f),
    CHASM("Расселина", 0xFF4E453C.toInt(), 5f),
    AQUEDUCT("Акведук", 0xFF9A8E76.toInt(), 4f),
    ICE_RIDGE("Ледяная гряда", 0xFF9FC4D8.toInt(), 8f),
    SAND_RIDGE("Барханная гряда", 0xFFD9C08A.toInt(), 8f),
    ROOT_WALL("Стена корней", 0xFF6B5433.toInt(), 8f),
    CORAL_WALL("Коралловая гряда", 0xFF62B0A4.toInt(), 6f),
    MIGRATION_PATH("Тропа зверей", 0xFF8A7B5C.toInt(), 2.5f),
    CITY_WALL("Городская стена", 0xFF6E6558.toInt(), 6f),
    INNER_WALL("Внутренняя стена", 0xFF7A7164.toInt(), 4f),
    PALISADE("Частокол", 0xFF7E6A4E.toInt(), 3f),
    MOAT("Ров", 0xFF5E7E92.toInt(), 7f),
    EMBANKMENT("Земляной вал", 0xFF8E7E62.toInt(), 8f),
    HIGH_WALL("Высокая стена", 0xFF655C50.toInt(), 8f),
    DOUBLE_WALL("Двойная стена", 0xFF6A6154.toInt(), 10f),
    TOWERED_WALL("Стена с башнями", 0xFF6E6558.toInt(), 7f),
    RUINED_WALL("Разрушенная стена", 0xFF867C6E.toInt(), 5f),
    WOODEN_WALL("Бревенчатая стена", 0xFF7E6A4E.toInt(), 4f),
    DRY_MOAT("Сухой ров", 0xFF8A7E68.toInt(), 6f),
    SPIKE_DITCH("Ров с кольями", 0xFF7E7058.toInt(), 6f),
    HEDGE_WALL("Живая изгородь", 0xFF5E8050.toInt(), 5f),
    MAGIC_WARD("Магический барьер", 0xFF8E7CC0.toInt(), 5f),
    CITY_AQUEDUCT("Городской акведук", 0xFF9A8E76.toInt(), 5f)
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
    SECRET_PATH("Тайная тропа", 0xFF6B5A6E.toInt(), 1.6f, true),
    ROYAL_ROAD("Королевский тракт", 0xFF8A5E2E.toInt(), 4f, false),
    PILGRIM_ROAD("Паломничий путь", 0xFF97814F.toInt(), 2f, true),
    SKY_ROUTE("Воздушный путь", 0xFF6E86A8.toInt(), 2f, true),
    UNDERGROUND_ROAD("Подземный ход", 0xFF5E5148.toInt(), 2f, true),
    RIVER_ROUTE("Речной путь", 0xFF4A7E9B.toInt(), 2f, true),
    MAIN_STREET("Главная улица", 0xFF9C8A6E.toInt(), 7f, false),
    STREET("Улица", 0xFFA3937A.toInt(), 5f, false),
    LANE("Переулок", 0xFFAB9C84.toInt(), 3f, false),
    ALLEY("Закоулок", 0xFFB0A28C.toInt(), 2f, true),
    STAIRS_WAY("Лестница", 0xFF988A74.toInt(), 3f, true),
    WATERFRONT("Набережная", 0xFF93968A.toInt(), 5f, false),
    CITY_CANAL_WAY("Городской канал", 0xFF5E92B0.toInt(), 6f, false)
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
    BAY("Залив", 0xFF5A94BC.toInt()),
    CRATER_LAKE("Кратерное озеро", 0xFF4E7E9E.toInt()),
    GLACIAL_LAKE("Ледниковое озеро", 0xFF86B6CE.toInt()),
    ENCHANTED_LAKE("Зачарованное озеро", 0xFF5E8FA8.toInt()),
    MARSH_WATER("Заболоченная вода", 0xFF6E8878.toInt()),
    HOT_LAKE("Горячее озеро", 0xFF7E93A0.toInt())
}

/** Стиль подписи на карте. */
enum class LabelStyle(val title: String, val size: Float, val color: Int, val italic: Boolean) {
    TITLE("Название мира", 34f, 0xFF3A2E22.toInt(), false),
    REGION("Область", 22f, 0xFF4A3B2C.toInt(), false),
    WATER("Море и вода", 20f, 0xFF2F5F7E.toInt(), true),
    MOUNTAIN("Горы", 18f, 0xFF564E45.toInt(), false),
    SMALL("Мелкая подпись", 14f, 0xFF4A3B2C.toInt(), true)
}

/** Шаг работы над картой — общий для карты мира и карты города. */
interface MapStage {
    val number: Int
    val title: String
    val hint: String
}

/** Вид карты: мир целиком или один город вблизи. */
enum class MapKind(val title: String, val hint: String) {
    WORLD("Карта мира", "материки, страны, города на всём материке"),
    CITY("Карта города", "улицы, кварталы и отдельные дома одного города")
}

/** Этапы создания карты — порядок работы над миром. */
enum class Stage(
    override val number: Int,
    override val title: String,
    override val hint: String
) : MapStage {
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

/** Этапы создания карты города. */
enum class CityStage(
    override val number: Int,
    override val title: String,
    override val hint: String
) : MapStage {
    GROUND(1, "Земля и вода", "Обведите землю, на которой стоит город: берег реки, остров, озеро, гавань."),
    WALLS(2, "Стены и ворота", "Проведите городские стены и ров, поставьте ворота и башни."),
    DISTRICTS(3, "Кварталы", "Разметьте районы: торговый, ремесленный, храмовый, богатый, трущобы."),
    STREETS(4, "Улицы и площади", "Проведите главные улицы, переулки и мосты."),
    BUILDINGS(5, "Здания", "Ратуша, соборы, лавки, мастерские и жилые дома. Квартал можно застроить целиком."),
    GREEN(6, "Сады и кладбища", "Парки, сады, огороды, рощи, кладбища, площади."),
    DETAILS(7, "Мелочи города", "Колодцы, фонтаны, статуи, виселицы, вывески, мосты и пристани."),
    CITY_INFO(8, "Названия и описание", "Подпишите улицы, кварталы и здания, заполните описание города.");

    companion object {
        fun byNumber(number: Int): CityStage =
            CityStage.entries.firstOrNull { it.number == number } ?: GROUND
    }
}

/** Все шаги для выбранного вида карты. */
fun stagesFor(kind: MapKind): List<MapStage> = when (kind) {
    MapKind.WORLD -> Stage.entries.toList()
    MapKind.CITY -> CityStage.entries.toList()
}

fun stageFor(kind: MapKind, number: Int): MapStage = when (kind) {
    MapKind.WORLD -> Stage.byNumber(number)
    MapKind.CITY -> CityStage.byNumber(number)
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
    ERASER("Стереть", "🧽"),
    FRAGMENT("Выделить область", "⧉"),
    BUILDING("Здание", "🏠"),
    DISTRICT("Квартал", "▦")
}

/** Каким по форме рисуется дом. */
enum class BuildingShape(val widthScale: Float, val depthScale: Float) {
    NORMAL(1f, 0.78f),
    WIDE(1.35f, 0.72f),
    LONG(2.1f, 0.6f),
    TOWER(0.72f, 0.72f),
    HALL(1.55f, 1.1f)
}

/** Группы городских построек. */
enum class BuildingGroup(val title: String) {
    TOWERS("Башни и ворота"),
    HOME("Жильё"),
    POWER("Власть и порядок"),
    FAITH("Вера"),
    TRADE("Торговля"),
    CRAFT("Ремёсла"),
    KNOWLEDGE("Знание и лекарство"),
    FUN("Отдых и зрелища"),
    SERVICE("Городские службы"),
    MAGIC_HOUSE("Магия"),
    RUIN_HOUSE("Заброшенное")
}

/**
 * Вид городской постройки.
 * color — цвет крыши, roof — как рисуется крыша, mark — знак на здании.
 */
enum class BuildingType(
    val title: String,
    val group: BuildingGroup,
    val color: Int,
    val mark: Glyph? = null,
    val big: Boolean = false,
    val shape: BuildingShape = BuildingShape.NORMAL
) {
    // Жильё
    HUT("Лачуга", BuildingGroup.HOME, 0xFF9C8460.toInt()),
    HOUSE("Дом горожанина", BuildingGroup.HOME, 0xFFB08A5E.toInt()),
    TALL_HOUSE("Высокий дом", BuildingGroup.HOME, 0xFFA87F58.toInt()),
    RICH_HOUSE("Богатый дом", BuildingGroup.HOME, 0xFFC09A62.toInt()),
    MANOR("Особняк", BuildingGroup.HOME, 0xFFC8A46C.toInt(), null, true, shape = BuildingShape.HALL),
    TENEMENT("Доходный дом", BuildingGroup.HOME, 0xFF9E8358.toInt(), shape = BuildingShape.LONG),
    TOWER_HOUSE("Дом-башня", BuildingGroup.HOME, 0xFFA98B66.toInt(), Glyph.TOWER, shape = BuildingShape.TOWER),
    FARMSTEAD_HOUSE("Усадьба с двором", BuildingGroup.HOME, 0xFFB6976A.toInt(), null, true, shape = BuildingShape.HALL),
    SHACK_ROW("Ряд бараков", BuildingGroup.HOME, 0xFF8F7A58.toInt(), shape = BuildingShape.LONG),

    // Власть
    TOWN_HALL("Ратуша", BuildingGroup.POWER, 0xFF9A6E4A.toInt(), Glyph.BANNER, true, shape = BuildingShape.HALL),
    PALACE("Дворец", BuildingGroup.POWER, 0xFFB07A4E.toInt(), Glyph.CROWN, true, shape = BuildingShape.HALL),
    KEEP("Донжон", BuildingGroup.POWER, 0xFF7E6B58.toInt(), Glyph.CASTLE, true, shape = BuildingShape.HALL),
    GUARD_HOUSE("Дом стражи", BuildingGroup.POWER, 0xFF8A7256.toInt(), Glyph.SWORD),
    BARRACKS_HOUSE("Казармы", BuildingGroup.POWER, 0xFF87725A.toInt(), Glyph.SWORD, true, shape = BuildingShape.LONG),
    COURT("Суд", BuildingGroup.POWER, 0xFF9E7C56.toInt(), Glyph.BOOK, shape = BuildingShape.WIDE),
    PRISON_HOUSE("Тюрьма", BuildingGroup.POWER, 0xFF6E6154.toInt(), Glyph.GATE),
    CUSTOMS("Таможня", BuildingGroup.POWER, 0xFF97764F.toInt(), Glyph.COINS),
    MINT_HOUSE("Монетный двор", BuildingGroup.POWER, 0xFFA5824F.toInt(), Glyph.COINS),
    GUILD_HOUSE("Дом гильдии", BuildingGroup.POWER, 0xFFA07C53.toInt(), Glyph.BANNER, shape = BuildingShape.WIDE),

    // Вера
    CHAPEL("Часовня", BuildingGroup.FAITH, 0xFF8E8FA0.toInt(), Glyph.TEMPLE, shape = BuildingShape.NORMAL),
    CHURCH("Церковь", BuildingGroup.FAITH, 0xFF8288A0.toInt(), Glyph.TEMPLE, true, shape = BuildingShape.HALL),
    CATHEDRAL_HOUSE("Собор", BuildingGroup.FAITH, 0xFF7B85A6.toInt(), Glyph.TEMPLE, true, shape = BuildingShape.HALL),
    MONASTERY_HOUSE("Монастырь", BuildingGroup.FAITH, 0xFF8A8C96.toInt(), Glyph.TEMPLE, true, shape = BuildingShape.HALL),
    SHRINE_HOUSE("Святилище", BuildingGroup.FAITH, 0xFF98939E.toInt(), Glyph.OBELISK),
    OLD_TEMPLE("Старый храм", BuildingGroup.FAITH, 0xFF8E8778.toInt(), Glyph.TEMPLE),
    CRYPT_HOUSE("Склеп", BuildingGroup.FAITH, 0xFF7E7A72.toInt(), Glyph.GRAVE, shape = BuildingShape.TOWER),

    // Торговля
    SHOP("Лавка", BuildingGroup.TRADE, 0xFFB79357.toInt()),
    MARKET_HALL("Торговые ряды", BuildingGroup.TRADE, 0xFFC0A05E.toInt(), Glyph.MARKET, true, shape = BuildingShape.HALL),
    WAREHOUSE("Склад", BuildingGroup.TRADE, 0xFF9C8A62.toInt(), shape = BuildingShape.LONG),
    BANK_HOUSE("Меняльная контора", BuildingGroup.TRADE, 0xFFAE8E58.toInt(), Glyph.COINS),
    INN_HOUSE("Постоялый двор", BuildingGroup.TRADE, 0xFFB5915C.toInt(), Glyph.INN_SIGN, true, shape = BuildingShape.WIDE),
    TAVERN("Таверна", BuildingGroup.TRADE, 0xFFB08B58.toInt(), Glyph.INN_SIGN),
    BAKERY("Пекарня", BuildingGroup.TRADE, 0xFFBE9A5E.toInt(), Glyph.WHEAT),
    BUTCHER("Мясная лавка", BuildingGroup.TRADE, 0xFFA87E5A.toInt(), Glyph.CATTLE),
    FISH_MARKET("Рыбный ряд", BuildingGroup.TRADE, 0xFF9FA07A.toInt(), Glyph.FISH),
    SPICE_SHOP("Лавка пряностей", BuildingGroup.TRADE, 0xFFB78F60.toInt(), Glyph.SPICE),
    CARAVAN_YARD("Караванный двор", BuildingGroup.TRADE, 0xFFAE9060.toInt(), Glyph.HORSESHOE, true, shape = BuildingShape.HALL),

    // Ремёсла
    SMITHY("Кузница", BuildingGroup.CRAFT, 0xFF8A7360.toInt(), Glyph.FORGE, shape = BuildingShape.WIDE),
    ARMOURER("Оружейная", BuildingGroup.CRAFT, 0xFF897462.toInt(), Glyph.SWORD),
    POTTERY("Гончарня", BuildingGroup.CRAFT, 0xFFA98262.toInt(), Glyph.POTION),
    TANNERY("Дубильня", BuildingGroup.CRAFT, 0xFF937C5C.toInt(), Glyph.FUR, shape = BuildingShape.WIDE),
    WEAVER("Ткацкая", BuildingGroup.CRAFT, 0xFFAE9068.toInt(), Glyph.SILK),
    DYER("Красильня", BuildingGroup.CRAFT, 0xFFA07C70.toInt(), Glyph.SPICE),
    CARPENTER("Плотницкая", BuildingGroup.CRAFT, 0xFFA98A5E.toInt(), Glyph.TIMBER),
    GLASSBLOWER("Стеклодувня", BuildingGroup.CRAFT, 0xFF93998E.toInt(), Glyph.POTION),
    JEWELLER("Ювелир", BuildingGroup.CRAFT, 0xFFBE9C5E.toInt(), Glyph.GEM),
    MILL_HOUSE("Мельница", BuildingGroup.CRAFT, 0xFFB09068.toInt(), Glyph.MILL, shape = BuildingShape.TOWER),
    BREWERY("Пивоварня", BuildingGroup.CRAFT, 0xFFAD8B5A.toInt(), Glyph.GRAPES, shape = BuildingShape.WIDE),
    SHIPYARD_HOUSE("Верфь", BuildingGroup.CRAFT, 0xFF94836A.toInt(), Glyph.SHIP, true, shape = BuildingShape.LONG),
    STONECUTTER("Камнерезная", BuildingGroup.CRAFT, 0xFF9A9088.toInt(), Glyph.STONE_BLOCKS),

    // Знание и лекарство
    LIBRARY_HOUSE("Библиотека", BuildingGroup.KNOWLEDGE, 0xFF8E8468.toInt(), Glyph.BOOK, true, shape = BuildingShape.HALL),
    SCHOOL("Школа", BuildingGroup.KNOWLEDGE, 0xFF9C8E6C.toInt(), Glyph.BOOK, shape = BuildingShape.WIDE),
    UNIVERSITY_HOUSE("Университет", BuildingGroup.KNOWLEDGE, 0xFF93876A.toInt(), Glyph.BOOK, true, shape = BuildingShape.HALL),
    OBSERVATORY_HOUSE("Обсерватория", BuildingGroup.KNOWLEDGE, 0xFF8A8C9C.toInt(), Glyph.OBSERVATORY, shape = BuildingShape.TOWER),
    HEALER("Лекарня", BuildingGroup.KNOWLEDGE, 0xFFA69882.toInt(), Glyph.HERBS),
    HOSPITAL("Госпиталь", BuildingGroup.KNOWLEDGE, 0xFFA0947E.toInt(), Glyph.HERBS, true, shape = BuildingShape.LONG),
    APOTHECARY("Аптека", BuildingGroup.KNOWLEDGE, 0xFFA99A76.toInt(), Glyph.POTION),

    // Отдых и зрелища
    BATH_HOUSE_CITY("Бани", BuildingGroup.FUN, 0xFF93A0A4.toInt(), Glyph.WELL, shape = BuildingShape.WIDE),
    THEATRE_HOUSE("Театр", BuildingGroup.FUN, 0xFFA98C74.toInt(), Glyph.ARENA, true, shape = BuildingShape.HALL),
    ARENA_HOUSE("Арена", BuildingGroup.FUN, 0xFFA1906E.toInt(), Glyph.ARENA, true, shape = BuildingShape.HALL),
    GAMBLING_DEN("Игорный дом", BuildingGroup.FUN, 0xFF9C7E66.toInt(), Glyph.COINS),
    PLEASURE_HOUSE("Дом утех", BuildingGroup.FUN, 0xFFB0808A.toInt()),
    MUSIC_HALL("Дом музыки", BuildingGroup.FUN, 0xFFA98E84.toInt()),

    // Городские службы
    STABLE("Конюшня", BuildingGroup.SERVICE, 0xFF9E8760.toInt(), Glyph.HORSESHOE, shape = BuildingShape.LONG),
    CART_YARD("Каретный двор", BuildingGroup.SERVICE, 0xFF9A8A6A.toInt(), Glyph.HORSESHOE, shape = BuildingShape.LONG),
    DOCK_HOUSE("Пакгауз у причала", BuildingGroup.SERVICE, 0xFF8E8874.toInt(), Glyph.ANCHOR, shape = BuildingShape.LONG),
    GRANARY("Амбар", BuildingGroup.SERVICE, 0xFFB29868.toInt(), Glyph.WHEAT, shape = BuildingShape.LONG),
    WATER_HOUSE("Водокачка", BuildingGroup.SERVICE, 0xFF8FA0A6.toInt(), Glyph.WELL),
    GATE_HOUSE("Надвратная башня", BuildingGroup.SERVICE, 0xFF867A6A.toInt(), Glyph.GATE, shape = BuildingShape.WIDE),
    WALL_TOWER("Башня стены", BuildingGroup.SERVICE, 0xFF7E7468.toInt(), Glyph.TOWER, shape = BuildingShape.TOWER),
    LIGHTHOUSE_HOUSE("Маяк", BuildingGroup.SERVICE, 0xFF8C9298.toInt(), Glyph.LIGHTHOUSE, shape = BuildingShape.TOWER),
    DOVECOTE("Голубятня", BuildingGroup.SERVICE, 0xFFA2937A.toInt(), Glyph.NEST),
    GALLOWS_YARD("Помост с виселицей", BuildingGroup.SERVICE, 0xFF7E7266.toInt(), Glyph.GRAVE),

    // Магия
    WIZARD_HOUSE("Башня мага", BuildingGroup.MAGIC_HOUSE, 0xFF7E7496.toInt(), Glyph.TOWER, true, shape = BuildingShape.TOWER),
    MAGE_SCHOOL("Академия магии", BuildingGroup.MAGIC_HOUSE, 0xFF7A7A9E.toInt(), Glyph.BOOK, true, shape = BuildingShape.HALL),
    ALCHEMIST_HOUSE("Алхимик", BuildingGroup.MAGIC_HOUSE, 0xFF88849C.toInt(), Glyph.POTION),
    ENCHANTER("Чародейная лавка", BuildingGroup.MAGIC_HOUSE, 0xFF8E86A4.toInt(), Glyph.CRYSTAL),
    SEER_HOUSE("Дом прорицателя", BuildingGroup.MAGIC_HOUSE, 0xFF92869C.toInt(), Glyph.EYE),

    // Заброшенное
    BURNT_HOUSE("Сгоревший дом", BuildingGroup.RUIN_HOUSE, 0xFF6E6358.toInt()),
    RUINED_HOUSE("Развалины", BuildingGroup.RUIN_HOUSE, 0xFF7E7668.toInt(), Glyph.RUINS),
    ABANDONED_HOUSE("Заброшенный дом", BuildingGroup.RUIN_HOUSE, 0xFF877E70.toInt()),
    PLAGUE_HOUSE("Чумной дом", BuildingGroup.RUIN_HOUSE, 0xFF7A7264.toInt(), Glyph.GRAVE),

    // Башни и ворота
    CORNER_TOWER("Угловая башня", BuildingGroup.TOWERS, 0xFF7A7166.toInt(), Glyph.TOWER, shape = BuildingShape.TOWER),
    ROUND_TOWER("Круглая башня", BuildingGroup.TOWERS, 0xFF807668.toInt(), Glyph.TOWER, shape = BuildingShape.TOWER),
    SQUARE_TOWER("Квадратная башня", BuildingGroup.TOWERS, 0xFF7E7568.toInt(), Glyph.TOWER, shape = BuildingShape.TOWER),
    WATCH_TOWER_HOUSE("Дозорная башня", BuildingGroup.TOWERS, 0xFF84796A.toInt(), Glyph.TOWER, shape = BuildingShape.TOWER),
    CLOCK_TOWER("Часовая башня", BuildingGroup.TOWERS, 0xFF8A7E6E.toInt(), Glyph.CLOCK, shape = BuildingShape.TOWER),
    BELL_TOWER("Колокольня", BuildingGroup.TOWERS, 0xFF8E8676.toInt(), Glyph.BELL, shape = BuildingShape.TOWER),
    SIGNAL_TOWER("Сигнальная башня", BuildingGroup.TOWERS, 0xFF7E7264.toInt(), Glyph.BEACON, shape = BuildingShape.TOWER),
    WATER_TOWER("Водонапорная башня", BuildingGroup.TOWERS, 0xFF8494A0.toInt(), Glyph.WELL, shape = BuildingShape.TOWER),
    DOVE_TOWER("Голубиная башня", BuildingGroup.TOWERS, 0xFF9A8E7A.toInt(), Glyph.NEST, shape = BuildingShape.TOWER),
    PRISON_TOWER("Тюремная башня", BuildingGroup.TOWERS, 0xFF6E655A.toInt(), Glyph.GATE, shape = BuildingShape.TOWER),
    ASTROLOGER_TOWER("Башня астролога", BuildingGroup.TOWERS, 0xFF7C7898.toInt(), Glyph.OBSERVATORY, shape = BuildingShape.TOWER),
    BEACON_TOWER("Башня-маяк", BuildingGroup.TOWERS, 0xFF8C9298.toInt(), Glyph.LIGHTHOUSE, shape = BuildingShape.TOWER),
    RUINED_TOWER("Разрушенная башня", BuildingGroup.TOWERS, 0xFF877E70.toInt(), Glyph.RUINS, shape = BuildingShape.TOWER),
    MAIN_GATE_HOUSE("Главные ворота", BuildingGroup.TOWERS, 0xFF7A7064.toInt(), Glyph.GATE, true, BuildingShape.WIDE),
    SIDE_GATE_HOUSE("Боковые ворота", BuildingGroup.TOWERS, 0xFF827868.toInt(), Glyph.GATE, false, BuildingShape.WIDE),
    WATER_GATE_HOUSE("Водяные ворота", BuildingGroup.TOWERS, 0xFF78808A.toInt(), Glyph.GATE, false, BuildingShape.WIDE),
    BARBICAN_HOUSE("Барбакан", BuildingGroup.TOWERS, 0xFF756C60.toInt(), Glyph.CASTLE, true, BuildingShape.WIDE),
    BASTION_HOUSE("Бастион", BuildingGroup.TOWERS, 0xFF6E6A5E.toInt(), Glyph.CASTLE, true, BuildingShape.WIDE),

    // Зрелища — продолжение
    AMPHITHEATRE("Амфитеатр", BuildingGroup.FUN, 0xFFA89474.toInt(), Glyph.ARENA, true, BuildingShape.HALL),
    OPERA_HOUSE("Оперный дом", BuildingGroup.FUN, 0xFFB09078.toInt(), Glyph.ARENA, true, BuildingShape.HALL),
    PUPPET_THEATRE("Кукольный театр", BuildingGroup.FUN, 0xFFA98E7A.toInt(), Glyph.ARENA, false, BuildingShape.WIDE),
    CIRCUS_TENT("Шапито", BuildingGroup.FUN, 0xFFB08A86.toInt(), Glyph.CAMP, true, BuildingShape.HALL),
    CONCERT_HALL("Концертный зал", BuildingGroup.FUN, 0xFFAE9480.toInt(), Glyph.BELL, true, BuildingShape.HALL),
    DANCE_HOUSE("Танцевальный дом", BuildingGroup.FUN, 0xFFB29584.toInt(), Glyph.ARENA, false, BuildingShape.WIDE),
    BEAR_PIT_HOUSE("Медвежья яма", BuildingGroup.FUN, 0xFF9C8874.toInt(), Glyph.MONSTER, false, BuildingShape.WIDE),
    COCKPIT("Петушиная арена", BuildingGroup.FUN, 0xFFA48E76.toInt(), Glyph.NEST, false, BuildingShape.WIDE),
    TOURNEY_GROUND("Ристалище", BuildingGroup.FUN, 0xFFA69270.toInt(), Glyph.SWORD, true, BuildingShape.HALL),
    HIPPODROME("Ипподром", BuildingGroup.FUN, 0xFFA8996E.toInt(), Glyph.HORSESHOE, true, BuildingShape.HALL),
    STORYTELLER_STAGE("Помост сказителя", BuildingGroup.FUN, 0xFFAE9686.toInt(), Glyph.BOOK, false, BuildingShape.WIDE);

    companion object {
        fun byGroup(group: BuildingGroup): List<BuildingType> =
            BuildingType.entries.filter { it.group == group }
    }
}

/** Городской квартал — район со своим характером. */
enum class DistrictType(val title: String, val color: Int) {
    MARKET_QUARTER("Торговый квартал", 0xFFD9B970.toInt()),
    CRAFT_QUARTER("Ремесленный квартал", 0xFFC59A6E.toInt()),
    TEMPLE_QUARTER("Храмовый квартал", 0xFFA9AEC4.toInt()),
    NOBLE_QUARTER("Знатный квартал", 0xFFD2A9A0.toInt()),
    POOR_QUARTER("Бедный квартал", 0xFFA99C84.toInt()),
    SLUMS("Трущобы", 0xFF97907C.toInt()),
    HARBOUR_QUARTER("Портовый квартал", 0xFF8FAFBC.toInt()),
    GARRISON_QUARTER("Военный квартал", 0xFFA7A08C.toInt()),
    SCHOLAR_QUARTER("Учёный квартал", 0xFFB3B08E.toInt()),
    MAGIC_QUARTER("Магический квартал", 0xFFAFA3C6.toInt()),
    FOREIGN_QUARTER("Чужеземный квартал", 0xFFC3AE8E.toInt()),
    OLD_TOWN("Старый город", 0xFFC2B296.toInt()),
    NEW_TOWN("Новый город", 0xFFCBBF9E.toInt()),
    FUN_QUARTER("Квартал зрелищ", 0xFFC9A98E.toInt()),
    FARM_QUARTER("Огороды и хозяйства", 0xFFB7C08E.toInt()),
    GRAVE_QUARTER("Кладбище", 0xFF9EA396.toInt()),
    PARK_QUARTER("Парк и сады", 0xFFA4C08C.toInt())
}
