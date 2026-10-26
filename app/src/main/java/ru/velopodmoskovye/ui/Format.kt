package ru.velopodmoskovye.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Только метрические единицы. */
object Format {
    private val ru = Locale("ru")

    fun km(km: Double): String = when {
        km < 1.0 -> "${(km * 1000).toInt()} м"
        km < 10.0 -> String.format(ru, "%.1f км", km)
        else -> "${km.toInt()} км"
    }

    fun meters(m: Double): String = km(m / 1000.0)

    fun hours(h: Double): String {
        val total = (h * 60).toInt()
        val hh = total / 60
        val mm = total % 60
        return when {
            hh == 0 -> "$mm мин"
            mm == 0 -> "$hh ч"
            else -> "$hh ч $mm мин"
        }
    }

    fun minutes(m: Int): String = hours(m / 60.0)

    fun stars(mustHave: Int): String = when (mustHave) {
        3 -> "★"
        2 -> "☆"
        else -> ""
    }

    fun mustHaveLabel(mustHave: Int): String = when (mustHave) {
        3 -> "★ обязательно"
        2 -> "☆ очень желательно"
        else -> "по пути"
    }

    fun dateTime(ms: Long): String = SimpleDateFormat("d MMMM yyyy, HH:mm", ru).format(Date(ms))

    fun percent(share: Double): String = String.format(ru, "%.2f %%", share * 100)
}
