package com.penaltycoach.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.penaltycoach.app.data.local.AppRepository
import com.penaltycoach.app.data.model.AppData
import com.penaltycoach.app.data.model.MatchScheduleSettings
import com.penaltycoach.app.data.model.PenaltySeries
import com.penaltycoach.app.data.model.PenaltyShot
import com.penaltycoach.app.data.model.ShotFoot
import com.penaltycoach.app.data.model.ShotPower
import com.penaltycoach.app.data.model.ShotResult
import com.penaltycoach.app.data.model.ShotZone
import com.penaltycoach.app.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Single shared ViewModel for all penalty data + settings. It is intentionally
 * the one source of truth for the UI, backed by [AppRepository] (DataStore).
 * All mutations are read-modify-write against the persisted [AppData], so state
 * is always consistent across screens.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(app.applicationContext)

    val state: StateFlow<AppData> = repo.appData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppData()
    )

    /**
     * Becomes true after the first real DataStore emission. Used to keep the
     * splash screen up until stored state is loaded, avoiding an onboarding
     * "flash" on cold start.
     */
    val ready: StateFlow<Boolean> = repo.appData
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    // -------------------------------------------------------------------
    // Onboarding / settings
    // -------------------------------------------------------------------

    fun completeOnboarding() = viewModelScope.launch {
        repo.update { it.copy(settings = it.settings.copy(onboardingCompleted = true)) }
    }

    fun showOnboardingAgain() = viewModelScope.launch {
        repo.update { it.copy(settings = it.settings.copy(onboardingCompleted = false)) }
    }

    fun setCompactMode(enabled: Boolean) = viewModelScope.launch {
        repo.update { it.copy(settings = it.settings.copy(compactMode = enabled)) }
    }

    fun updateMatchSettings(newSettings: MatchScheduleSettings) = viewModelScope.launch {
        repo.update { it.copy(settings = it.settings.copy(matchSchedule = newSettings)) }
    }

    // -------------------------------------------------------------------
    // Shots
    // -------------------------------------------------------------------

    /**
     * Create and persist a new shot. If [seriesId] is provided, the matching
     * series is updated (completedShots + shotIds) in the same write.
     */
    fun addShot(
        zone: ShotZone,
        result: ShotResult,
        foot: ShotFoot = ShotFoot.Unknown,
        power: ShotPower = ShotPower.Medium,
        note: String = "",
        seriesId: String? = null
    ): String {
        val now = DateUtils.nowIso()
        val id = UUID.randomUUID().toString()
        val shot = PenaltyShot(
            id = id,
            date = DateUtils.today(),
            time = DateUtils.nowTime(),
            zone = zone,
            result = result,
            foot = foot,
            shotPower = power,
            note = note,
            seriesId = seriesId,
            createdAt = now,
            updatedAt = now
        )
        viewModelScope.launch {
            repo.update { data ->
                val newShots = data.shots + shot
                val newSeries = if (seriesId == null) data.series else data.series.map { s ->
                    if (s.id == seriesId) {
                        s.copy(
                            completedShots = s.completedShots + 1,
                            shotIds = s.shotIds + id,
                            updatedAt = now
                        )
                    } else s
                }
                data.copy(shots = newShots, series = newSeries)
            }
        }
        return id
    }

    fun updateShot(
        id: String,
        zone: ShotZone,
        result: ShotResult,
        foot: ShotFoot,
        power: ShotPower,
        note: String
    ) = viewModelScope.launch {
        val now = DateUtils.nowIso()
        repo.update { data ->
            data.copy(shots = data.shots.map { s ->
                if (s.id == id) s.copy(
                    zone = zone, result = result, foot = foot,
                    shotPower = power, note = note, updatedAt = now
                ) else s
            })
        }
    }

    fun deleteShot(id: String) = viewModelScope.launch {
        repo.update { data ->
            val shot = data.shots.firstOrNull { it.id == id }
            val newShots = data.shots.filterNot { it.id == id }
            val newSeries = if (shot?.seriesId == null) data.series else data.series.map { s ->
                if (s.id == shot.seriesId) s.copy(
                    completedShots = (s.completedShots - 1).coerceAtLeast(0),
                    shotIds = s.shotIds.filterNot { it == id }
                ) else s
            }
            data.copy(shots = newShots, series = newSeries)
        }
    }

    fun deleteShotsForDate(date: String) = viewModelScope.launch {
        repo.update { data ->
            val removedIds = data.shots.filter { it.date == date }.map { it.id }.toSet()
            data.copy(
                shots = data.shots.filterNot { it.date == date },
                series = data.series.map { s ->
                    val remaining = s.shotIds.filterNot { it in removedIds }
                    s.copy(shotIds = remaining, completedShots = remaining.size)
                }
            )
        }
    }

    // -------------------------------------------------------------------
    // Series
    // -------------------------------------------------------------------

    /** Start a new series and return its id immediately for navigation. */
    fun startSeries(target: Int): String {
        val now = DateUtils.nowIso()
        val id = UUID.randomUUID().toString()
        val series = PenaltySeries(
            id = id,
            date = DateUtils.today(),
            targetShots = target,
            completedShots = 0,
            shotIds = emptyList(),
            notes = "",
            createdAt = now,
            updatedAt = now
        )
        viewModelScope.launch {
            repo.update { it.copy(series = it.series + series) }
        }
        return id
    }

    fun setSeriesNotes(id: String, notes: String) = viewModelScope.launch {
        repo.update { data ->
            data.copy(series = data.series.map { s ->
                if (s.id == id) s.copy(notes = notes, updatedAt = DateUtils.nowIso()) else s
            })
        }
    }

    /** Remove an empty/abandoned series so it does not clutter history. */
    fun discardSeriesIfEmpty(id: String) = viewModelScope.launch {
        repo.update { data ->
            val s = data.series.firstOrNull { it.id == id }
            if (s != null && s.shotIds.isEmpty()) {
                data.copy(series = data.series.filterNot { it.id == id })
            } else data
        }
    }

    // -------------------------------------------------------------------
    // Bulk data management
    // -------------------------------------------------------------------

    fun clearMatchCache() = viewModelScope.launch { repo.clearMatchCache() }
    fun deleteAllShots() = viewModelScope.launch { repo.deleteAllShots() }
    fun deleteAllSeries() = viewModelScope.launch { repo.deleteAllSeries() }
    fun resetAllData() = viewModelScope.launch { repo.resetAll() }
}
