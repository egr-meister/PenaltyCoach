package com.penaltycoach.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.penaltycoach.app.data.model.MatchSource
import com.penaltycoach.app.data.model.NormalizedMatch
import com.penaltycoach.app.ui.MatchViewModel
import com.penaltycoach.app.ui.components.EmptyState
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.Pill
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.DarkGraphite
import com.penaltycoach.app.ui.theme.GoalGreen
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.ui.theme.WarningYellow
import com.penaltycoach.app.ui.theme.WhiteText
import com.penaltycoach.app.util.DateUtils

@Composable
fun MatchScheduleScreen(
    matchViewModel: MatchViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val ui by matchViewModel.uiState.collectAsStateWithLifecycle()

    // Cache-first load on open; fetches once if empty/stale. Never auto-polls.
    LaunchedEffect(Unit) { matchViewModel.initialLoad() }

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(
            title = "Match Schedule",
            subtitle = "Optional reference feature",
            onBack = onBack,
            trailing = {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Match settings", tint = WhiteText)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            val windowLabel = if (ui.usingDefaultWindow) "Today + 9 days" else "Custom window"
                            Text(
                                text = "Window: $windowLabel",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${ui.dateFrom.ifBlank { "—" }} → ${ui.dateTo.ifBlank { "—" }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryGrayText
                            )
                            if (ui.lastUpdated.isNotBlank()) {
                                Text(
                                    text = "Last updated: ${ui.lastUpdated.take(16).replace('T', ' ')}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecondaryGrayText
                                )
                            }
                        }
                        IconButton(onClick = { matchViewModel.refresh() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = BrightOrange)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SourcePill(ui.source)
                }
            }

            if (ui.message.isNotBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        SectionCard {
                            Text(
                                text = ui.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkGraphite
                            )
                        }
                    }
                }
            }

            if (ui.loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = BrightOrange) }
                }
            } else if (ui.matches.isEmpty()) {
                item {
                    EmptyState(
                        title = "No matches available.",
                        message = "Try refreshing or check API settings."
                    )
                }
            } else {
                items(ui.matches, key = { it.id }) { match -> MatchCard(match) }
            }

            item {
                SectionCard {
                    Text(
                        text = "Match data is provided by football-data.org and may depend on your API plan. Availability, accuracy, competitions, and update frequency depend on the API provider and the current API plan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryGrayText
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun SourcePill(source: MatchSource) {
    val (label, color) = when (source) {
        MatchSource.Api -> "Live data" to GoalGreen
        MatchSource.Cache -> "Cached data" to BrightOrange
        MatchSource.Demo -> "Demo data" to WarningYellow
    }
    Pill(label, color, textColor = if (source == MatchSource.Demo) DarkGraphite else WhiteText)
}

@Composable
private fun MatchCard(match: NormalizedMatch) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${match.homeTeam} vs ${match.awayTeam}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                val comp = buildString {
                    append(match.competitionName)
                    if (match.competitionCode.isNotBlank()) append(" (${match.competitionCode})")
                }
                Text(comp, style = MaterialTheme.typography.bodyMedium, color = SecondaryGrayText)
                val dateLine = buildString {
                    append(if (match.date.isNotBlank()) DateUtils.displayDate(match.date) else "Date unknown")
                    if (match.time.isNotBlank()) append(" • ${match.time}")
                }
                Text(dateLine, style = MaterialTheme.typography.bodyMedium, color = SecondaryGrayText)
                Spacer(Modifier.height(6.dp))
                Pill(statusLabel(match.status), DarkGraphite)
            }
            // Show a score only when the API actually provided one.
            if (match.homeScore != null && match.awayScore != null) {
                Text(
                    text = "${match.homeScore} : ${match.awayScore}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrightOrange
                )
            }
        }
    }
}

private fun statusLabel(status: String): String = when (status.uppercase()) {
    "SCHEDULED", "TIMED" -> "Scheduled"
    "IN_PLAY" -> "In play"
    "PAUSED" -> "Paused"
    "FINISHED" -> "Finished"
    "POSTPONED" -> "Postponed"
    "SUSPENDED" -> "Suspended"
    "CANCELLED", "CANCELED" -> "Cancelled"
    else -> if (status.isBlank()) "Scheduled" else status
}
