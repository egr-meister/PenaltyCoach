package com.penaltycoach.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.components.EmptyState
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.Pill
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.GoalGreen
import com.penaltycoach.app.ui.theme.SavedBlue
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.util.DateUtils
import com.penaltycoach.app.util.Stats

@Composable
fun HistoryScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenDay: (String) -> Unit
) {
    val data by appViewModel.state.collectAsStateWithLifecycle()

    // Group by date, newest first.
    val days = data.shots
        .groupBy { it.date }
        .toList()
        .sortedByDescending { it.first }

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(title = "Shot History", subtitle = "Daily penalty practice", onBack = onBack)

        if (days.isEmpty()) {
            EmptyState(
                title = "No shot history yet.",
                message = "Record penalty shots and they'll appear here, grouped by day."
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(days, key = { it.first }) { (date, shots) ->
                val tally = Stats.tally(shots)
                val best = Stats.bestZone(shots)
                SectionCard(modifier = Modifier.clickable { onOpenDay(date) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = DateUtils.displayDate(date),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${tally.total} shots • ${tally.conversion}% converted",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryGrayText
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Pill("${tally.goals} Goal", GoalGreen)
                                Pill("${tally.misses} Miss", MaterialTheme.colorScheme.error)
                                Pill("${tally.saved} Saved", SavedBlue)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Best zone: ${best?.label ?: "—"}",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrightOrange
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open day",
                            tint = SecondaryGrayText
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
