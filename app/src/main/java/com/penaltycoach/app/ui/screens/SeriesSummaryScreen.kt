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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.components.EmptyState
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.components.StatTile
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.GoalGreen
import com.penaltycoach.app.ui.theme.SavedBlue
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.ui.theme.WhiteText
import com.penaltycoach.app.util.DateUtils
import com.penaltycoach.app.util.Stats

@Composable
fun SeriesSummaryScreen(
    appViewModel: AppViewModel,
    seriesId: String,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val data by appViewModel.state.collectAsStateWithLifecycle()
    val series = data.series.firstOrNull { it.id == seriesId }
    val shots = data.shots.filter { it.seriesId == seriesId }

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(title = "Series Summary", onBack = onBack)

        if (series == null) {
            EmptyState(
                title = "Series not found",
                message = "This series is no longer available. Your other data is safe."
            )
            return@Column
        }

        val tally = Stats.tally(shots)
        val best = Stats.bestZone(shots)
        val weakest = Stats.weakestZone(shots)
        var notes by remember { mutableStateOf(series.notes) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "${series.targetShots}-shot series • ${DateUtils.displayDate(series.date)}",
                style = MaterialTheme.typography.labelLarge,
                color = SecondaryGrayText
            )

            SectionCard {
                Text("Result", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(tally.total.toString(), "Shots", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                    StatTile("${tally.conversion}%", "Conversion", BrightOrange, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(tally.goals.toString(), "Goals", GoalGreen, Modifier.weight(1f))
                    StatTile(tally.misses.toString(), "Misses", MaterialTheme.colorScheme.error, Modifier.weight(1f))
                    StatTile(tally.saved.toString(), "Saved", SavedBlue, Modifier.weight(1f))
                }
            }

            SectionCard {
                Text("Zones", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Best zone: ${best?.label ?: "—"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoalGreen
                )
                Text(
                    "Weakest zone: ${weakest?.label ?: "—"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }

            SectionCard {
                Text("Series notes", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What did you work on? What went well?") },
                    minLines = 2
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { appViewModel.setSeriesNotes(seriesId, notes.trim()) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightOrange, contentColor = WhiteText)
                ) { Text("Save notes") }
            }

            OutlinedButton(
                onClick = onOpenHistory,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Go to History") }

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = WhiteText)
            ) { Text("Done") }

            Spacer(Modifier.height(8.dp))
        }
    }
}
