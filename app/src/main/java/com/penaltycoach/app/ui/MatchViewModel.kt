package com.penaltycoach.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.penaltycoach.app.data.demo.DemoData
import com.penaltycoach.app.data.local.AppRepository
import com.penaltycoach.app.data.model.MatchScheduleCache
import com.penaltycoach.app.data.model.MatchSource
import com.penaltycoach.app.data.model.NormalizedMatch
import com.penaltycoach.app.data.remote.FootballDataRepository
import com.penaltycoach.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for the secondary Match Schedule screen. Owns network access
 * (through [FootballDataRepository]) and caching (through [AppRepository]).
 * It never polls automatically and never refreshes in the background — only on
 * first open (cache-first, one optional fetch if stale) and on manual refresh.
 */
class MatchViewModel(app: Application) : AndroidViewModel(app) {

    private val appRepo = AppRepository(app.applicationContext)
    private val footballRepo = FootballDataRepository()

    data class UiState(
        val loading: Boolean = false,
        val matches: List<NormalizedMatch> = emptyList(),
        val source: MatchSource = MatchSource.Demo,
        val message: String = "",
        val lastUpdated: String = "",
        val dateFrom: String = "",
        val dateTo: String = "",
        val usingDefaultWindow: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val hasToken: Boolean get() = footballRepo.hasToken

    private var loadedOnce = false

    /** Cache-first load on screen open; fetches once if cache is empty or stale. */
    fun initialLoad() {
        if (loadedOnce) return
        loadedOnce = true
        viewModelScope.launch {
            val data = appRepo.appData.first()
            val cache = data.matchScheduleCache
            val (from, to, isDefault) = effectiveWindow()

            if (cache.cachedMatches.isNotEmpty()) {
                _uiState.value = UiState(
                    loading = false,
                    matches = cache.cachedMatches,
                    source = MatchSource.Cache,
                    message = cache.lastError,
                    lastUpdated = cache.lastUpdatedAt,
                    dateFrom = from, dateTo = to, usingDefaultWindow = isDefault
                )
                // Only reach out again if the cached data is not from today.
                if (isStale(cache.lastUpdatedAt)) fetch(showLoading = false)
            } else {
                fetch(showLoading = true)
            }
        }
    }

    /** Manual refresh button — always uses the current selected window. */
    fun refresh() {
        loadedOnce = true
        fetch(showLoading = true)
    }

    private fun fetch(showLoading: Boolean) {
        viewModelScope.launch {
            val data = appRepo.appData.first()
            val settings = data.settings.matchSchedule
            val cache = data.matchScheduleCache
            val (from, to, isDefault) = effectiveWindow()

            // Respect the user's explicit demo/API preferences.
            if (!settings.apiEnabled || settings.useDemoData) {
                val demo = DemoData.matches()
                _uiState.value = UiState(
                    loading = false,
                    matches = demo,
                    source = MatchSource.Demo,
                    message = if (!settings.apiEnabled)
                        "Match API is turned off in settings. Showing demo matches."
                    else "Demo data is enabled in settings.",
                    lastUpdated = cache.lastUpdatedAt,
                    dateFrom = from, dateTo = to, usingDefaultWindow = isDefault
                )
                return@launch
            }

            if (showLoading) {
                _uiState.value = _uiState.value.copy(
                    loading = true, dateFrom = from, dateTo = to, usingDefaultWindow = isDefault
                )
            }

            val result = footballRepo.fetchMatches(from, to, settings.competitionCode)

            when {
                result.usedDemoData -> {
                    _uiState.value = UiState(
                        loading = false,
                        matches = result.matches,
                        source = MatchSource.Demo,
                        message = result.error,
                        lastUpdated = cache.lastUpdatedAt,
                        dateFrom = from, dateTo = to, usingDefaultWindow = isDefault
                    )
                }
                result.ok -> {
                    val now = DateUtils.nowIso()
                    val newCache = MatchScheduleCache(
                        cachedMatches = result.matches,
                        lastUpdatedAt = now,
                        lastError = "",
                        lastDateFrom = from,
                        lastDateTo = to
                    )
                    appRepo.update { it.copy(matchScheduleCache = newCache) }
                    _uiState.value = UiState(
                        loading = false,
                        matches = result.matches,
                        source = MatchSource.Api,
                        message = if (result.matches.isEmpty())
                            "No matches found for this date range." else "",
                        lastUpdated = now,
                        dateFrom = from, dateTo = to, usingDefaultWindow = isDefault
                    )
                }
                else -> {
                    // Failed. Fall back to cache, then demo — never blank/crash.
                    if (cache.cachedMatches.isNotEmpty()) {
                        _uiState.value = UiState(
                            loading = false,
                            matches = cache.cachedMatches,
                            source = MatchSource.Cache,
                            message = "${result.error} Showing cached matches.",
                            lastUpdated = cache.lastUpdatedAt,
                            dateFrom = from, dateTo = to, usingDefaultWindow = isDefault
                        )
                    } else {
                        _uiState.value = UiState(
                            loading = false,
                            matches = DemoData.matches(),
                            source = MatchSource.Demo,
                            message = "${result.error} Showing demo matches.",
                            lastUpdated = cache.lastUpdatedAt,
                            dateFrom = from, dateTo = to, usingDefaultWindow = isDefault
                        )
                    }
                    // Record the last error without discarding cached matches.
                    appRepo.update {
                        it.copy(matchScheduleCache = it.matchScheduleCache.copy(lastError = result.error))
                    }
                }
            }
        }
    }

    /**
     * Resolve the request window: use the user's custom dates when both are
     * present and valid, otherwise the default today .. today+9 (10 days).
     */
    private suspend fun effectiveWindow(): Triple<String, String, Boolean> {
        val settings = appRepo.appData.first().settings.matchSchedule
        val from = settings.dateFrom.trim()
        val to = settings.dateTo.trim()
        val useCustom = from.isNotBlank() && to.isNotBlank() &&
            DateUtils.isValidOrEmptyDate(from) &&
            DateUtils.isValidOrEmptyDate(to) &&
            DateUtils.isRangeValid(from, to)
        return if (useCustom) {
            Triple(from, to, false)
        } else {
            Triple(DateUtils.today(), DateUtils.defaultDateTo(), true)
        }
    }

    private fun isStale(lastUpdatedAt: String): Boolean {
        if (lastUpdatedAt.isBlank()) return true
        return !lastUpdatedAt.startsWith(DateUtils.today())
    }

    /** Called after the user clears the cache so the next open re-fetches. */
    fun resetLoadFlag() {
        loadedOnce = false
    }
}
