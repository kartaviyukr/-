package com.weatherwear.assistant.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClothingAdvisorTest {

    private fun input(
        temperatureNow: Double = 10.0,
        feelsLikeNow: Double = temperatureNow,
        feelsLikeMin: Double = feelsLikeNow,
        feelsLikeMax: Double = feelsLikeNow,
        windSpeedMax: Double = 2.0,
        windGustsMax: Double = 4.0,
        precipitationProbabilityMax: Int = 0,
        precipitationSum: Double = 0.0,
        snowfallSum: Double = 0.0,
        uvIndexMax: Double = 1.0,
        humidityNow: Int = 55,
        isDay: Boolean = true,
        weatherCodes: List<Int> = listOf(0),
    ) = AdviceInput(
        temperatureNow = temperatureNow,
        feelsLikeNow = feelsLikeNow,
        feelsLikeMin = feelsLikeMin,
        feelsLikeMax = feelsLikeMax,
        windSpeedMax = windSpeedMax,
        windGustsMax = windGustsMax,
        precipitationProbabilityMax = precipitationProbabilityMax,
        precipitationSum = precipitationSum,
        snowfallSum = snowfallSum,
        uvIndexMax = uvIndexMax,
        humidityNow = humidityNow,
        isDay = isDay,
        weatherCodes = weatherCodes,
    )

    @Test
    fun `границы температурных диапазонов`() {
        assertEquals(TempBand.HOT, ClothingAdvisor.bandOf(30.0))
        assertEquals(TempBand.HOT, ClothingAdvisor.bandOf(27.0))
        assertEquals(TempBand.WARM, ClothingAdvisor.bandOf(26.9))
        assertEquals(TempBand.MILD, ClothingAdvisor.bandOf(17.0))
        assertEquals(TempBand.COOL, ClothingAdvisor.bandOf(12.0))
        assertEquals(TempBand.CHILLY, ClothingAdvisor.bandOf(7.0))
        assertEquals(TempBand.COLD, ClothingAdvisor.bandOf(2.0))
        assertEquals(TempBand.FREEZING, ClothingAdvisor.bandOf(-7.9))
        assertEquals(TempBand.SEVERE, ClothingAdvisor.bandOf(-15.0))
    }

    @Test
    fun `в сухую погоду зонт не нужен`() {
        val advice = ClothingAdvisor.advise(input(temperatureNow = 20.0, precipitationProbabilityMax = 5))
        assertEquals(UmbrellaVerdict.NOT_NEEDED, advice.umbrella)
    }

    @Test
    fun `при высокой вероятности дождя зонт обязателен`() {
        val advice = ClothingAdvisor.advise(
            input(
                temperatureNow = 14.0,
                precipitationProbabilityMax = 85,
                precipitationSum = 4.0,
                weatherCodes = listOf(3, 63),
            ),
        )
        assertEquals(UmbrellaVerdict.TAKE_IT, advice.umbrella)
        assertTrue(advice.umbrellaText.contains("85%"))
    }

    @Test
    fun `небольшой шанс дождя - складной зонт на всякий случай`() {
        val advice = ClothingAdvisor.advise(
            input(temperatureNow = 16.0, precipitationProbabilityMax = 40, weatherCodes = listOf(2, 61)),
        )
        assertEquals(UmbrellaVerdict.JUST_IN_CASE, advice.umbrella)
    }

    @Test
    fun `при сильном ветре вместо зонта капюшон`() {
        val advice = ClothingAdvisor.advise(
            input(
                temperatureNow = 11.0,
                precipitationProbabilityMax = 80,
                precipitationSum = 3.0,
                windSpeedMax = 11.0,
                windGustsMax = 18.0,
                weatherCodes = listOf(65),
            ),
        )
        assertEquals(UmbrellaVerdict.USE_HOOD_INSTEAD, advice.umbrella)
        assertTrue(advice.warnings.any { it.contains("Сильный ветер") })
    }

    @Test
    fun `при снеге зонт не предлагается`() {
        val advice = ClothingAdvisor.advise(
            input(
                temperatureNow = -6.0,
                feelsLikeNow = -11.0,
                feelsLikeMin = -12.0,
                feelsLikeMax = -5.0,
                precipitationProbabilityMax = 90,
                snowfallSum = 3.0,
                weatherCodes = listOf(73),
            ),
        )
        assertEquals(UmbrellaVerdict.SNOW_EXPECTED, advice.umbrella)
        assertTrue(advice.footwear.contains("Зимние"))
    }

    @Test
    fun `в мороз советуют шапку перчатки и шарф`() {
        val advice = ClothingAdvisor.advise(
            input(temperatureNow = -12.0, feelsLikeNow = -18.0, feelsLikeMin = -20.0, feelsLikeMax = -14.0),
        )
        assertEquals(TempBand.SEVERE, advice.band)
        assertTrue(advice.accessories.any { it.contains("шапка", ignoreCase = true) })
        assertTrue(advice.accessories.any { it.contains("арежк") || it.contains("ерчатк") })
        assertTrue(advice.layers.any { it.contains("Термобельё") })
        assertTrue(advice.warnings.any { it.contains("обморожения") })
    }

    @Test
    fun `в жару напоминают о воде и защите от солнца`() {
        val advice = ClothingAdvisor.advise(
            input(
                temperatureNow = 31.0,
                feelsLikeNow = 33.0,
                feelsLikeMin = 28.0,
                feelsLikeMax = 34.0,
                uvIndexMax = 9.0,
                humidityNow = 75,
            ),
        )
        assertEquals(TempBand.HOT, advice.band)
        assertTrue(advice.accessories.any { it.contains("вод", ignoreCase = true) })
        assertTrue(advice.accessories.any { it.contains("SPF") })
        assertTrue(advice.warnings.any { it.contains("УФ") })
        assertTrue(advice.warnings.any { it.contains("душно") })
    }

    @Test
    fun `одеваемся по самому холодному моменту дня`() {
        // Днём +18, но вечером ощущается +4 — совет должен быть про прохладу.
        val advice = ClothingAdvisor.advise(
            input(temperatureNow = 18.0, feelsLikeNow = 18.0, feelsLikeMin = 4.0, feelsLikeMax = 18.0),
        )
        assertEquals(TempBand.COLD, advice.band)
        assertTrue(advice.layers.any { it.contains("слоями") })
    }

    @Test
    fun `ледяной дождь превращается в предупреждение о гололёде`() {
        val advice = ClothingAdvisor.advise(
            input(
                temperatureNow = 0.0,
                feelsLikeNow = -3.0,
                feelsLikeMin = -4.0,
                feelsLikeMax = 1.0,
                precipitationProbabilityMax = 75,
                precipitationSum = 2.0,
                weatherCodes = listOf(66),
            ),
        )
        assertTrue(advice.warnings.any { it.contains("гололёда") })
        assertTrue(advice.footwear.contains("нескользящей"))
    }

    @Test
    fun `в комфортную погоду нет лишних предупреждений`() {
        val advice = ClothingAdvisor.advise(
            input(temperatureNow = 19.0, feelsLikeNow = 19.0, feelsLikeMin = 18.0, feelsLikeMax = 21.0),
        )
        assertEquals(TempBand.MILD, advice.band)
        assertTrue(advice.warnings.isEmpty())
        assertFalse(advice.layers.isEmpty())
        assertEquals(UmbrellaVerdict.NOT_NEEDED, advice.umbrella)
    }
}
