package com.penaltycoach.app.data.remote

import com.penaltycoach.app.BuildConfig
import com.penaltycoach.app.data.demo.DemoData
import com.penaltycoach.app.data.model.MatchSource
import com.penaltycoach.app.data.model.NormalizedMatch
import com.penaltycoach.app.data.remote.dto.MatchDto
import com.penaltycoach.app.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Stable result object returned to the UI. It never throws — every failure mode
 * is captured as [ok] = false with a friendly [error] message, and callers can
 * still fall back to cached/demo data.
 */
data class FootballApiResult(
    val ok: Boolean,
    val matches: List<NormalizedMatch>,
    val error: String,
    val usedDemoData: Boolean
)

/**
 * The single isolated gateway to football-data.org. It reads the token and base
 * URL from BuildConfig, adds the X-Auth-Token header, calls only /matches, and
 * normalizes the response into [NormalizedMatch]. All exceptions are caught and
 * converted into friendly messages; the raw token is never logged.
 */
class FootballDataRepository {

    private val placeholderToken = "your_api_token_here"

    private val token: String = BuildConfig.FOOTBALL_DATA_API_TOKEN.trim()
    private val baseUrl: String = normalizeBaseUrl(BuildConfig.FOOTBALL_API_BASE_URL)

    val hasToken: Boolean
        get() = token.isNotBlank() && token != placeholderToken

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val service: FootballDataApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                if (hasToken) {
                    // Header attached centrally; token value is never logged.
                    builder.header("X-Auth-Token", token)
                }
                chain.proceed(builder.build())
            }
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FootballDataApiService::class.java)
    }

    /**
     * Fetch matches for the given window. Uses today .. today+9 when the caller
     * passes blank dates. Returns demo data when no token is configured, and a
     * friendly error (ok = false) on any network / parsing failure.
     */
    suspend fun fetchMatches(
        dateFrom: String,
        dateTo: String,
        competitionCode: String = ""
    ): FootballApiResult = withContext(Dispatchers.IO) {
        val from = dateFrom.ifBlank { DateUtils.today() }
        val to = dateTo.ifBlank { DateUtils.defaultDateTo() }

        if (!hasToken) {
            return@withContext FootballApiResult(
                ok = true,
                matches = filterByCompetition(DemoData.matches(), competitionCode),
                error = "API token is not configured. Showing demo matches.",
                usedDemoData = true
            )
        }

        try {
            val response = service.getMatches(dateFrom = from, dateTo = to)
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    val normalized = (body?.matches ?: emptyList())
                        .mapNotNull { normalize(it) }
                    FootballApiResult(
                        ok = true,
                        matches = filterByCompetition(normalized, competitionCode),
                        error = "",
                        usedDemoData = false
                    )
                }
                response.code() == 429 -> FootballApiResult(
                    ok = false,
                    matches = emptyList(),
                    error = "API request limit reached. Please try again later.",
                    usedDemoData = false
                )
                response.code() == 401 || response.code() == 403 -> FootballApiResult(
                    ok = false,
                    matches = emptyList(),
                    error = "API access was rejected. Check your token or plan.",
                    usedDemoData = false
                )
                else -> FootballApiResult(
                    ok = false,
                    matches = emptyList(),
                    error = "Could not load matches (server responded ${response.code()}).",
                    usedDemoData = false
                )
            }
        } catch (e: java.net.UnknownHostException) {
            FootballApiResult(false, emptyList(), "No internet connection.", false)
        } catch (e: java.net.SocketTimeoutException) {
            FootballApiResult(false, emptyList(), "The request timed out. Try again.", false)
        } catch (e: Exception) {
            // Includes malformed / unexpected response shapes.
            FootballApiResult(false, emptyList(), "Could not load the latest matches.", false)
        }
    }

    /** Local, stable competition filter (empty code = no filtering). */
    private fun filterByCompetition(
        matches: List<NormalizedMatch>,
        code: String
    ): List<NormalizedMatch> {
        val c = code.trim()
        if (c.isBlank()) return matches
        return matches.filter { it.competitionCode.equals(c, ignoreCase = true) }
    }

    /** Safely normalize one DTO; returns null only if there is truly no id. */
    private fun normalize(dto: MatchDto): NormalizedMatch? {
        val id = dto.id?.toString() ?: return null
        val (date, time) = DateUtils.localDateTimeFromUtc(dto.utcDate)
        return NormalizedMatch(
            id = id,
            utcDate = dto.utcDate.orEmpty(),
            date = date,
            time = time,
            competitionName = dto.competition?.name?.ifBlank { "Unknown" } ?: "Unknown",
            competitionCode = dto.competition?.code.orEmpty(),
            homeTeam = teamName(dto.homeTeam?.name, dto.homeTeam?.shortName),
            awayTeam = teamName(dto.awayTeam?.name, dto.awayTeam?.shortName),
            status = dto.status?.ifBlank { "SCHEDULED" } ?: "SCHEDULED",
            homeScore = dto.score?.fullTime?.home,
            awayScore = dto.score?.fullTime?.away,
            winner = dto.score?.winner.orEmpty(),
            source = MatchSource.Api
        )
    }

    private fun teamName(name: String?, shortName: String?): String =
        name?.ifBlank { null } ?: shortName?.ifBlank { null } ?: "Unknown"

    private fun normalizeBaseUrl(raw: String): String {
        val base = raw.ifBlank { "https://api.football-data.org/v4" }
        return if (base.endsWith("/")) base else "$base/"
    }
}
