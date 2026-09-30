package com.fantasymap.creator.model

import kotlinx.serialization.Serializable
import kotlin.math.floor
import kotlin.random.Random

/** Характеристики D&D 5e. */
enum class Ability(val title: String, val short: String) {
    STR("Сила", "СИЛ"),
    DEX("Ловкость", "ЛОВ"),
    CON("Телосложение", "ТЕЛ"),
    INT("Интеллект", "ИНТ"),
    WIS("Мудрость", "МДР"),
    CHA("Харизма", "ХАР")
}

/** Навыки и проверки: что бросают игроки, чтобы найти спрятанное. */
enum class CheckSkill(val title: String, val ability: Ability) {
    PERCEPTION("Внимательность", Ability.WIS),
    INVESTIGATION("Анализ", Ability.INT),
    INSIGHT("Проницательность", Ability.WIS),
    SURVIVAL("Выживание", Ability.WIS),
    ARCANA("Магия", Ability.INT),
    RELIGION("Религия", Ability.INT),
    HISTORY("История", Ability.INT),
    NATURE("Природа", Ability.INT),
    MEDICINE("Медицина", Ability.WIS),
    ANIMAL_HANDLING("Уход за животными", Ability.WIS),
    ATHLETICS("Атлетика", Ability.STR),
    ACROBATICS("Акробатика", Ability.DEX),
    SLEIGHT_OF_HAND("Ловкость рук", Ability.DEX),
    STEALTH("Скрытность", Ability.DEX),
    DECEPTION("Обман", Ability.CHA),
    INTIMIDATION("Запугивание", Ability.CHA),
    PERFORMANCE("Выступление", Ability.CHA),
    PERSUASION("Убеждение", Ability.CHA),
    THIEVES_TOOLS("Воровские инструменты", Ability.DEX),
    /** Любая проверка подходит — мастер решает сам. */
    ANY("Любая проверка", Ability.WIS)
}

/**
 * Тайна на карте: что спрятано, какой проверкой находится и что узнают игроки.
 * Пока hidden, игроки этого не видят.
 */
@Serializable
data class Secret(
    val hidden: Boolean = false,
    /** Класс сложности проверки. */
    val dc: Int = 12,
    val skill: CheckSkill = CheckSkill.PERCEPTION,
    /** Что мастер прочитает игрокам, когда тайна раскроется. */
    val reveal: String = ""
)

/** Атака: название, бонус попадания и урон вида «1к8+3». */
@Serializable
data class Attack(
    val name: String = "Удар",
    val bonus: Int = 3,
    val damage: String = "1к6+1",
    val damageType: String = ""
)

/** Лист персонажа — всё, что нужно для бросков по правилам. */
@Serializable
data class CharacterSheet(
    val str: Int = 10,
    val dex: Int = 10,
    val con: Int = 10,
    val int: Int = 10,
    val wis: Int = 10,
    val cha: Int = 10,
    val proficiency: Int = 2,
    val speed: Int = 30,
    /** Дальность зрения в футах; 0 — видит только освещённое рядом. */
    val vision: Int = 60,
    val skills: List<CheckSkill> = emptyList(),
    val saves: List<Ability> = emptyList(),
    val attacks: List<Attack> = listOf(Attack())
) {
    fun score(ability: Ability): Int = when (ability) {
        Ability.STR -> str
        Ability.DEX -> dex
        Ability.CON -> con
        Ability.INT -> int
        Ability.WIS -> wis
        Ability.CHA -> cha
    }

    fun mod(ability: Ability): Int = Dnd.modifier(score(ability))

    fun skillBonus(skill: CheckSkill): Int =
        mod(skill.ability) + if (skill in skills) proficiency else 0

    fun saveBonus(ability: Ability): Int =
        mod(ability) + if (ability in saves) proficiency else 0
}

/** Правила D&D 5e для бросков в режиме игры. */
object Dnd {

    fun modifier(score: Int): Int = floor((score - 10) / 2.0).toInt()

    fun signed(value: Int): String = if (value >= 0) "+$value" else "$value"

    /** Лист существа по его виду: грубые ориентиры, мастер правит в карточке. */
    fun defaultSheet(token: Token): CharacterSheet {
        val hero = token.faction == TokenFaction.HERO || token.faction == TokenFaction.ALLY
        val bonus = ((token.ac - 10) + 2).coerceIn(2, 11)
        val damage = when {
            token.maxHp <= 10 -> "1к6+1"
            token.maxHp <= 30 -> "1к8+2"
            token.maxHp <= 70 -> "2к8+3"
            token.maxHp <= 150 -> "2к10+5"
            else -> "3к10+7"
        }
        return if (hero) {
            CharacterSheet(
                str = 14, dex = 14, con = 13, int = 11, wis = 13, cha = 11,
                skills = listOf(CheckSkill.PERCEPTION, CheckSkill.INVESTIGATION, CheckSkill.ATHLETICS),
                saves = listOf(Ability.STR, Ability.CON),
                attacks = listOf(Attack("Длинный меч", bonus = 5, damage = "1к8+3", damageType = "рубящий"),
                    Attack("Короткий лук", bonus = 5, damage = "1к6+2", damageType = "колющий"))
            )
        } else {
            CharacterSheet(
                str = 12, dex = 12, con = 12, wis = 10,
                skills = listOf(CheckSkill.PERCEPTION, CheckSkill.STEALTH),
                attacks = listOf(Attack("Атака", bonus = bonus, damage = damage))
            )
        }
    }

    fun sheetOf(token: Token): CharacterSheet = token.sheet ?: defaultSheet(token)

    /** Разбор кубиков вида «2к6+3», «1d8-1», «к20», «4». */
    data class DiceSpec(val count: Int, val sides: Int, val modifier: Int)

    fun parse(text: String): DiceSpec? {
        val t = text.lowercase().replace(" ", "").replace('d', 'к')
        if (t.isEmpty()) return null
        val match = Regex("^(\\d*)к(\\d+)([+-]\\d+)?$").find(t)
        if (match != null) {
            val count = match.groupValues[1].ifEmpty { "1" }.toInt().coerceIn(1, 50)
            val sides = match.groupValues[2].toInt().coerceIn(2, 1000)
            val mod = match.groupValues[3].ifEmpty { "0" }.toInt()
            return DiceSpec(count, sides, mod)
        }
        return t.toIntOrNull()?.let { DiceSpec(0, 1, it) }
    }

    data class Roll(val dice: List<Int>, val total: Int, val text: String)

    /** Бросок кубиков; crit — удвоить число кубиков (критическое попадание). */
    fun roll(spec: DiceSpec, random: Random, crit: Boolean = false): Roll {
        val count = if (crit) spec.count * 2 else spec.count
        val dice = List(count) { random.nextInt(1, spec.sides + 1) }
        val total = (dice.sum() + spec.modifier).coerceAtLeast(0)
        val text = buildString {
            if (dice.isNotEmpty()) append(dice.joinToString("+"))
            if (spec.modifier != 0 || dice.isEmpty()) append(if (dice.isEmpty()) "${spec.modifier}" else signed(spec.modifier))
        }
        return Roll(dice, total, text)
    }

    /** к20 с преимуществом (1), помехой (-1) или обычный (0). */
    data class D20(val first: Int, val second: Int?, val kept: Int)

    fun d20(random: Random, advantage: Int): D20 {
        val first = random.nextInt(1, 21)
        if (advantage == 0) return D20(first, null, first)
        val second = random.nextInt(1, 21)
        val kept = if (advantage > 0) maxOf(first, second) else minOf(first, second)
        return D20(first, second, kept)
    }

    private fun d20Text(d: D20): String = if (d.second == null) "${d.first}" else "${d.first}/${d.second}→${d.kept}"

    data class CheckResult(val d20: D20, val bonus: Int, val total: Int, val text: String)

    fun check(name: String, what: String, bonus: Int, random: Random, advantage: Int): CheckResult {
        val d = d20(random, advantage)
        val total = d.kept + bonus
        val text = "$name · $what: к20 ${d20Text(d)}${signed(bonus)} = $total" +
            (if (d.kept == 20) " · натуральная 20!" else if (d.kept == 1) " · натуральная 1" else "")
        return CheckResult(d, bonus, total, text)
    }

    data class AttackResult(
        val d20: D20,
        val total: Int,
        val hit: Boolean,
        val crit: Boolean,
        val damage: Int,
        val text: String
    )

    /** Атака по правилам: 20 — всегда попадание и двойные кубики урона, 1 — всегда промах. */
    fun attack(
        attacker: String,
        attack: Attack,
        target: String,
        targetAc: Int,
        random: Random,
        advantage: Int
    ): AttackResult {
        val d = d20(random, advantage)
        val total = d.kept + attack.bonus
        val crit = d.kept == 20
        val hit = crit || (d.kept != 1 && total >= targetAc)
        var damage = 0
        val text = StringBuilder("$attacker → $target · ${attack.name}: к20 ${d20Text(d)}${signed(attack.bonus)} = $total против КД $targetAc — ")
        if (hit) {
            val spec = parse(attack.damage) ?: DiceSpec(1, 4, 0)
            val roll = roll(spec, random, crit)
            damage = roll.total
            text.append(if (crit) "КРИТ! " else "попадание, ")
            text.append("урон ${roll.text} = $damage")
            if (attack.damageType.isNotBlank()) text.append(" (${attack.damageType})")
        } else {
            text.append(if (d.kept == 1) "провал (натуральная 1)" else "промах")
        }
        return AttackResult(d, total, hit, crit, damage, text.toString())
    }

    /** Ключ клетки сетки для списка изученных клеток. */
    fun cellKey(cx: Int, cy: Int): Long = (cx.toLong() shl 32) or (cy.toLong() and 0xFFFFFFFFL)

    fun cellX(key: Long): Int = (key shr 32).toInt()

    fun cellY(key: Long): Int = key.toInt()
}
