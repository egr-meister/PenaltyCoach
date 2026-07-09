package com.penaltycoach.app.util

import com.penaltycoach.app.data.model.PenaltyShot
import com.penaltycoach.app.data.model.ShotFoot
import com.penaltycoach.app.data.model.ShotResult
import com.penaltycoach.app.data.model.ShotZone

/**
 * Pure, side-effect-free statistics helpers. All functions tolerate empty input
 * and never divide by zero. Nothing here makes performance, medical, or coaching
 * claims — it is simple counting and percentages over the user's own logged shots.
 */
object Stats {

    data class Tally(
        val total: Int = 0,
        val goals: Int = 0,
        val misses: Int = 0,
        val saved: Int = 0
    ) {
        val conversion: Int get() = percentage(goals, total)
    }

    data class ZoneStat(
        val zone: ShotZone,
        val tally: Tally
    )

    /** Integer percentage of [part] out of [whole]; 0 when [whole] is 0. */
    fun percentage(part: Int, whole: Int): Int =
        if (whole <= 0) 0 else Math.round(part * 100f / whole)

    fun tally(shots: List<PenaltyShot>): Tally {
        var goals = 0
        var misses = 0
        var saved = 0
        for (s in shots) {
            when (s.result) {
                ShotResult.Goal -> goals++
                ShotResult.Miss -> misses++
                ShotResult.Saved -> saved++
            }
        }
        return Tally(total = shots.size, goals = goals, misses = misses, saved = saved)
    }

    /** Per-zone tallies for every zone (zones with no shots return an empty Tally). */
    fun byZone(shots: List<PenaltyShot>): List<ZoneStat> {
        val grouped = shots.groupBy { it.zone }
        return ShotZone.entries.map { zone ->
            ZoneStat(zone, tally(grouped[zone].orEmpty()))
        }
    }

    /** Zone with the highest conversion among zones that have at least one shot. */
    fun bestZone(shots: List<PenaltyShot>): ShotZone? =
        byZone(shots)
            .filter { it.tally.total > 0 }
            .maxWithOrNull(
                compareBy({ it.tally.conversion }, { it.tally.total })
            )?.zone

    /** Zone with the lowest conversion among zones that have at least one shot. */
    fun weakestZone(shots: List<PenaltyShot>): ShotZone? =
        byZone(shots)
            .filter { it.tally.total > 0 }
            .minWithOrNull(
                compareBy({ it.tally.conversion }, { -it.tally.total })
            )?.zone

    /** Zone with the most shots recorded (most practiced). */
    fun mostUsedZone(shots: List<PenaltyShot>): ShotZone? =
        byZone(shots)
            .filter { it.tally.total > 0 }
            .maxByOrNull { it.tally.total }
            ?.zone

    data class FootStat(
        val foot: ShotFoot,
        val total: Int,
        val goals: Int
    ) {
        val conversion: Int get() = percentage(goals, total)
    }

    fun byFoot(shots: List<PenaltyShot>): List<FootStat> {
        val grouped = shots.groupBy { it.foot }
        return ShotFoot.entries.map { foot ->
            val list = grouped[foot].orEmpty()
            FootStat(foot, list.size, list.count { it.result == ShotResult.Goal })
        }
    }

    fun shotsToday(shots: List<PenaltyShot>, today: String): List<PenaltyShot> =
        shots.filter { it.date == today }

    fun shotsThisWeek(shots: List<PenaltyShot>): List<PenaltyShot> =
        shots.filter { DateUtils.isWithinLastWeek(it.date) }
}
