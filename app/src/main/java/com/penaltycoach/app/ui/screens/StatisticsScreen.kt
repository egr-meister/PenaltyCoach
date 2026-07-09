package com.penaltycoach.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.components.EmptyState
import com.penaltycoach.app.ui.components.GoalZoneBoard
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.Pill
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.components.StatTile
import com.penaltycoach.app.ui.components.boardStats
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.GoalGreen
import com.penaltycoach.app.ui.theme.SavedBlue
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.util.DateUtils
import com.penaltycoach.app.util.Stats

@Composable
fun StatisticsScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit
) {
    val data by appViewModel.state.collectAsStateWithLifecycle()
    val shots = data.shots
    val tally = Stats.tally(shots)
    val best = Stats.bestZone(shots)
    val weakest = Stats.weakestZone(shots)
    val mostUsed = Stats.mostUsedZone(shots)
    val today = DateUtils.today()

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(title = "Statistics", subtitle = "Your penalty practice overview", onBack = onBack)

        if (shots.isEmpty()) {
            EmptyState(
                title = "No statistics yet.",
                message = "Record some penalty shots to see your stats here."
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard {
                Text("Overall", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(tally.total.toString(), "Total shots", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                    StatTile("${tally.conversion}%", "Conversion", BrightOrange, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(tally.goals.toString(), "Goals", GoalGreen, Modifier.weight(1f))
                    StatTile(tally.misses.toString(), "Misses", MaterialTheme.colorScheme.error, Modifier.weight(1f))
                    StatTile(tally.saved.toString(), "Saved", SavedBlue, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(Stats.shotsToday(shots, today).size.toString(), "Shots today", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                    StatTile(Stats.shotsThisWeek(shots).size.toString(), "Shots this week", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                }
            }

            SectionCard {
                Text("Result share", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("Goal ${Stats.percentage(tally.goals, tally.total)}%", GoalGreen)
                    Pill("Miss ${Stats.percentage(tally.misses, tally.total)}%", MaterialTheme.colorScheme.error)
                    Pill("Saved ${Stats.percentage(tally.saved, tally.total)}%", SavedBlue)
                }
            }

            SectionCard {
                Text("Zone map", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Goals / shots and conversion per zone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrayText
                )
                Spacer(Modifier.height(12.dp))
                GoalZoneBoard(
                    stats = boardStats(shots),
                    selectedZone = null,
                    onZoneClick = {},
                    showStats = true
                )
                Spacer(Modifier.height(12.dp))
                ZoneLine("Most used zone", mostUsed?.label ?: "—", MaterialTheme.colorScheme.secondary)
                ZoneLine("Best zone", best?.label ?: "—", GoalGreen)
                ZoneLine("Weakest zone", weakest?.label ?: "—", MaterialTheme.colorScheme.error)
            }

            SectionCard {
                Text("By foot", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Stats.byFoot(shots).forEach { fs ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(fs.foot.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "${fs.total} shots • ${fs.conversion}% converted",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryGrayText
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ZoneLine(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = valueColor, fontWeight = FontWeight.Bold)
    }
}
