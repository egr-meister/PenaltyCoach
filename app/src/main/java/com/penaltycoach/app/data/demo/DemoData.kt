package com.penaltycoach.app.data.demo

import com.penaltycoach.app.data.model.MatchSource
import com.penaltycoach.app.data.model.NormalizedMatch
import com.penaltycoach.app.util.DateUtils

/**
 * Static, offline demo fixtures shown when there is no API token and no cache
 * (or whenever "use demo data" is enabled). Team and competition names are
 * plain, generic placeholders — no official logos, branding, or real player
 * data. Dates are generated relative to today so the demo always looks current.
 */
object DemoData {

    fun matches(): List<NormalizedMatch> {
        val d0 = DateUtils.today()
        val d1 = DateUtils.datePlusDays(1)
        val d2 = DateUtils.datePlusDays(2)
        val d4 = DateUtils.datePlusDays(4)
        val d6 = DateUtils.datePlusDays(6)

        return listOf(
            NormalizedMatch(
                id = "demo-1",
                date = d0, time = "18:30",
                competitionName = "Demo League", competitionCode = "DL",
                homeTeam = "Northside FC", awayTeam = "Riverside United",
                status = "SCHEDULED", source = MatchSource.Demo
            ),
            NormalizedMatch(
                id = "demo-2",
                date = d0, time = "21:00",
                competitionName = "Demo League", competitionCode = "DL",
                homeTeam = "Hilltop Rovers", awayTeam = "Central Athletic",
                status = "SCHEDULED", source = MatchSource.Demo
            ),
            NormalizedMatch(
                id = "demo-3",
                date = d1, time = "16:00",
                competitionName = "Demo Cup", competitionCode = "DC",
                homeTeam = "Eastgate City", awayTeam = "Westend Town",
                status = "SCHEDULED", source = MatchSource.Demo
            ),
            NormalizedMatch(
                id = "demo-4",
                date = d2, time = "19:45",
                competitionName = "Demo League", competitionCode = "DL",
                homeTeam = "Harbor Rangers", awayTeam = "Meadow Park",
                status = "SCHEDULED", source = MatchSource.Demo
            ),
            NormalizedMatch(
                id = "demo-5",
                date = d4, time = "20:00",
                competitionName = "Demo Cup", competitionCode = "DC",
                homeTeam = "Summit Wanderers", awayTeam = "Lakeside FC",
                status = "SCHEDULED", source = MatchSource.Demo
            ),
            NormalizedMatch(
                id = "demo-6",
                date = d6, time = "17:15",
                competitionName = "Demo League", competitionCode = "DL",
                homeTeam = "Old Mill FC", awayTeam = "Greenfield Athletic",
                status = "SCHEDULED", source = MatchSource.Demo
            )
        )
    }
}
