package com.penaltycoach.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.penaltycoach.app.data.model.AppData
import com.penaltycoach.app.data.model.MatchScheduleCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

// A single DataStore Preferences file backs the whole app. We store the entire
// AppData tree as one JSON string; this keeps persistence trivial and avoids a
// database. All reads merge with defaults and recover from corrupted JSON.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "penaltycoach_store")

/**
 * The single local repository for all user data (shots, series, settings, and
 * the cached match schedule). Nothing here touches the network.
 */
class AppRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true   // tolerate schema changes / missing fields
        encodeDefaults = true
        isLenient = true
    }

    private object Keys {
        val APP_DATA = stringPreferencesKey("app_data_json")
    }

    /** Emits the current [AppData], always non-null, always safely defaulted. */
    val appData: Flow<AppData> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs -> decode(prefs[Keys.APP_DATA]) }

    private fun decode(raw: String?): AppData {
        if (raw.isNullOrBlank()) return AppData()
        return try {
            json.decodeFromString(AppData.serializer(), raw)
        } catch (e: Exception) {
            // Corrupted JSON -> never crash, just start from safe defaults.
            AppData()
        }
    }

    /**
     * Atomically read-modify-write the whole tree. The [transform] receives the
     * current (safely decoded) state and returns the new state.
     */
    suspend fun update(transform: (AppData) -> AppData) {
        context.dataStore.edit { prefs ->
            val current = decode(prefs[Keys.APP_DATA])
            val next = transform(current)
            prefs[Keys.APP_DATA] = json.encodeToString(AppData.serializer(), next)
        }
    }

    suspend fun clearMatchCache() = update { it.copy(matchScheduleCache = MatchScheduleCache()) }

    suspend fun deleteAllShots() = update { it.copy(shots = emptyList()) }

    suspend fun deleteAllSeries() = update { it.copy(series = emptyList()) }

    /** Wipes everything back to first-run defaults. */
    suspend fun resetAll() {
        context.dataStore.edit { prefs -> prefs.remove(Keys.APP_DATA) }
    }
}
