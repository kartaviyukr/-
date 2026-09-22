package com.fantasymap.creator.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.fantasymap.creator.model.CustomAsset
import com.fantasymap.creator.model.CustomLibrary
import com.fantasymap.creator.render.TextureSource
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Хранилище авторского контента: описания заготовок в одном JSON,
 * картинки — отдельными файлами рядом.
 */
class AssetStore(private val context: Context) : TextureSource {

    private val dir: File = File(context.filesDir, "custom").apply { mkdirs() }
    private val indexFile = File(dir, "library.json")
    private val cache = HashMap<String, Bitmap?>()
    private var known: List<CustomAsset>? = null

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun imageFile(id: String): File = File(dir, "$id.png")

    fun list(): List<CustomAsset> {
        known?.let { return it }
        val loaded = runCatching {
            if (!indexFile.exists()) return@runCatching emptyList<CustomAsset>()
            json.decodeFromString(CustomLibrary.serializer(), indexFile.readText())
                .assets
                .sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
        known = loaded
        return loaded
    }

    private fun writeAll(assets: List<CustomAsset>) {
        known = assets.sortedByDescending { it.createdAt }
        runCatching {
            indexFile.writeText(json.encodeToString(CustomLibrary.serializer(), CustomLibrary(assets)))
        }
    }

    /** Добавить или изменить заготовку. */
    fun save(asset: CustomAsset) {
        val rest = list().filter { it.id != asset.id }
        writeAll(rest + asset)
    }

    fun delete(id: String) {
        writeAll(list().filter { it.id != id })
        runCatching { imageFile(id).delete() }
        cache.remove(id)
    }

    fun byId(id: String): CustomAsset? = list().firstOrNull { it.id == id }

    override fun asset(id: String): CustomAsset? = byId(id)

    override fun bitmap(id: String): Bitmap? = texture(id)

    /**
     * Перенести выбранную картинку в библиотеку.
     * Большие картинки ужимаются: на карте всё равно не видно больше.
     */
    fun importImage(uri: Uri, id: String): Boolean = runCatching {
        val limit = 768
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        val side = max(opts.outWidth, opts.outHeight)
        if (side <= 0) return false
        var sample = 1
        while (side / sample > limit * 2) sample *= 2
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        val source = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        } ?: return false

        val longest = max(source.width, source.height)
        val bitmap = if (longest > limit) {
            val ratio = limit.toFloat() / longest
            Bitmap.createScaledBitmap(
                source,
                max(1, (source.width * ratio).roundToInt()),
                max(1, (source.height * ratio).roundToInt()),
                true
            )
        } else {
            source
        }

        FileOutputStream(imageFile(id)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        cache.remove(id)
        true
    }.getOrDefault(false)

    /** Картинка заготовки. Читается один раз и держится в памяти. */
    fun texture(id: String): Bitmap? {
        if (cache.containsKey(id)) return cache[id]
        val file = imageFile(id)
        val bitmap = if (file.exists()) {
            runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
        } else {
            null
        }
        cache[id] = bitmap
        return bitmap
    }

    /** Средний цвет картинки — им заливается место, если картинку не видно. */
    fun averageColor(id: String): Int? {
        val bitmap = texture(id) ?: return null
        val step = max(1, max(bitmap.width, bitmap.height) / 24)
        var r = 0L
        var g = 0L
        var b = 0L
        var count = 0
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                if ((pixel ushr 24) > 40) {
                    r += (pixel shr 16) and 0xFF
                    g += (pixel shr 8) and 0xFF
                    b += pixel and 0xFF
                    count++
                }
                x += step
            }
            y += step
        }
        if (count == 0) return null
        return (0xFF shl 24) or
            ((r / count).toInt() shl 16) or
            ((g / count).toInt() shl 8) or
            (b / count).toInt()
    }
}
