package com.penaltycoach.app.data.model

import kotlinx.serialization.Serializable

/**
 * All persistent data models for PenaltyCoach.
 *
 * Everything is a plain @Serializable Kotlin data class so the whole app state
 * can be stored as a single JSON string in DataStore Preferences. Enums carry a
 * human-readable [label] and, where relevant, a compact [shortLabel] for the
 * goal-zone board. No Room, no database — deliberately simple and stable.
 */

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

@Serializable
enum class ShotZone(val label: String, val shortLabel: String, val row: Int, val col: Int) {
    TopLeft("Top Left", "TL", 0, 0),
    TopCenter("Top Center", "TC", 0, 1),
    TopRight("Top Right", "TR", 0, 2),
    MiddleLeft("Middle Left", "ML", 1, 0),
    MiddleCenter("Middle Center", "MC", 1, 1),
    MiddleRight("Middle Right", "MR", 1, 2),
    BottomLeft("Bottom Left", "BL", 2, 0),
    BottomCenter("Bottom Center", "BC", 2, 1),
    BottomRight("Bottom Right", "BR", 2, 2);

    companion object {
        /** Ordered rows for the 3x3 board (top -> bottom). */
        val rows: List<List<ShotZone>> = listOf(
            listOf(TopLeft, TopCenter, TopRight),
            listOf(MiddleLeft, MiddleCenter, MiddleRight),
            listOf(BottomLeft, BottomCenter, BottomRight)
        )

        /** Safe parse: never throws for unknown/blank names. */
        fun fromNameOrNull(name: String?): ShotZone? =
            entries.firstOrNull { it.name == name }
    }
}

@Serializable
enum class ShotResult(val label: String) {
    Goal("Goal"),
    Miss("Miss"),
    Saved("Saved");

    companion object {
        fun fromNameOrDefault(name: String?): ShotResult =
            entries.firstOrNull { it.name == name } ?: Miss
    }
}

@Serializable
enum class ShotFoot(val label: String) {
    Left("Left"),
    Right("Right"),
    Unknown("Unknown");

    companion object {
        fun fromNameOrDefault(name: String?): ShotFoot =
            entries.firstOrNull { it.name == name } ?: Unknown
    }
}

@Serializable
enum class ShotPower(val label: String) {
    Low("Low"),
    Medium("Medium"),
    High("High");

    companion object {
        fun fromNameOrDefault(name: String?): ShotPower =
            entries.firstOrNull { it.name == name } ?: Medium
    }
}

@Serializable
enum class MatchSource(val label: String) {
    Api("Live"),
    Cache("Cached"),
    Demo("Demo")
}

// ---------------------------------------------------------------------------
// Penalty data
// ---------------------------------------------------------------------------

@Serializable
data class PenaltyShot(
    val id: String,
    val date: String,          // YYYY-MM-DD
    val time: String,          // HH:mm
    val zone: ShotZone,
    val result: ShotResult,
    val foot: ShotFoot = ShotFoot.Unknown,
    val shotPower: ShotPower = ShotPower.Medium,
    val note: String = "",
    val seriesId: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class PenaltySeries(
    val id: String,
    val date: String,
    val targetShots: Int,
    val completedShots: Int = 0,
    val shotIds: List<String> = emptyList(),
    val notes: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

// ---------------------------------------------------------------------------
// Match schedule (secondary feature)
// ---------------------------------------------------------------------------

@Serializable
data class NormalizedMatch(
    val id: String,
    val utcDate: String = "",
    val date: String = "",     // YYYY-MM-DD (derived)
    val time: String = "",     // HH:mm (derived)
    val competitionName: String = "Unknown",
    val competitionCode: String = "",
    val homeTeam: String = "Unknown",
    val awayTeam: String = "Unknown",
    val status: String = "SCHEDULED",
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val winner: String = "",
    val source: MatchSource = MatchSource.Demo
)

@Serializable
data class MatchScheduleSettings(
    val apiEnabled: Boolean = true,
    val useDemoData: Boolean = false,
    val dateFrom: String = "",
    val dateTo: String = "",
    val competitionCode: String = ""
)

@Serializable
data class MatchScheduleCache(
    val cachedMatches: List<NormalizedMatch> = emptyList(),
    val lastUpdatedAt: String = "",
    val lastError: String = "",
    val lastDateFrom: String = "",
    val lastDateTo: String = ""
)

// ---------------------------------------------------------------------------
// Settings + root app state
// ---------------------------------------------------------------------------

@Serializable
data class Settings(
    val onboardingCompleted: Boolean = false,
    val compactMode: Boolean = false,
    val matchSchedule: MatchScheduleSettings = MatchScheduleSettings()
)

@Serializable
data class AppData(
    val shots: List<PenaltyShot> = emptyList(),
    val series: List<PenaltySeries> = emptyList(),
    val settings: Settings = Settings(),
    val matchScheduleCache: MatchScheduleCache = MatchScheduleCache()
)
