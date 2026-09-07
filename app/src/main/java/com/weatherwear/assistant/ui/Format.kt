package com.weatherwear.assistant.ui

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

private val WEEKDAYS = arrayOf(
    "понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье",
)

private val MONTHS = arrayOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря",
)

fun formatTemp(value: Double): String = "${value.roundToInt()}°"

fun formatSignedTemp(value: Double): String {
    val rounded = value.roundToInt()
    return if (rounded > 0) "+$rounded°" else "$rounded°"
}

fun formatWind(value: Double): String = "${value.roundToInt()} м/с"

fun formatHour(time: LocalDateTime): String = "%02d:00".format(time.hour)

fun formatTime(time: LocalDateTime): String = "%02d:%02d".format(time.hour, time.minute)

fun formatDayTitle(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Сегодня"
    today.plusDays(1) -> "Завтра"
    else -> WEEKDAYS[date.dayOfWeek.value - 1].replaceFirstChar { it.uppercase() }
}

fun formatDate(date: LocalDate): String = "${date.dayOfMonth} ${MONTHS[date.monthValue - 1]}"
