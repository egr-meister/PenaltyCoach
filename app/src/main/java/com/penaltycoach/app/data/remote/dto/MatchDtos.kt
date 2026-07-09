package com.penaltycoach.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTOs for the football-data.org v4 /matches response.
 *
 * Every field is nullable with a default so a missing or reshaped response can
 * never crash deserialization. Json is configured with ignoreUnknownKeys, so
 * additional fields returned by the API are simply ignored.
 */

@Serializable
data class MatchesResponseDto(
    val matches: List<MatchDto>? = null
)

@Serializable
data class MatchDto(
    val id: Long? = null,
    val utcDate: String? = null,
    val status: String? = null,
    val competition: CompetitionDto? = null,
    val homeTeam: TeamDto? = null,
    val awayTeam: TeamDto? = null,
    val score: ScoreDto? = null
)

@Serializable
data class CompetitionDto(
    val name: String? = null,
    val code: String? = null
)

@Serializable
data class TeamDto(
    val name: String? = null,
    val shortName: String? = null,
    val tla: String? = null
)

@Serializable
data class ScoreDto(
    val winner: String? = null,
    val fullTime: ScoreDetailDto? = null
)

@Serializable
data class ScoreDetailDto(
    val home: Int? = null,
    val away: Int? = null
)
