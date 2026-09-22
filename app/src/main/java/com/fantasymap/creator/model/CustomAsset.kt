package com.fantasymap.creator.model

import kotlinx.serialization.Serializable
import java.util.UUID

/** Что именно создал автор: постройку, зону или отдельный объект. */
enum class CustomKind(val title: String, val hint: String) {
    BUILDING(
        "Постройка",
        "Картинка ложится на след здания: дом, храм, башня, что угодно своё."
    ),
    ZONE(
        "Зона",
        "Картинка замащивает область целиком — свой лес, свои пески, свой квартал."
    ),
    OBJECT(
        "Объект",
        "Картинка ставится значком в точке: свой знак, свой зверь, своё чудо."
    )
}

/**
 * Авторская заготовка: своя картинка, которую можно ставить на карту
 * как постройку, зону или объект.
 *
 * Сама картинка лежит отдельным файлом во внутренней памяти,
 * здесь хранится только её описание.
 */
@Serializable
data class CustomAsset(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val kind: CustomKind = CustomKind.BUILDING,
    /** Где уместна заготовка: на карте мира, города или везде. */
    val scope: MarkerScope = MarkerScope.BOTH,
    /** Размер заготовки относительно обычного дома или значка. */
    val size: Float = 1f,
    /** Размер плитки текстуры зоны в единицах карты. */
    val tile: Float = 90f,
    /** Запасной цвет: им заливается место, если картинка потерялась. */
    val color: Int = 0xFFB08A5E.toInt(),
    /** Рисовать ли обводку вокруг заготовки. */
    val outlined: Boolean = true,
    val createdAt: Long = 0L
) {
    fun fits(mapKind: MapKind): Boolean = when (scope) {
        MarkerScope.BOTH -> true
        MarkerScope.CITY -> mapKind == MapKind.CITY
        MarkerScope.WORLD -> mapKind == MapKind.WORLD
    }
}

/** Вся авторская библиотека одним файлом. */
@Serializable
data class CustomLibrary(
    val assets: List<CustomAsset> = emptyList()
)
