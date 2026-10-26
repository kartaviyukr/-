package ru.velopodmoskovye.data.progress

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.velopodmoskovye.data.content.ContentRepository
import ru.velopodmoskovye.data.db.ActiveRouteEntity
import ru.velopodmoskovye.data.db.AppDatabase
import ru.velopodmoskovye.data.db.RideStatEntity
import ru.velopodmoskovye.data.db.TitleAwardEntity
import ru.velopodmoskovye.data.db.VisitEntity
import ru.velopodmoskovye.domain.progress.EarnedTitle
import ru.velopodmoskovye.domain.progress.Fix
import ru.velopodmoskovye.domain.progress.ProgressCalculator
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val db: AppDatabase,
    private val content: ContentRepository,
) {
    private val dao = db.progressDao()

    val visits: Flow<List<VisitEntity>> = dao.visits()
    val visitedIds: Flow<Set<String>> = visits.map { list -> list.mapTo(HashSet()) { it.stopId } }
    val titles: Flow<List<TitleAwardEntity>> = dao.titles()
    val activeRoute: Flow<ActiveRouteEntity?> = dao.activeRoute()
    val trackedMeters: Flow<Double> = dao.totalMeters()

    suspend fun activeRouteOnce(): ActiveRouteEntity? = dao.activeRouteOnce()

    /**
     * Отмечает точку и пересчитывает звания.
     * Возвращает звания, которые появились впервые (для анимации).
     */
    suspend fun markVisited(stopId: String, destinationId: String, fix: Fix?, method: String, distanceM: Double?): List<EarnedTitle> {
        val now = System.currentTimeMillis()
        return db.withTransaction {
            val inserted = dao.insertVisit(
                VisitEntity(stopId, destinationId, now, fix?.point?.lat, fix?.point?.lon, fix?.accuracyM, method, distanceM),
            )
            if (inserted == -1L) emptyList() else recomputeTitles(now)
        }
    }

    suspend fun recomputeTitles(now: Long = System.currentTimeMillis()): List<EarnedTitle> {
        val pack = content.pack()
        val visited = dao.visitsOnce().mapTo(HashSet()) { it.stopId }
        val earned = ProgressCalculator.earnedTitles(pack.spiral, pack.destinations, visited)
        val known = dao.titleIds().toSet()
        val fresh = earned.filter { it.id !in known }
        dao.insertTitles(fresh.map { TitleAwardEntity(it.id, it.kind.name, it.name, it.description, now, seen = false) })
        return fresh
    }

    suspend fun markTitleSeen(id: String) = dao.markSeen(id)

    suspend fun startRoute(destinationId: String) =
        dao.setActiveRoute(ActiveRouteEntity(destinationId = destinationId, startedAt = System.currentTimeMillis()))

    suspend fun finishRoute() = dao.clearActiveRoute()

    suspend fun addMeters(meters: Double, day: LocalDate = LocalDate.now()) {
        val key = day.toString()
        db.withTransaction {
            val cur = dao.stat(key)?.meters ?: 0.0
            dao.upsertStat(RideStatEntity(key, cur + meters))
        }
    }

    suspend fun resetAll() = db.withTransaction {
        dao.clearVisits()
        dao.clearTitles()
        dao.clearStats()
        dao.clearActiveRoute()
    }
}
