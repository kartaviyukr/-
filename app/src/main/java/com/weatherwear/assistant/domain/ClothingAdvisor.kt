package com.weatherwear.assistant.domain

import kotlin.math.abs
import kotlin.math.roundToInt

/** Исходные данные для совета: агрегат по интересующему отрезку времени. */
data class AdviceInput(
    /** Температура воздуха сейчас, °C. */
    val temperatureNow: Double,
    /** Ощущаемая температура сейчас, °C. */
    val feelsLikeNow: Double,
    /** Минимальная ощущаемая температура на горизонте планирования, °C. */
    val feelsLikeMin: Double,
    /** Максимальная ощущаемая температура на горизонте планирования, °C. */
    val feelsLikeMax: Double,
    /** Максимальная скорость ветра, м/с. */
    val windSpeedMax: Double,
    /** Максимальные порывы ветра, м/с. */
    val windGustsMax: Double,
    /** Наибольшая вероятность осадков, %. */
    val precipitationProbabilityMax: Int,
    /** Суммарные осадки, мм. */
    val precipitationSum: Double,
    /** Суммарный снег, см. */
    val snowfallSum: Double,
    /** Максимальный УФ-индекс. */
    val uvIndexMax: Double,
    /** Относительная влажность сейчас, %. */
    val humidityNow: Int,
    /** Сейчас светло? */
    val isDay: Boolean,
    /** Коды погоды WMO на горизонте планирования. */
    val weatherCodes: List<Int>,
)

enum class TempBand {
    HOT, WARM, MILD, COOL, CHILLY, COLD, FREEZING, SEVERE
}

enum class UmbrellaVerdict {
    /** Осадков не ждём. */
    NOT_NEEDED,

    /** Небольшой шанс — складной зонт в рюкзак. */
    JUST_IN_CASE,

    /** Дождь почти наверняка. */
    TAKE_IT,

    /** Дождь есть, но ветер сломает зонт — нужен капюшон/дождевик. */
    USE_HOOD_INSTEAD,

    /** Ожидается снег — зонт не поможет. */
    SNOW_EXPECTED,
}

data class Advice(
    val headline: String,
    val summary: String,
    val band: TempBand,
    val umbrella: UmbrellaVerdict,
    val umbrellaText: String,
    val layers: List<String>,
    val accessories: List<String>,
    val footwear: String,
    val warnings: List<String>,
)

/**
 * Превращает погодные числа в человеческий совет: во что одеться,
 * брать ли зонт и о чём стоит помнить.
 *
 * Одеваемся по самому холодному моменту горизонта планирования
 * (обычно это ближайшие 12 часов), а не по текущей температуре —
 * иначе к вечеру можно замёрзнуть.
 */
object ClothingAdvisor {

    fun advise(input: AdviceInput): Advice {
        val band = bandOf(input.feelsLikeMin)
        val umbrella = umbrellaVerdict(input)

        return Advice(
            headline = headline(band, input),
            summary = summary(band, input),
            band = band,
            umbrella = umbrella,
            umbrellaText = umbrellaText(umbrella, input),
            layers = layers(band, input),
            accessories = accessories(band, input),
            footwear = footwear(band, input),
            warnings = warnings(band, input),
        )
    }

    fun bandOf(feelsLike: Double): TempBand = when {
        feelsLike >= 27 -> TempBand.HOT
        feelsLike >= 22 -> TempBand.WARM
        feelsLike >= 17 -> TempBand.MILD
        feelsLike >= 12 -> TempBand.COOL
        feelsLike >= 7 -> TempBand.CHILLY
        feelsLike >= 2 -> TempBand.COLD
        feelsLike >= -8 -> TempBand.FREEZING
        else -> TempBand.SEVERE
    }

    private fun headline(band: TempBand, input: AdviceInput): String = when (band) {
        TempBand.HOT -> "Жарко"
        TempBand.WARM -> "Тепло"
        TempBand.MILD -> "Комфортно"
        TempBand.COOL -> "Свежо"
        TempBand.CHILLY -> "Прохладно"
        TempBand.COLD -> "Холодно"
        TempBand.FREEZING -> "Мороз"
        TempBand.SEVERE -> "Сильный мороз"
    } + if (input.precipitationProbabilityMax >= 60) ", и будет мокро" else ""

    private fun summary(band: TempBand, input: AdviceInput): String {
        val feels = input.feelsLikeNow.roundToInt()
        val air = input.temperatureNow.roundToInt()
        val diff = feels - air
        val feelsPart = when {
            abs(diff) >= 3 -> "Сейчас $air°, а ощущается как $feels°"
            else -> "Сейчас около $air°"
        }
        val rangePart = "ощущаемая за день от ${input.feelsLikeMin.roundToInt()}° до ${input.feelsLikeMax.roundToInt()}°"
        return "$feelsPart, $rangePart."
    }

    private fun umbrellaVerdict(input: AdviceInput): UmbrellaVerdict {
        val snowExpected = input.snowfallSum >= 0.2 || input.weatherCodes.any { WeatherCodes.isSnow(it) }
        val rainExpected = input.weatherCodes.any { WeatherCodes.isRain(it) }
        if (snowExpected && !rainExpected) return UmbrellaVerdict.SNOW_EXPECTED

        val strongSignal = input.precipitationProbabilityMax >= 70 || input.precipitationSum >= 2.0
        val weakSignal = input.precipitationProbabilityMax >= 35 ||
            input.precipitationSum >= 0.3 ||
            (rainExpected && input.precipitationProbabilityMax >= 25)

        return when {
            !strongSignal && !weakSignal -> UmbrellaVerdict.NOT_NEEDED
            input.windGustsMax >= 12 -> UmbrellaVerdict.USE_HOOD_INSTEAD
            strongSignal -> UmbrellaVerdict.TAKE_IT
            else -> UmbrellaVerdict.JUST_IN_CASE
        }
    }

    private fun umbrellaText(verdict: UmbrellaVerdict, input: AdviceInput): String {
        val chance = input.precipitationProbabilityMax
        return when (verdict) {
            UmbrellaVerdict.NOT_NEEDED -> "Зонт не нужен — осадков не ожидается."
            UmbrellaVerdict.JUST_IN_CASE ->
                "Возьмите складной зонт на всякий случай: вероятность осадков $chance%."
            UmbrellaVerdict.TAKE_IT ->
                "Зонт обязательно: вероятность осадков $chance%, ожидается около " +
                    "${formatMm(input.precipitationSum)} мм."
            UmbrellaVerdict.USE_HOOD_INSTEAD ->
                "Дождь есть ($chance%), но порывы до ${input.windGustsMax.roundToInt()} м/с — " +
                    "зонт вывернет. Лучше дождевик или куртка с капюшоном."
            UmbrellaVerdict.SNOW_EXPECTED ->
                "Ожидается снег — зонт не поможет, нужна куртка с капюшоном."
        }
    }

    private fun layers(band: TempBand, input: AdviceInput): List<String> {
        val base = when (band) {
            TempBand.HOT -> listOf(
                "Футболка или майка из хлопка/льна",
                "Шорты, лёгкая юбка или тонкие брюки",
            )
            TempBand.WARM -> listOf(
                "Футболка",
                "Лёгкие брюки или шорты",
            )
            TempBand.MILD -> listOf(
                "Футболка или лонгслив",
                "Рубашка либо тонкая кофта сверху",
                "Джинсы или брюки",
            )
            TempBand.COOL -> listOf(
                "Лонгслив или тонкий свитер",
                "Ветровка либо джинсовка",
                "Джинсы или брюки",
            )
            TempBand.CHILLY -> listOf(
                "Футболка как первый слой",
                "Свитер или худи",
                "Демисезонная куртка",
                "Плотные брюки",
            )
            TempBand.COLD -> listOf(
                "Тёплая кофта или свитер",
                "Утеплённая куртка",
                "Плотные брюки",
            )
            TempBand.FREEZING -> listOf(
                "Термобельё или плотный лонгслив",
                "Шерстяной свитер либо флис",
                "Зимняя куртка или пуховик",
                "Утеплённые брюки",
            )
            TempBand.SEVERE -> listOf(
                "Термобельё обязательно",
                "Флис и шерстяной свитер",
                "Тёплый пуховик",
                "Утеплённые или горнолыжные брюки",
            )
        }

        val extra = mutableListOf<String>()
        val spread = input.feelsLikeMax - input.feelsLikeMin
        if (spread >= 8 && band != TempBand.HOT) {
            extra += "Одевайтесь слоями: днём потеплеет на ${spread.roundToInt()}°"
        }
        if (input.windSpeedMax >= 8 && band >= TempBand.COOL) {
            extra += "Верхний слой лучше ветрозащитный"
        }
        if (input.precipitationSum >= 1.0 && band != TempBand.HOT) {
            extra += "Куртка с водоотталкивающей пропиткой"
        }
        return base + extra
    }

    private fun accessories(band: TempBand, input: AdviceInput): List<String> {
        val list = mutableListOf<String>()
        when (band) {
            TempBand.SEVERE -> {
                list += "Тёплая шапка"
                list += "Варежки или тёплые перчатки"
                list += "Шарф или бафф — закройте лицо"
                list += "Термоноски"
            }
            TempBand.FREEZING -> {
                list += "Шапка"
                list += "Перчатки"
                list += "Шарф"
                list += "Тёплые носки"
            }
            TempBand.COLD -> {
                list += "Шапка"
                list += "Перчатки"
            }
            TempBand.CHILLY -> {
                list += "Лёгкая шапка или капюшон"
            }
            else -> Unit
        }

        if (input.uvIndexMax >= 6) {
            list += "Солнцезащитный крем SPF 30+"
            list += "Солнечные очки"
        } else if (input.uvIndexMax >= 3 && input.isDay) {
            list += "Солнечные очки не помешают"
        }

        if (band == TempBand.HOT) {
            list += "Кепка или панама"
            list += "Бутылка воды"
        }

        if (input.windSpeedMax >= 10 && band >= TempBand.CHILLY) {
            list += "Шарф или бафф от ветра"
        }
        return list
    }

    private fun footwear(band: TempBand, input: AdviceInput): String {
        val wet = input.precipitationSum >= 0.5 || input.precipitationProbabilityMax >= 60
        val snowy = input.snowfallSum >= 0.2 || input.weatherCodes.any { WeatherCodes.isSnow(it) }
        // Гололёд — это осадки около нуля, а не любой мороз с осадками.
        val icy = input.weatherCodes.any { WeatherCodes.isFreezingPrecipitation(it) } ||
            (input.feelsLikeMin in -5.0..1.0 && wet)

        return when {
            icy -> "Обувь с рифлёной нескользящей подошвой — возможен гололёд"
            snowy || band == TempBand.SEVERE || band == TempBand.FREEZING ->
                "Зимние утеплённые ботинки"
            wet -> "Непромокаемая обувь: ботинки или резиновые сапоги"
            band == TempBand.HOT -> "Сандалии или лёгкие кроссовки"
            band == TempBand.WARM -> "Лёгкие кроссовки"
            band == TempBand.COLD -> "Утеплённые ботинки"
            else -> "Кроссовки или ботинки"
        }
    }

    private fun warnings(band: TempBand, input: AdviceInput): List<String> {
        val list = mutableListOf<String>()

        if (input.windGustsMax >= 15) {
            list += "Сильный ветер, порывы до ${input.windGustsMax.roundToInt()} м/с"
        }
        if (input.temperatureNow - input.feelsLikeNow >= 5) {
            list += "Из-за ветра ощущается на ${(input.temperatureNow - input.feelsLikeNow).roundToInt()}° холоднее"
        }
        if (input.weatherCodes.any { WeatherCodes.isThunder(it) }) {
            list += "Возможна гроза — не прячьтесь под деревьями"
        }
        if (input.weatherCodes.any { WeatherCodes.isFog(it) }) {
            list += "Туман: закладывайте больше времени на дорогу"
        }
        if (input.weatherCodes.any { WeatherCodes.isFreezingPrecipitation(it) }) {
            list += "Ледяной дождь — высокий риск гололёда"
        }
        if (input.uvIndexMax >= 8) {
            list += "Очень высокий УФ-индекс (${input.uvIndexMax.roundToInt()}) — избегайте солнца с 12 до 16"
        }
        if (band == TempBand.HOT && input.humidityNow >= 70) {
            list += "Жара с влажностью ${input.humidityNow}% — будет душно, пейте воду"
        }
        if (band == TempBand.SEVERE) {
            list += "Ограничьте время на улице: риск обморожения"
        }
        return list
    }

    private fun formatMm(mm: Double): String {
        val rounded = (mm * 10).roundToInt() / 10.0
        return if (rounded == rounded.toInt().toDouble()) rounded.toInt().toString() else rounded.toString()
    }
}
