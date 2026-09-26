package com.fantasymap.creator.model

/** Чем крыта постройка: ключ фото-текстуры из assets/textures. */
enum class RoofMaterial(val title: String, val texture: String) {
    CLAY_TILES("Красная черепица", "roof_clay"),
    GREY_TILES("Серая черепица", "roof"),
    SLATE("Сланец", "roof_slate"),
    SHINGLES("Дранка", "roof_shingle"),
    THATCH("Солома", "hay"),
    PLANKS("Доски", "old_wood"),
    TIN("Жесть", "roof_tin"),
    METAL("Железо", "metal"),
    COPPER("Медь", "roof_copper"),
    STONE("Каменные плиты", "stone_tiles"),
    PLASTER("Плоская глиняная", "plaster"),
    MARBLE("Мрамор", "marble"),
    GOLD("Позолота", "roof_gold"),
    GLASS("Стекло", "roof_glass"),
    CANVAS("Полотно", "fabric"),
    TURF("Дёрн", "aerial_grass"),
    BRICK("Кирпич", "brick"),
    CHARRED("Гарь", "ash"),
    MOSS("Мох", "moss"),
    CRYSTAL("Кристалл", "crystal"),
    DARK_STONE("Тёмный камень", "castle_wall"),
    BARK("Кора", "bark"),
    ROCK("Скала", "rock"),
    MAGIC("Чары", "magic")
}

/** Как сложена крыша сверху. */
enum class RoofForm {
    GABLE, HIP, PYRAMID, DOME, CONE, FLAT, COURTYARD, SAWTOOTH, CROSS, TENT, RUIN
}

/** Мелочь на крыше, отличающая постройку от соседей. */
enum class RoofDetail {
    NONE, CHIMNEY, DORMERS, TWIN_CHIMNEYS, SKYLIGHT, VENTS, LANTERN, SPIRE, BANNER, GARDEN, WEATHERVANE, BELL
}

data class RoofLook(val material: RoofMaterial, val form: RoofForm, val detail: RoofDetail)

/**
 * Облик крыши каждой постройки. У каждого вида здания своё сочетание
 * материала, формы и детали — двух одинаковых крыш в городе нет
 * (проверяется тестом BuildingLookTest).
 */
object BuildingLook {

    fun of(type: BuildingType): RoofLook = looks.getValue(type)

    private val looks: Map<BuildingType, RoofLook> by lazy { assign() }

    private fun assign(): Map<BuildingType, RoofLook> {
        val used = HashSet<RoofLook>()
        val result = LinkedHashMap<BuildingType, RoofLook>()
        val details = RoofDetail.entries
        val forms = RoofForm.entries
        for (type in BuildingType.entries) {
            val (material, form) = base(type)
            val start = details.indexOf(preferredDetail(type))
            var chosen: RoofLook? = null
            // Сначала своя форма, потом соседние — пока не найдётся свободное сочетание.
            val formOrder = listOf(form) + forms.filter { it != form && it != RoofForm.RUIN }
            loop@ for (f in formOrder) {
                for (k in details.indices) {
                    val look = RoofLook(material, f, details[(start + k) % details.size])
                    if (look !in used) {
                        chosen = look
                        break@loop
                    }
                }
            }
            val look = chosen ?: RoofLook(material, form, RoofDetail.NONE)
            used.add(look)
            result[type] = look
        }
        return result
    }

    private fun preferredDetail(type: BuildingType): RoofDetail = when (type.group) {
        BuildingGroup.HOME -> RoofDetail.CHIMNEY
        BuildingGroup.FAITH -> RoofDetail.SPIRE
        BuildingGroup.POWER -> RoofDetail.BANNER
        BuildingGroup.CRAFT -> RoofDetail.TWIN_CHIMNEYS
        BuildingGroup.TRADE -> RoofDetail.DORMERS
        BuildingGroup.KNOWLEDGE -> RoofDetail.SKYLIGHT
        BuildingGroup.FUN -> RoofDetail.BANNER
        BuildingGroup.SERVICE -> RoofDetail.VENTS
        BuildingGroup.MAGIC_HOUSE -> RoofDetail.LANTERN
        BuildingGroup.RUIN_HOUSE -> RoofDetail.NONE
        BuildingGroup.TOWERS -> RoofDetail.BELL
    }

    private fun base(type: BuildingType): Pair<RoofMaterial, RoofForm> {
        overrides[type]?.let { return it }
        val material = when (type.group) {
            BuildingGroup.HOME -> RoofMaterial.CLAY_TILES
            BuildingGroup.POWER -> RoofMaterial.SLATE
            BuildingGroup.FAITH -> RoofMaterial.SLATE
            BuildingGroup.TRADE -> RoofMaterial.CLAY_TILES
            BuildingGroup.CRAFT -> RoofMaterial.SHINGLES
            BuildingGroup.KNOWLEDGE -> RoofMaterial.GREY_TILES
            BuildingGroup.FUN -> RoofMaterial.CLAY_TILES
            BuildingGroup.SERVICE -> RoofMaterial.PLANKS
            BuildingGroup.MAGIC_HOUSE -> RoofMaterial.MAGIC
            BuildingGroup.RUIN_HOUSE -> RoofMaterial.CHARRED
            BuildingGroup.TOWERS -> RoofMaterial.STONE
        }
        val form = when {
            type.group == BuildingGroup.RUIN_HOUSE -> RoofForm.RUIN
            type.shape == BuildingShape.TOWER -> RoofForm.PYRAMID
            type.shape == BuildingShape.HALL -> RoofForm.HIP
            type.shape == BuildingShape.WIDE -> RoofForm.HIP
            else -> RoofForm.GABLE
        }
        return material to form
    }

    private val overrides: Map<BuildingType, Pair<RoofMaterial, RoofForm>> = mapOf(
        BuildingType.HUT to (RoofMaterial.THATCH to RoofForm.GABLE),
        BuildingType.TALL_HOUSE to (RoofMaterial.GREY_TILES to RoofForm.GABLE),
        BuildingType.RICH_HOUSE to (RoofMaterial.SLATE to RoofForm.HIP),
        BuildingType.MANOR to (RoofMaterial.SLATE to RoofForm.COURTYARD),
        BuildingType.TENEMENT to (RoofMaterial.BRICK to RoofForm.FLAT),
        BuildingType.TOWER_HOUSE to (RoofMaterial.STONE to RoofForm.PYRAMID),
        BuildingType.FARMSTEAD_HOUSE to (RoofMaterial.THATCH to RoofForm.COURTYARD),
        BuildingType.SHACK_ROW to (RoofMaterial.PLANKS to RoofForm.GABLE),
        BuildingType.TOWN_HALL to (RoofMaterial.COPPER to RoofForm.HIP),
        BuildingType.PALACE to (RoofMaterial.GOLD to RoofForm.COURTYARD),
        BuildingType.KEEP to (RoofMaterial.DARK_STONE to RoofForm.FLAT),
        BuildingType.PRISON_HOUSE to (RoofMaterial.DARK_STONE to RoofForm.COURTYARD),
        BuildingType.MINT_HOUSE to (RoofMaterial.COPPER to RoofForm.FLAT),
        BuildingType.CHAPEL to (RoofMaterial.SLATE to RoofForm.CROSS),
        BuildingType.CHURCH to (RoofMaterial.SLATE to RoofForm.CROSS),
        BuildingType.CATHEDRAL_HOUSE to (RoofMaterial.COPPER to RoofForm.CROSS),
        BuildingType.MONASTERY_HOUSE to (RoofMaterial.CLAY_TILES to RoofForm.COURTYARD),
        BuildingType.SHRINE_HOUSE to (RoofMaterial.MARBLE to RoofForm.DOME),
        BuildingType.OLD_TEMPLE to (RoofMaterial.MOSS to RoofForm.DOME),
        BuildingType.CRYPT_HOUSE to (RoofMaterial.DARK_STONE to RoofForm.PYRAMID),
        BuildingType.MARKET_HALL to (RoofMaterial.CANVAS to RoofForm.SAWTOOTH),
        BuildingType.WAREHOUSE to (RoofMaterial.PLANKS to RoofForm.GABLE),
        BuildingType.BANK_HOUSE to (RoofMaterial.MARBLE to RoofForm.FLAT),
        BuildingType.CARAVAN_YARD to (RoofMaterial.PLASTER to RoofForm.COURTYARD),
        BuildingType.SPICE_SHOP to (RoofMaterial.PLASTER to RoofForm.FLAT),
        BuildingType.SMITHY to (RoofMaterial.METAL to RoofForm.GABLE),
        BuildingType.GLASSBLOWER to (RoofMaterial.GLASS to RoofForm.SAWTOOTH),
        BuildingType.MILL_HOUSE to (RoofMaterial.SHINGLES to RoofForm.CONE),
        BuildingType.SHIPYARD_HOUSE to (RoofMaterial.PLANKS to RoofForm.SAWTOOTH),
        BuildingType.STONECUTTER to (RoofMaterial.STONE to RoofForm.GABLE),
        BuildingType.POTTERY to (RoofMaterial.CLAY_TILES to RoofForm.FLAT),
        BuildingType.BREWERY to (RoofMaterial.TIN to RoofForm.SAWTOOTH),
        BuildingType.LIBRARY_HOUSE to (RoofMaterial.COPPER to RoofForm.HIP),
        BuildingType.UNIVERSITY_HOUSE to (RoofMaterial.SLATE to RoofForm.COURTYARD),
        BuildingType.OBSERVATORY_HOUSE to (RoofMaterial.COPPER to RoofForm.DOME),
        BuildingType.HOSPITAL to (RoofMaterial.GREY_TILES to RoofForm.COURTYARD),
        BuildingType.BATH_HOUSE_CITY to (RoofMaterial.MARBLE to RoofForm.DOME),
        BuildingType.THEATRE_HOUSE to (RoofMaterial.CLAY_TILES to RoofForm.DOME),
        BuildingType.ARENA_HOUSE to (RoofMaterial.STONE to RoofForm.COURTYARD),
        BuildingType.STABLE to (RoofMaterial.THATCH to RoofForm.GABLE),
        BuildingType.GRANARY to (RoofMaterial.THATCH to RoofForm.HIP),
        BuildingType.WATER_HOUSE to (RoofMaterial.STONE to RoofForm.DOME),
        BuildingType.GATE_HOUSE to (RoofMaterial.STONE to RoofForm.FLAT),
        BuildingType.WALL_TOWER to (RoofMaterial.STONE to RoofForm.FLAT),
        BuildingType.LIGHTHOUSE_HOUSE to (RoofMaterial.STONE to RoofForm.CONE),
        BuildingType.DOVECOTE to (RoofMaterial.SHINGLES to RoofForm.CONE),
        BuildingType.GALLOWS_YARD to (RoofMaterial.PLANKS to RoofForm.FLAT),
        BuildingType.WIZARD_HOUSE to (RoofMaterial.MAGIC to RoofForm.CONE),
        BuildingType.MAGE_SCHOOL to (RoofMaterial.MAGIC to RoofForm.COURTYARD),
        BuildingType.ALCHEMIST_HOUSE to (RoofMaterial.COPPER to RoofForm.GABLE),
        BuildingType.ENCHANTER to (RoofMaterial.CRYSTAL to RoofForm.GABLE),
        BuildingType.SEER_HOUSE to (RoofMaterial.MAGIC to RoofForm.DOME),
        BuildingType.BURNT_HOUSE to (RoofMaterial.CHARRED to RoofForm.RUIN),
        BuildingType.RUINED_HOUSE to (RoofMaterial.STONE to RoofForm.RUIN),
        BuildingType.ABANDONED_HOUSE to (RoofMaterial.MOSS to RoofForm.RUIN),
        BuildingType.PLAGUE_HOUSE to (RoofMaterial.THATCH to RoofForm.RUIN),
        BuildingType.ROUND_TOWER to (RoofMaterial.STONE to RoofForm.CONE),
        BuildingType.SQUARE_TOWER to (RoofMaterial.STONE to RoofForm.FLAT),
        BuildingType.WATCH_TOWER_HOUSE to (RoofMaterial.PLANKS to RoofForm.PYRAMID),
        BuildingType.CLOCK_TOWER to (RoofMaterial.COPPER to RoofForm.PYRAMID),
        BuildingType.BELL_TOWER to (RoofMaterial.SLATE to RoofForm.PYRAMID),
        BuildingType.SIGNAL_TOWER to (RoofMaterial.STONE to RoofForm.CONE),
        BuildingType.WATER_TOWER to (RoofMaterial.TIN to RoofForm.CONE),
        BuildingType.DOVE_TOWER to (RoofMaterial.SHINGLES to RoofForm.PYRAMID),
        BuildingType.PRISON_TOWER to (RoofMaterial.DARK_STONE to RoofForm.CONE),
        BuildingType.ASTROLOGER_TOWER to (RoofMaterial.MAGIC to RoofForm.CONE),
        BuildingType.BEACON_TOWER to (RoofMaterial.BRICK to RoofForm.CONE),
        BuildingType.RUINED_TOWER to (RoofMaterial.ROCK to RoofForm.RUIN),
        BuildingType.MAIN_GATE_HOUSE to (RoofMaterial.DARK_STONE to RoofForm.FLAT),
        BuildingType.BARBICAN_HOUSE to (RoofMaterial.DARK_STONE to RoofForm.COURTYARD),
        BuildingType.BASTION_HOUSE to (RoofMaterial.ROCK to RoofForm.FLAT),
        BuildingType.AMPHITHEATRE to (RoofMaterial.STONE to RoofForm.COURTYARD),
        BuildingType.OPERA_HOUSE to (RoofMaterial.COPPER to RoofForm.DOME),
        BuildingType.CIRCUS_TENT to (RoofMaterial.CANVAS to RoofForm.TENT),
        BuildingType.TOURNEY_GROUND to (RoofMaterial.TURF to RoofForm.COURTYARD),
        BuildingType.HIPPODROME to (RoofMaterial.TURF to RoofForm.COURTYARD),
        BuildingType.BEAR_PIT_HOUSE to (RoofMaterial.PLANKS to RoofForm.COURTYARD),
        BuildingType.MAGES_GUILD to (RoofMaterial.MAGIC to RoofForm.HIP),
        BuildingType.THIEVES_GUILD to (RoofMaterial.DARK_STONE to RoofForm.GABLE),
        BuildingType.DRAGON_ROOST to (RoofMaterial.ROCK to RoofForm.CONE),
        BuildingType.GRIFFON_AERIE to (RoofMaterial.PLANKS to RoofForm.CONE),
        BuildingType.AIRSHIP_DOCK to (RoofMaterial.METAL to RoofForm.FLAT),
        BuildingType.TREASURY to (RoofMaterial.GOLD to RoofForm.FLAT),
        BuildingType.ARSENAL to (RoofMaterial.DARK_STONE to RoofForm.SAWTOOTH),
        BuildingType.HALL_OF_FAME to (RoofMaterial.MARBLE to RoofForm.HIP),
        BuildingType.BLACK_MARKET to (RoofMaterial.CANVAS to RoofForm.FLAT),
        BuildingType.MENAGERIE to (RoofMaterial.TURF to RoofForm.COURTYARD),
        BuildingType.GREENHOUSE to (RoofMaterial.GLASS to RoofForm.GABLE),
        BuildingType.STAR_DOME to (RoofMaterial.GLASS to RoofForm.DOME),
        BuildingType.DEATH_TEMPLE to (RoofMaterial.DARK_STONE to RoofForm.PYRAMID),
        BuildingType.SUN_TEMPLE to (RoofMaterial.GOLD to RoofForm.DOME),
        BuildingType.DWARVEN_FORGE to (RoofMaterial.ROCK to RoofForm.FLAT),
        BuildingType.TREEHOUSE to (RoofMaterial.BARK to RoofForm.DOME),
        BuildingType.WITCH_HOUSE to (RoofMaterial.MOSS to RoofForm.CONE),
        BuildingType.VAMPIRE_MANOR to (RoofMaterial.DARK_STONE to RoofForm.CROSS),
        BuildingType.HAUNTED_HOUSE to (RoofMaterial.MOSS to RoofForm.RUIN),
        BuildingType.GOLEM_WORKSHOP to (RoofMaterial.METAL to RoofForm.SAWTOOTH),
        BuildingType.PORTAL_HALL to (RoofMaterial.MAGIC to RoofForm.DOME),
        BuildingType.FIGHTING_PIT to (RoofMaterial.PLANKS to RoofForm.COURTYARD),
        BuildingType.BARD_COLLEGE to (RoofMaterial.CLAY_TILES to RoofForm.COURTYARD),
        BuildingType.ASYLUM to (RoofMaterial.GREY_TILES to RoofForm.COURTYARD),
        BuildingType.PUPPET_THEATRE to (RoofMaterial.CANVAS to RoofForm.TENT),
        BuildingType.STORYTELLER_STAGE to (RoofMaterial.CANVAS to RoofForm.GABLE)
    )
}
