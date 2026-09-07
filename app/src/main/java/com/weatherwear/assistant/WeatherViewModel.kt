package com.weatherwear.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwear.assistant.data.DEFAULT_PLACE
import com.weatherwear.assistant.data.LocationProvider
import com.weatherwear.assistant.data.Place
import com.weatherwear.assistant.data.Prefs
import com.weatherwear.assistant.data.WeatherBundle
import com.weatherwear.assistant.data.WeatherRepository
import com.weatherwear.assistant.domain.Advice
import com.weatherwear.assistant.domain.AdvicePlanner
import com.weatherwear.assistant.domain.ClothingAdvisor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class UiState(
    val loading: Boolean = false,
    val bundle: WeatherBundle? = null,
    val advice: Advice? = null,
    val error: String? = null,
    val locationHint: String? = null,
    val searchOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Place> = emptyList(),
    val searching: Boolean = false,
    val usingGeolocation: Boolean = false,
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository()
    private val locationProvider = LocationProvider(application)
    private val prefs = Prefs(application)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        start()
    }

    /** Первый запуск: пробуем геолокацию, иначе — последний город, иначе Москва. */
    fun start() {
        if (prefs.useGeolocation && locationProvider.hasPermission()) {
            refreshByLocation()
        } else {
            load(prefs.loadPlace() ?: DEFAULT_PLACE, fromGeolocation = false)
        }
    }

    fun hasLocationPermission(): Boolean = locationProvider.hasPermission()

    /** Пользователь дал разрешение на геолокацию — определяем место заново. */
    fun onLocationPermissionGranted() {
        prefs.useGeolocation = true
        refreshByLocation()
    }

    fun refreshByLocation() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, locationHint = "Определяем местоположение…") }
            val place = runCatching { locationProvider.currentPlace() }.getOrNull()
            if (place == null) {
                val fallback = prefs.loadPlace() ?: DEFAULT_PLACE
                _state.update {
                    it.copy(
                        locationHint = "Не удалось определить местоположение, показываем «${fallback.name}»",
                    )
                }
                loadInternal(fallback, fromGeolocation = false)
            } else {
                prefs.useGeolocation = true
                prefs.savePlace(place)
                _state.update { it.copy(locationHint = null) }
                loadInternal(place, fromGeolocation = true)
            }
        }
    }

    /** Обновление прогноза для уже выбранного места. */
    fun refresh() {
        val current = _state.value
        if (current.usingGeolocation) {
            refreshByLocation()
        } else {
            load(current.bundle?.place ?: prefs.loadPlace() ?: DEFAULT_PLACE, fromGeolocation = false)
        }
    }

    fun selectPlace(place: Place) {
        prefs.useGeolocation = false
        prefs.savePlace(place)
        _state.update {
            it.copy(searchOpen = false, searchQuery = "", searchResults = emptyList(), locationHint = null)
        }
        load(place, fromGeolocation = false)
    }

    fun openSearch() = _state.update { it.copy(searchOpen = true) }

    fun closeSearch() =
        _state.update { it.copy(searchOpen = false, searchQuery = "", searchResults = emptyList()) }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _state.update { it.copy(searchResults = emptyList(), searching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(350) // не дёргаем сеть на каждую букву
            _state.update { it.copy(searching = true) }
            val results = runCatching { repository.searchPlaces(query) }.getOrDefault(emptyList())
            _state.update { it.copy(searchResults = results, searching = false) }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }

    private fun load(place: Place, fromGeolocation: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { loadInternal(place, fromGeolocation) }
    }

    private suspend fun loadInternal(place: Place, fromGeolocation: Boolean) {
        _state.update { it.copy(loading = true, error = null) }
        try {
            val bundle = repository.loadWeather(place)
            val advice = ClothingAdvisor.advise(AdvicePlanner.buildInput(bundle))
            _state.update {
                it.copy(
                    loading = false,
                    bundle = bundle,
                    advice = advice,
                    error = null,
                    usingGeolocation = fromGeolocation,
                )
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: IOException) {
            _state.update {
                it.copy(loading = false, error = "Нет связи с сервером погоды: ${e.message ?: "проверьте интернет"}")
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(loading = false, error = "Не удалось получить прогноз: ${e.message ?: "неизвестная ошибка"}")
            }
        }
    }
}
