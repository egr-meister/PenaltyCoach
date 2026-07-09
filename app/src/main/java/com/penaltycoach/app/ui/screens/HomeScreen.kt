package com.penaltycoach.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.penaltycoach.app.data.model.ShotZone
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.components.EmptyState
import com.penaltycoach.app.ui.components.GoalZoneBoard
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.components.StatTile
import com.penaltycoach.app.ui.components.boardStats
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.DarkGraphite
import com.penaltycoach.app.ui.theme.GoalGreen
import com.penaltycoach.app.ui.theme.SavedBlue
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.ui.theme.WhiteText
import com.penaltycoach.app.util.DateUtils
import com.penaltycoach.app.util.Stats

@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    onRecordShot: () -> Unit,
    onStartSeries: (Int) -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMatches: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val data by appViewModel.state.collectAsStateWithLifecycle()
    val today = DateUtils.today()
    val todayShots = Stats.shotsToday(data.shots, today)
    val tally = Stats.tally(todayShots)

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(
            title = "PenaltyCoach",
            subtitle = "Penalty training tracker",
            trailing = {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = WhiteText)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = DateUtils.displayDate(today),
                style = MaterialTheme.typography.labelLarge,
                color = SecondaryGrayText
            )

            // Today summary tiles
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    value = tally.total.toString(),
                    caption = "Shots today",
                    accent = DarkGraphite,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "${tally.conversion}%",
                    caption = "Conversion today",
                    accent = BrightOrange,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(value = tally.goals.toString(), caption = "Goals", accent = GoalGreen, modifier = Modifier.weight(1f))
                StatTile(value = tally.misses.toString(), caption = "Misses", accent = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                StatTile(value = tally.saved.toString(), caption = "Saved", accent = SavedBlue, modifier = Modifier.weight(1f))
            }

            // Goal zone board (compact preview) — tap a zone to record a shot.
            SectionCard {
                Text(
                    text = "Goal zone board",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap a zone to record a penalty shot.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrayText
                )
                Spacer(Modifier.height(12.dp))
                GoalZoneBoard(
                    stats = boardStats(todayShots),
                    selectedZone = null,
                    onZoneClick = { _: ShotZone -> onRecordShot() },
                    compact = true,
                    showStats = false
                )
                if (todayShots.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    EmptyState(
                        title = "No penalty shots today.",
                        message = "Choose a zone and record your first shot."
                    )
                }
            }

            // Quick actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction("Record Shot", Icons.Filled.Add, BrightOrange, Modifier.weight(1f), onRecordShot)
                QuickAction("Statistics", Icons.Filled.QueryStats, DarkGraphite, Modifier.weight(1f), onOpenStatistics)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction("5-Shot Series", Icons.Filled.SportsSoccer, GoalGreen, Modifier.weight(1f)) { onStartSeries(5) }
                QuickAction("10-Shot Series", Icons.Filled.SportsSoccer, SavedBlue, Modifier.weight(1f)) { onStartSeries(10) }
            }
            QuickAction("Shot History", Icons.Filled.History, DarkGraphite, Modifier.fillMaxWidth(), onOpenHistory)

            // Secondary Match Schedule card (kept small and calm)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onOpenMatches)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = BrightOrange)
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Match Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Optional reference — upcoming football matches.",
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
private fun QuickAction(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = WhiteText, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = WhiteText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
