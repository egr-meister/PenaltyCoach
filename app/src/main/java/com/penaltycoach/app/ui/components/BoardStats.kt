package com.penaltycoach.app.ui.components

import com.penaltycoach.app.data.model.PenaltyShot
import com.penaltycoach.app.data.model.ShotZone
import com.penaltycoach.app.util.Stats

/** Build the per-zone cell stats the [GoalZoneBoard] needs from a shot list. */
fun boardStats(shots: List<PenaltyShot>): Map<ShotZone, ZoneCellStat> {
    return Stats.byZone(shots).associate { zs ->
        zs.zone to ZoneCellStat(
            total = zs.tally.total,
            goals = zs.tally.goals,
            conversion = zs.tally.conversion
        )
    }
}
