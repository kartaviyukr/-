package com.fantasymap.creator.data

import android.content.Context
import com.fantasymap.creator.model.MapProject
import com.fantasymap.creator.model.ProjectSummary
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/** Файловое хранилище проектов карт: каждый проект — отдельный JSON во внутренней памяти. */
class ProjectStore(context: Context) {

    private val dir: File = File(context.filesDir, "projects").apply { mkdirs() }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val prettyJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        prettyPrint = true
    }

    private fun fileFor(id: String) = File(dir, "$id.json")

    fun list(): List<ProjectSummary> {
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".json") } ?: return emptyList()
        return files.mapNotNull { file ->
            runCatching {
                val project = json.decodeFromString(MapProject.serializer(), file.readText())
                ProjectSummary(
                    id = project.id,
                    name = project.name,
                    updatedAt = project.updatedAt,
                    stage = project.stage,
                    landCount = project.landmasses.size,
                    markerCount = project.markers.size,
                    countryCount = project.countries.size
                )
            }.getOrNull()
        }.sortedByDescending { it.updatedAt }
    }

    fun load(id: String): MapProject? = runCatching {
        val file = fileFor(id)
        if (!file.exists()) return null
        json.decodeFromString(MapProject.serializer(), file.readText())
    }.getOrNull()

    fun save(project: MapProject) {
        runCatching {
            val target = fileFor(project.id)
            val tmp = File(dir, "${project.id}.tmp")
            tmp.writeText(json.encodeToString(MapProject.serializer(), project))
            if (target.exists()) target.delete()
            if (!tmp.renameTo(target)) {
                target.writeText(tmp.readText())
                tmp.delete()
            }
        }
    }

    fun delete(id: String) {
        runCatching { fileFor(id).delete() }
    }

    fun duplicate(id: String): MapProject? {
        val original = load(id) ?: return null
        val copy = original.copy(
            id = UUID.randomUUID().toString(),
            name = original.name + " (копия)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        save(copy)
        return copy
    }

    /** Текст для экспорта проекта в файл. */
    fun toJsonText(project: MapProject): String =
        prettyJson.encodeToString(MapProject.serializer(), project)

    /** Разбор импортируемого файла. Проекту выдаётся новый идентификатор. */
    fun importFromText(text: String): MapProject? = runCatching {
        val parsed = json.decodeFromString(MapProject.serializer(), text)
        val imported = parsed.copy(
            id = UUID.randomUUID().toString(),
            updatedAt = System.currentTimeMillis()
        )
        save(imported)
        imported
    }.getOrNull()
}
