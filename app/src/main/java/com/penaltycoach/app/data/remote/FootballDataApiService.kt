package com.penaltycoach.app.data.remote

import com.penaltycoach.app.data.remote.dto.MatchesResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the football-data.org v4 matches endpoint.
 *
 * ONLY the /matches endpoint is used. No odds, predictions, bookmaker, or
 * live-betting endpoints exist here by design. The X-Auth-Token header is
 * attached centrally by an OkHttp interceptor (see FootballDataRepository),
 * so it is not part of this interface.
 */
interface FootballDataApiService {

    @GET("matches")
    suspend fun getMatches(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): Response<MatchesResponseDto>
}
