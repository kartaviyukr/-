package ru.velopodmoskovye.data.content

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.velopodmoskovye.domain.model.ContentManifest
import ru.velopodmoskovye.domain.model.Destination
import ru.velopodmoskovye.domain.model.SpiralPack
import ru.velopodmoskovye.domain.model.SpiralPlace
import javax.inject.Inject
import javax.inject.Singleton

/** Весь контент-пак в памяти: он небольшой и нужен почти на каждом экране. */
data class ContentPack(
    val manifest: ContentManifest,
    val spiral: SpiralPack,
    val destinations: Map<String, Destination>,
) {
    val regionNames: Map<String, String> = spiral.regions.associate { it.id to it.name }

    /** Места спирали, которые покрывает маршрут (сам город и усадьбы-перемычки). */
    fun placesOf(destinationId: String): List<SpiralPlace> = spiral.places.filter { it.destinationId == destinationId }
}

object ContentParser {
    val json = Json { ignoreUnknownKeys = true }

    /** [read] получает путь внутри пака, например "destinations/reutov.json". */
    fun parse(read: (String) -> String): ContentPack {
        val manifest = json.decodeFromString<ContentManifest>(read("manifest.json"))
        val spiral = json.decodeFromString<SpiralPack>(read(manifest.spiral))
        val destinations = manifest.destinations.associate { file ->
            val d = json.decodeFromString<Destination>(read("destinations/$file"))
            d.id to d.copy(stops = d.stops.sortedBy { it.order })
        }
        return ContentPack(manifest, spiral, destinations)
    }
}

@Singleton
class ContentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val mutex = Mutex()
    @Volatile private var cache: ContentPack? = null

    suspend fun pack(): ContentPack = cache ?: mutex.withLock {
        cache ?: withContext(Dispatchers.IO) {
            ContentParser.parse { path -> context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() } }
        }.also { cache = it }
    }
}
