package ru.velopodmoskovye.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Отметка на точке. Для ручной отметки обязательно храним координаты и время — защита от «читерства». */
@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey val stopId: String,
    val destinationId: String,
    val visitedAt: Long,
    val lat: Double?,
    val lon: Double?,
    val accuracyM: Double?,
    /** "auto" — геофенс, "manual" — «я здесь» с подтверждением. */
    val method: String,
    val distanceM: Double?,
)

@Entity(tableName = "titles")
data class TitleAwardEntity(
    @PrimaryKey val titleId: String,
    val kind: String,
    val name: String,
    val description: String,
    val awardedAt: Long,
    val seen: Boolean,
)

@Entity(tableName = "active_route")
data class ActiveRouteEntity(
    @PrimaryKey val slot: Int = 0,
    val destinationId: String,
    val startedAt: Long,
)

/** Пройденные по GPS метры за день (только при активном маршруте). */
@Entity(tableName = "ride_stats")
data class RideStatEntity(
    @PrimaryKey val day: String,
    val meters: Double,
)

@Dao
interface ProgressDao {
    @Query("SELECT * FROM visits ORDER BY visitedAt")
    fun visits(): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits")
    suspend fun visitsOnce(): List<VisitEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVisit(v: VisitEntity): Long

    @Query("DELETE FROM visits WHERE stopId = :stopId")
    suspend fun deleteVisit(stopId: String)

    @Query("SELECT * FROM titles ORDER BY awardedAt DESC")
    fun titles(): Flow<List<TitleAwardEntity>>

    @Query("SELECT titleId FROM titles")
    suspend fun titleIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTitles(t: List<TitleAwardEntity>)

    @Query("UPDATE titles SET seen = 1 WHERE titleId = :id")
    suspend fun markSeen(id: String)

    @Query("SELECT * FROM active_route WHERE slot = 0")
    fun activeRoute(): Flow<ActiveRouteEntity?>

    @Query("SELECT * FROM active_route WHERE slot = 0")
    suspend fun activeRouteOnce(): ActiveRouteEntity?

    @Upsert
    suspend fun setActiveRoute(r: ActiveRouteEntity)

    @Query("DELETE FROM active_route")
    suspend fun clearActiveRoute()

    @Query("SELECT COALESCE(SUM(meters), 0) FROM ride_stats")
    fun totalMeters(): Flow<Double>

    @Query("SELECT * FROM ride_stats WHERE day = :day")
    suspend fun stat(day: String): RideStatEntity?

    @Upsert
    suspend fun upsertStat(s: RideStatEntity)

    @Query("DELETE FROM visits")
    suspend fun clearVisits()

    @Query("DELETE FROM titles")
    suspend fun clearTitles()

    @Query("DELETE FROM ride_stats")
    suspend fun clearStats()
}

@Database(
    entities = [VisitEntity::class, TitleAwardEntity::class, ActiveRouteEntity::class, RideStatEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
}
