package com.fantasymap.creator.model

import kotlinx.serialization.Serializable
import java.util.UUID

/** Чья фишка: от этого зависит цвет кольца и что видят игроки. */
enum class TokenFaction(val title: String, val color: Int) {
    ENEMY("Враг", 0xFFC0392B.toInt()),
    BOSS("Главарь", 0xFF8E44AD.toInt()),
    NEUTRAL("Нейтральный", 0xFFD4A017.toInt()),
    ALLY("Союзник", 0xFF2E9E5B.toInt()),
    HERO("Герой", 0xFF2E6FC0.toInt())
}

/** Размер существа в клетках сетки, как в настольных играх. */
enum class TokenSize(val title: String, val cells: Float) {
    TINY("Крошечный", 0.5f),
    SMALL("Маленький", 1f),
    MEDIUM("Средний", 1f),
    LARGE("Большой", 2f),
    HUGE("Огромный", 3f),
    GARGANTUAN("Исполинский", 4f)
}

enum class TokenGroup(val title: String) {
    HUMANOIDS("Люди и разбойники"),
    GREENSKINS("Гоблины и орки"),
    UNDEAD("Нежить"),
    BEASTS("Звери"),
    VERMIN("Пауки, змеи, насекомые"),
    DRAGONS("Драконы"),
    FIENDS("Демоны и бесы"),
    MONSTERS("Чудовища"),
    ELEMENTALS("Элементали и големы"),
    GIANTS("Великаны"),
    FEY("Феи и лесные духи"),
    DEEP("Твари из глубин"),
    HEROES("Герои"),
    FOLK("Мирные и спутники")
}

/**
 * Готовые фишки существ. hp и ac — ориентиры для мастера,
 * их всегда можно поправить в карточке фишки.
 */
enum class TokenType(
    val title: String,
    val group: TokenGroup,
    val glyph: Glyph,
    val size: TokenSize = TokenSize.MEDIUM,
    val faction: TokenFaction = TokenFaction.ENEMY,
    val hp: Int = 10,
    val ac: Int = 12
) {
    // Люди и разбойники
    BANDIT("Бандит", TokenGroup.HUMANOIDS, Glyph.DAGGER, hp = 11, ac = 12),
    BANDIT_CAPTAIN("Атаман разбойников", TokenGroup.HUMANOIDS, Glyph.SWORD, faction = TokenFaction.BOSS, hp = 65, ac = 15),
    THUG("Громила", TokenGroup.HUMANOIDS, Glyph.HUMANOID, hp = 32, ac = 11),
    GUARD("Стражник", TokenGroup.HUMANOIDS, Glyph.SHIELD, hp = 11, ac = 16),
    KNIGHT("Рыцарь", TokenGroup.HUMANOIDS, Glyph.SWORD, hp = 52, ac = 18),
    ARCHER("Лучник", TokenGroup.HUMANOIDS, Glyph.BOW, hp = 16, ac = 13),
    CROSSBOWMAN("Арбалетчик", TokenGroup.HUMANOIDS, Glyph.BOW, hp = 18, ac = 14),
    ASSASSIN("Убийца", TokenGroup.HUMANOIDS, Glyph.DAGGER, hp = 78, ac = 15),
    BERSERKER("Берсерк", TokenGroup.HUMANOIDS, Glyph.HUMANOID, hp = 67, ac = 13),
    CULTIST("Культист", TokenGroup.HUMANOIDS, Glyph.HAT, hp = 9, ac = 12),
    CULT_FANATIC("Фанатик культа", TokenGroup.HUMANOIDS, Glyph.HAT, hp = 33, ac = 13),
    MAGE_ENEMY("Боевой маг", TokenGroup.HUMANOIDS, Glyph.HAT, hp = 40, ac = 12),
    NECROMANCER("Некромант", TokenGroup.HUMANOIDS, Glyph.SKULL, faction = TokenFaction.BOSS, hp = 66, ac = 12),
    PRIEST_ENEMY("Тёмный жрец", TokenGroup.HUMANOIDS, Glyph.SUN, hp = 27, ac = 13),
    PIRATE("Пират", TokenGroup.HUMANOIDS, Glyph.SWORD, hp = 16, ac = 13),
    PIRATE_CAPTAIN("Капитан пиратов", TokenGroup.HUMANOIDS, Glyph.SWORD, faction = TokenFaction.BOSS, hp = 84, ac = 15),
    GLADIATOR("Гладиатор", TokenGroup.HUMANOIDS, Glyph.SHIELD, hp = 112, ac = 16),
    SPY("Шпион", TokenGroup.HUMANOIDS, Glyph.EYE, hp = 27, ac = 12),
    WARLORD("Военачальник", TokenGroup.HUMANOIDS, Glyph.CROWN, faction = TokenFaction.BOSS, hp = 229, ac = 18),

    // Гоблины и орки
    KOBOLD("Кобольд", TokenGroup.GREENSKINS, Glyph.GOBLIN, TokenSize.SMALL, hp = 5, ac = 12),
    GOBLIN("Гоблин", TokenGroup.GREENSKINS, Glyph.GOBLIN, TokenSize.SMALL, hp = 7, ac = 15),
    GOBLIN_ARCHER("Гоблин-лучник", TokenGroup.GREENSKINS, Glyph.BOW, TokenSize.SMALL, hp = 7, ac = 13),
    GOBLIN_BOSS("Вожак гоблинов", TokenGroup.GREENSKINS, Glyph.GOBLIN, TokenSize.SMALL, TokenFaction.BOSS, 21, 17),
    GOBLIN_SHAMAN("Гоблин-шаман", TokenGroup.GREENSKINS, Glyph.TOTEM, TokenSize.SMALL, hp = 12, ac = 12),
    HOBGOBLIN("Хобгоблин", TokenGroup.GREENSKINS, Glyph.SHIELD, hp = 11, ac = 18),
    BUGBEAR("Медвежатник", TokenGroup.GREENSKINS, Glyph.GOBLIN, hp = 27, ac = 16),
    ORC("Орк", TokenGroup.GREENSKINS, Glyph.GOBLIN, hp = 15, ac = 13),
    ORC_BERSERKER("Орк-берсерк", TokenGroup.GREENSKINS, Glyph.HORNS, hp = 30, ac = 13),
    ORC_CHIEF("Вождь орков", TokenGroup.GREENSKINS, Glyph.CROWN, faction = TokenFaction.BOSS, hp = 93, ac = 16),
    OGRE("Огр", TokenGroup.GREENSKINS, Glyph.GIANT, TokenSize.LARGE, hp = 59, ac = 11),
    TROLL("Тролль", TokenGroup.GREENSKINS, Glyph.GIANT, TokenSize.LARGE, hp = 84, ac = 15),
    WORG_RIDER("Наездник на варге", TokenGroup.GREENSKINS, Glyph.PAW, TokenSize.LARGE, hp = 30, ac = 14),

    // Нежить
    SKELETON("Скелет", TokenGroup.UNDEAD, Glyph.SKULL, hp = 13, ac = 13),
    SKELETON_ARCHER("Скелет-лучник", TokenGroup.UNDEAD, Glyph.BOW, hp = 13, ac = 13),
    ZOMBIE("Зомби", TokenGroup.UNDEAD, Glyph.SKULL, hp = 22, ac = 8),
    GHOUL("Упырь", TokenGroup.UNDEAD, Glyph.SKULL, hp = 22, ac = 12),
    WIGHT("Умертвие", TokenGroup.UNDEAD, Glyph.SKULL, hp = 45, ac = 14),
    GHOST("Призрак", TokenGroup.UNDEAD, Glyph.GHOST, hp = 45, ac = 11),
    BANSHEE("Баньши", TokenGroup.UNDEAD, Glyph.GHOST, hp = 58, ac = 12),
    WRAITH("Призрачный страж", TokenGroup.UNDEAD, Glyph.GHOST, hp = 67, ac = 13),
    MUMMY("Мумия", TokenGroup.UNDEAD, Glyph.SKULL, hp = 58, ac = 11),
    VAMPIRE_SPAWN("Вампир-отродье", TokenGroup.UNDEAD, Glyph.BAT, hp = 82, ac = 15),
    VAMPIRE("Вампир", TokenGroup.UNDEAD, Glyph.BAT, faction = TokenFaction.BOSS, hp = 144, ac = 16),
    DEATH_KNIGHT("Рыцарь смерти", TokenGroup.UNDEAD, Glyph.SWORD, faction = TokenFaction.BOSS, hp = 180, ac = 20),
    LICH("Лич", TokenGroup.UNDEAD, Glyph.CROWN, faction = TokenFaction.BOSS, hp = 135, ac = 17),
    BONE_GIANT("Костяной исполин", TokenGroup.UNDEAD, Glyph.SKULL, TokenSize.HUGE, hp = 150, ac = 14),

    // Звери
    WOLF("Волк", TokenGroup.BEASTS, Glyph.PAW, hp = 11, ac = 13),
    DIRE_WOLF("Лютоволк", TokenGroup.BEASTS, Glyph.PAW, TokenSize.LARGE, hp = 37, ac = 14),
    WARG("Варг", TokenGroup.BEASTS, Glyph.PAW, TokenSize.LARGE, hp = 26, ac = 13),
    BEAR("Медведь", TokenGroup.BEASTS, Glyph.PAW, TokenSize.LARGE, hp = 34, ac = 11),
    BOAR("Кабан", TokenGroup.BEASTS, Glyph.PAW, hp = 11, ac = 11),
    PANTHER("Пантера", TokenGroup.BEASTS, Glyph.PAW, hp = 13, ac = 12),
    GIANT_RAT("Гигантская крыса", TokenGroup.BEASTS, Glyph.PAW, TokenSize.SMALL, hp = 7, ac = 12),
    RAT_SWARM("Рой крыс", TokenGroup.BEASTS, Glyph.PAW, hp = 24, ac = 10),
    BAT_SWARM("Стая летучих мышей", TokenGroup.BEASTS, Glyph.BAT, hp = 22, ac = 12),
    GIANT_EAGLE("Гигантский орёл", TokenGroup.BEASTS, Glyph.WINGS, TokenSize.LARGE, TokenFaction.NEUTRAL, 26, 13),
    HORSE_B("Лошадь", TokenGroup.BEASTS, Glyph.HORSESHOE, TokenSize.LARGE, TokenFaction.NEUTRAL, 19, 10),
    SHARK("Акула", TokenGroup.BEASTS, Glyph.FISH, TokenSize.LARGE, hp = 45, ac = 12),

    // Пауки, змеи, насекомые
    GIANT_SPIDER("Гигантский паук", TokenGroup.VERMIN, Glyph.SPIDER, TokenSize.LARGE, hp = 26, ac = 14),
    SPIDER_SWARM("Рой пауков", TokenGroup.VERMIN, Glyph.SPIDER, hp = 22, ac = 12),
    PHASE_SPIDER("Мерцающий паук", TokenGroup.VERMIN, Glyph.SPIDER, TokenSize.LARGE, hp = 32, ac = 13),
    GIANT_SNAKE("Гигантская змея", TokenGroup.VERMIN, Glyph.SERPENT, TokenSize.LARGE, hp = 60, ac = 12),
    VIPER("Ядовитая змея", TokenGroup.VERMIN, Glyph.SERPENT, TokenSize.TINY, hp = 2, ac = 13),
    GIANT_SCORPION("Гигантский скорпион", TokenGroup.VERMIN, Glyph.SPIDER, TokenSize.LARGE, hp = 52, ac = 15),
    GIANT_CENTIPEDE("Гигантская многоножка", TokenGroup.VERMIN, Glyph.SERPENT, TokenSize.SMALL, hp = 4, ac = 13),
    INSECT_SWARM("Рой насекомых", TokenGroup.VERMIN, Glyph.SPIDER, hp = 22, ac = 12),
    GIANT_WORM("Пещерный червь", TokenGroup.VERMIN, Glyph.SERPENT, TokenSize.GARGANTUAN, hp = 247, ac = 18),

    // Драконы
    DRAGON_WYRMLING("Дракончик", TokenGroup.DRAGONS, Glyph.DRAGON, hp = 33, ac = 17),
    YOUNG_DRAGON("Молодой дракон", TokenGroup.DRAGONS, Glyph.DRAGON, TokenSize.LARGE, hp = 178, ac = 18),
    ADULT_DRAGON("Взрослый дракон", TokenGroup.DRAGONS, Glyph.DRAGON, TokenSize.HUGE, TokenFaction.BOSS, 256, 19),
    ANCIENT_DRAGON("Древний дракон", TokenGroup.DRAGONS, Glyph.DRAGON, TokenSize.GARGANTUAN, TokenFaction.BOSS, 546, 22),
    WYVERN("Виверна", TokenGroup.DRAGONS, Glyph.WINGS, TokenSize.LARGE, hp = 110, ac = 13),
    DRAKE("Дрейк", TokenGroup.DRAGONS, Glyph.DRAGON, hp = 45, ac = 14),
    PSEUDODRAGON("Псевдодракон", TokenGroup.DRAGONS, Glyph.DRAGON, TokenSize.TINY, TokenFaction.NEUTRAL, 7, 13),

    // Демоны и бесы
    IMP("Бес", TokenGroup.FIENDS, Glyph.HORNS, TokenSize.TINY, hp = 10, ac = 13),
    QUASIT("Квазит", TokenGroup.FIENDS, Glyph.HORNS, TokenSize.TINY, hp = 7, ac = 13),
    HELL_HOUND("Адская гончая", TokenGroup.FIENDS, Glyph.PAW, hp = 45, ac = 15),
    SUCCUBUS("Суккуб", TokenGroup.FIENDS, Glyph.WINGS, hp = 66, ac = 15),
    DEMON("Демон", TokenGroup.FIENDS, Glyph.HORNS, TokenSize.LARGE, hp = 110, ac = 15),
    PIT_FIEND("Владыка преисподней", TokenGroup.FIENDS, Glyph.HORNS, TokenSize.LARGE, TokenFaction.BOSS, 300, 19),
    FLAME_LORD("Огненный демон", TokenGroup.FIENDS, Glyph.FLAME, TokenSize.HUGE, TokenFaction.BOSS, 262, 19),

    // Чудовища
    OWLBEAR("Совомедведь", TokenGroup.MONSTERS, Glyph.PAW, TokenSize.LARGE, hp = 59, ac = 13),
    MANTICORE("Мантикора", TokenGroup.MONSTERS, Glyph.WINGS, TokenSize.LARGE, hp = 68, ac = 14),
    CHIMERA("Химера", TokenGroup.MONSTERS, Glyph.HORNS, TokenSize.LARGE, hp = 114, ac = 14),
    BASILISK("Василиск", TokenGroup.MONSTERS, Glyph.SERPENT, hp = 52, ac = 15),
    MEDUSA("Медуза", TokenGroup.MONSTERS, Glyph.SERPENT, hp = 127, ac = 15),
    MINOTAUR("Минотавр", TokenGroup.MONSTERS, Glyph.HORNS, TokenSize.LARGE, hp = 76, ac = 14),
    GRIFFON("Грифон", TokenGroup.MONSTERS, Glyph.WINGS, TokenSize.LARGE, hp = 59, ac = 12),
    HYDRA("Гидра", TokenGroup.MONSTERS, Glyph.SERPENT, TokenSize.HUGE, TokenFaction.BOSS, 172, 15),
    HARPY("Гарпия", TokenGroup.MONSTERS, Glyph.WINGS, hp = 38, ac = 11),
    MIMIC("Мимик", TokenGroup.MONSTERS, Glyph.CHEST, hp = 58, ac = 12),
    GELATINOUS_CUBE("Студенистый куб", TokenGroup.MONSTERS, Glyph.OOZE, TokenSize.LARGE, hp = 84, ac = 6),
    OOZE_B("Слизь", TokenGroup.MONSTERS, Glyph.OOZE, hp = 45, ac = 8),
    WEREWOLF("Оборотень", TokenGroup.MONSTERS, Glyph.PAW, hp = 58, ac = 12),
    TROGLODYTE("Троглодит", TokenGroup.MONSTERS, Glyph.GOBLIN, hp = 13, ac = 11),
    GARGOYLE("Горгулья", TokenGroup.MONSTERS, Glyph.WINGS, hp = 52, ac = 15),
    KRAKEN_SPAWN("Щупальце кракена", TokenGroup.MONSTERS, Glyph.TENTACLES, TokenSize.HUGE, hp = 90, ac = 14),

    // Элементали и големы
    FIRE_ELEMENTAL("Огненный элементаль", TokenGroup.ELEMENTALS, Glyph.FLAME, TokenSize.LARGE, hp = 102, ac = 13),
    WATER_ELEMENTAL("Водный элементаль", TokenGroup.ELEMENTALS, Glyph.OOZE, TokenSize.LARGE, hp = 114, ac = 14),
    EARTH_ELEMENTAL("Земляной элементаль", TokenGroup.ELEMENTALS, Glyph.GOLEM, TokenSize.LARGE, hp = 126, ac = 17),
    AIR_ELEMENTAL("Воздушный элементаль", TokenGroup.ELEMENTALS, Glyph.GHOST, TokenSize.LARGE, hp = 90, ac = 15),
    MAGMA_MEPHIT("Магмовый бесёнок", TokenGroup.ELEMENTALS, Glyph.FLAME, TokenSize.SMALL, hp = 22, ac = 11),
    STONE_GOLEM("Каменный голем", TokenGroup.ELEMENTALS, Glyph.GOLEM, TokenSize.LARGE, hp = 178, ac = 17),
    IRON_GOLEM("Железный голем", TokenGroup.ELEMENTALS, Glyph.GOLEM, TokenSize.LARGE, TokenFaction.BOSS, 210, 20),
    CLAY_GOLEM("Глиняный голем", TokenGroup.ELEMENTALS, Glyph.GOLEM, TokenSize.LARGE, hp = 133, ac = 14),
    ANIMATED_ARMOR("Оживший доспех", TokenGroup.ELEMENTALS, Glyph.SHIELD, hp = 33, ac = 18),
    FLYING_SWORD("Летающий меч", TokenGroup.ELEMENTALS, Glyph.SWORD, TokenSize.SMALL, hp = 17, ac = 17),

    // Великаны
    HILL_GIANT("Холмовой великан", TokenGroup.GIANTS, Glyph.GIANT, TokenSize.HUGE, hp = 105, ac = 13),
    FROST_GIANT("Ледяной великан", TokenGroup.GIANTS, Glyph.GIANT, TokenSize.HUGE, hp = 138, ac = 15),
    FIRE_GIANT("Огненный великан", TokenGroup.GIANTS, Glyph.GIANT, TokenSize.HUGE, hp = 162, ac = 18),
    STONE_GIANT("Каменный великан", TokenGroup.GIANTS, Glyph.GIANT, TokenSize.HUGE, hp = 126, ac = 17),
    CYCLOPS("Циклоп", TokenGroup.GIANTS, Glyph.EYE, TokenSize.HUGE, hp = 138, ac = 14),
    ETTIN("Двухголовый великан", TokenGroup.GIANTS, Glyph.GIANT, TokenSize.LARGE, hp = 85, ac = 12),

    // Феи и лесные духи
    PIXIE("Пикси", TokenGroup.FEY, Glyph.WINGS, TokenSize.TINY, TokenFaction.NEUTRAL, 1, 15),
    SPRITE("Спрайт", TokenGroup.FEY, Glyph.WINGS, TokenSize.TINY, TokenFaction.NEUTRAL, 2, 15),
    DRYAD("Дриада", TokenGroup.FEY, Glyph.LEAF, faction = TokenFaction.NEUTRAL, hp = 22, ac = 11),
    SATYR("Сатир", TokenGroup.FEY, Glyph.HORNS, faction = TokenFaction.NEUTRAL, hp = 31, ac = 14),
    TREANT("Древень", TokenGroup.FEY, Glyph.TREE, TokenSize.HUGE, TokenFaction.NEUTRAL, 138, 16),
    BLIGHT("Ожившая коряга", TokenGroup.FEY, Glyph.LEAF, TokenSize.SMALL, hp = 11, ac = 13),
    HAG("Ведьма болот", TokenGroup.FEY, Glyph.HAT, faction = TokenFaction.BOSS, hp = 82, ac = 17),
    WILL_O_WISP("Блуждающий огонёк", TokenGroup.FEY, Glyph.GHOST, TokenSize.TINY, hp = 22, ac = 19),

    // Твари из глубин
    TENTACLE_HORROR("Щупальцевый ужас", TokenGroup.DEEP, Glyph.TENTACLES, TokenSize.LARGE, hp = 93, ac = 14),
    ALL_SEEING_EYE("Всевидящее око", TokenGroup.DEEP, Glyph.EYE, TokenSize.LARGE, TokenFaction.BOSS, 180, 18),
    MIND_EATER("Пожиратель разума", TokenGroup.DEEP, Glyph.TENTACLES, hp = 71, ac = 15),
    CAVE_LURKER("Пещерный скрытень", TokenGroup.DEEP, Glyph.EYE, TokenSize.LARGE, hp = 97, ac = 13),
    VOID_SPAWN("Порождение пустоты", TokenGroup.DEEP, Glyph.OOZE, hp = 45, ac = 13),
    FISHFOLK("Рыболюд", TokenGroup.DEEP, Glyph.FISH, hp = 11, ac = 13),

    // Герои
    HERO_FIGHTER("Воин", TokenGroup.HEROES, Glyph.SWORD, faction = TokenFaction.HERO, hp = 44, ac = 18),
    HERO_BARBARIAN("Варвар", TokenGroup.HEROES, Glyph.HORNS, faction = TokenFaction.HERO, hp = 55, ac = 15),
    HERO_PALADIN("Паладин", TokenGroup.HEROES, Glyph.SHIELD, faction = TokenFaction.HERO, hp = 44, ac = 18),
    HERO_ROGUE("Плут", TokenGroup.HEROES, Glyph.DAGGER, faction = TokenFaction.HERO, hp = 33, ac = 15),
    HERO_RANGER("Следопыт", TokenGroup.HEROES, Glyph.BOW, faction = TokenFaction.HERO, hp = 39, ac = 15),
    HERO_WIZARD("Волшебник", TokenGroup.HEROES, Glyph.HAT, faction = TokenFaction.HERO, hp = 27, ac = 12),
    HERO_SORCERER("Чародей", TokenGroup.HEROES, Glyph.FLAME, faction = TokenFaction.HERO, hp = 28, ac = 12),
    HERO_WARLOCK("Колдун", TokenGroup.HEROES, Glyph.EYE, faction = TokenFaction.HERO, hp = 33, ac = 13),
    HERO_CLERIC("Жрец", TokenGroup.HEROES, Glyph.SUN, faction = TokenFaction.HERO, hp = 38, ac = 18),
    HERO_DRUID("Друид", TokenGroup.HEROES, Glyph.LEAF, faction = TokenFaction.HERO, hp = 33, ac = 14),
    HERO_BARD("Бард", TokenGroup.HEROES, Glyph.LUTE, faction = TokenFaction.HERO, hp = 33, ac = 14),
    HERO_MONK("Монах", TokenGroup.HEROES, Glyph.HUMANOID, faction = TokenFaction.HERO, hp = 33, ac = 16),
    HERO_ARTIFICER("Изобретатель", TokenGroup.HEROES, Glyph.GOLEM, faction = TokenFaction.HERO, hp = 33, ac = 16),

    // Мирные и спутники
    COMMONER("Простолюдин", TokenGroup.FOLK, Glyph.HUMANOID, faction = TokenFaction.NEUTRAL, hp = 4, ac = 10),
    MERCHANT("Торговец", TokenGroup.FOLK, Glyph.COINS, faction = TokenFaction.NEUTRAL, hp = 9, ac = 10),
    NOBLE("Вельможа", TokenGroup.FOLK, Glyph.CROWN, faction = TokenFaction.NEUTRAL, hp = 9, ac = 15),
    CHILD("Ребёнок", TokenGroup.FOLK, Glyph.HUMANOID, TokenSize.SMALL, TokenFaction.NEUTRAL, 2, 10),
    PRISONER("Пленник", TokenGroup.FOLK, Glyph.LOCK, faction = TokenFaction.NEUTRAL, hp = 4, ac = 10),
    HIRELING("Наёмник-спутник", TokenGroup.FOLK, Glyph.SWORD, faction = TokenFaction.ALLY, hp = 11, ac = 14),
    ACOLYTE("Послушник", TokenGroup.FOLK, Glyph.SUN, faction = TokenFaction.ALLY, hp = 9, ac = 10),
    FAMILIAR("Фамильяр", TokenGroup.FOLK, Glyph.WINGS, TokenSize.TINY, TokenFaction.ALLY, 1, 11),
    WARHORSE("Боевой конь", TokenGroup.FOLK, Glyph.HORSESHOE, TokenSize.LARGE, TokenFaction.ALLY, 19, 11),
    SUMMONED("Призванное существо", TokenGroup.FOLK, Glyph.PORTAL, faction = TokenFaction.ALLY, hp = 20, ac = 13);

    companion object {
        fun byGroup(group: TokenGroup): List<TokenType> = TokenType.entries.filter { it.group == group }
    }
}

/** Состояния существа — цветные метки вокруг фишки. */
enum class Condition(val title: String, val color: Int) {
    POISONED("Отравлен", 0xFF6BAF2E.toInt()),
    STUNNED("Оглушён", 0xFFE8C547.toInt()),
    PRONE("Сбит с ног", 0xFF9A7B4F.toInt()),
    BLINDED("Ослеплён", 0xFF4A4A4A.toInt()),
    CHARMED("Очарован", 0xFFE87BB5.toInt()),
    FRIGHTENED("Испуган", 0xFF7E57C2.toInt()),
    GRAPPLED("Схвачен", 0xFFB5651D.toInt()),
    RESTRAINED("Опутан", 0xFF8D6E63.toInt()),
    INVISIBLE("Невидим", 0xFFB0BEC5.toInt()),
    PARALYZED("Парализован", 0xFF00ACC1.toInt()),
    BURNING("Горит", 0xFFFF7043.toInt()),
    BLEEDING("Кровоточит", 0xFFC62828.toInt()),
    SLEEPING("Спит", 0xFF5C6BC0.toInt()),
    CONCENTRATING("Концентрация", 0xFF26A69A.toInt()),
    BLESSED("Благословлён", 0xFFFFD54F.toInt()),
    CURSED("Проклят", 0xFF6A1B9A.toInt()),
    HASTED("Ускорен", 0xFF29B6F6.toInt()),
    SLOWED("Замедлен", 0xFF78909C.toInt()),
    UNCONSCIOUS("Без сознания", 0xFF37474F.toInt())
}

/** Фишка существа на боевой локации. */
@Serializable
data class Token(
    val id: String = UUID.randomUUID().toString(),
    val type: TokenType = TokenType.GOBLIN,
    val name: String = "",
    val pos: Vec = Vec(0f, 0f),
    val size: TokenSize = TokenSize.MEDIUM,
    val faction: TokenFaction = TokenFaction.ENEMY,
    val hp: Int = 10,
    val maxHp: Int = 10,
    val ac: Int = 12,
    /** Инициатива; null — ещё не бросали. */
    val initiative: Int? = null,
    val conditions: List<Condition> = emptyList(),
    /** Аура вокруг фишки в футах: свет, заклинание, зона страха. */
    val aura: Int = 0,
    val notes: String = "",
    val showLabel: Boolean = true,
    /** Спрятана от игроков: видна только мастеру. */
    val hidden: Boolean = false,
    /** Своя картинка вместо значка — портрет героя или врага. */
    val assetId: String? = null
) {
    val dead: Boolean get() = maxHp > 0 && hp <= 0
    val title: String get() = name.ifBlank { type.title }
}

/** Область, закрытая туманом войны. */
@Serializable
data class FogArea(
    val id: String = UUID.randomUUID().toString(),
    val points: List<Vec> = emptyList()
)

/** Сетка боевой локации. */
enum class GridKind(val title: String) {
    SQUARE("Квадратная"),
    HEX("Шестиугольная"),
    NONE("Без сетки")
}

/** Описание сцены и ход боя. */
@Serializable
data class SceneInfo(
    val goal: String = "",
    val enemyTactics: String = "",
    val reward: String = "",
    val notes: String = "",
    val round: Int = 1,
    /** Чей ход по списку инициативы. */
    val turn: Int = 0
)

/** Шаги работы над боевой локацией. */
enum class BattleStage(
    override val number: Int,
    override val title: String,
    override val hint: String
) : MapStage {
    GROUND(1, "Пол и местность", "Выберите основу и закрасьте пол, траву, воду, лаву. Сетка уже на месте."),
    WALLS(2, "Стены и двери", "Проведите стены, решётки и уступы, поставьте двери, лестницы и проходы."),
    PROPS(3, "Обстановка", "Столы, сундуки, колонны, алтари, деревья и валуны — всё, за чем прячутся."),
    TRAPS(4, "Ловушки и добыча", "Ловушки, тайники, рычаги, сундуки с сокровищами и ключи."),
    ENEMIES(5, "Враги", "Расставьте врагов: касание ставит фишку, повторные получают номера."),
    HEROES(6, "Герои и союзники", "Фишки игроков, спутников и мирных жителей."),
    FOG(7, "Туман и свет", "Закройте туманом то, чего герои ещё не видят; отметьте свет и тьму."),
    SCENE(8, "Бой и описание", "Инициатива, кубики, раунды и описание сцены для мастера.");

    companion object {
        fun byNumber(number: Int): BattleStage = entries.firstOrNull { it.number == number } ?: GROUND
    }
}
