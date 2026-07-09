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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.penaltycoach.app.data.model.ShotResult
import com.penaltycoach.app.data.model.ShotZone
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.components.GoalZoneBoard
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.ResultSelector
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.components.StatTile
import com.penaltycoach.app.ui.components.boardStats
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.GoalGreen
import com.penaltycoach.app.ui.theme.SavedBlue
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.ui.theme.WhiteText
import com.penaltycoach.app.util.Stats

/**
 * Guided penalty series (5 or 10 shots). A series row is created on entry; each
 * recorded shot is attached to it. When the target is reached we navigate to the
 * summary. Cancelling keeps any shots already recorded (empty series are dropped).
 */
@Composable
fun SeriesScreen(
    appViewModel: AppViewModel,
    target: Int,
    onBack: () -> Unit,
    onFinished: (String) -> Unit
) {
    val safeTarget = if (target == 10) 10 else 5
    // Create the series exactly once for this screen instance.
    val seriesId = remember { appViewModel.startSeries(safeTarget) }

    val data by appViewModel.state.collectAsStateWithLifecycle()
    val series = data.series.firstOrNull { it.id == seriesId }
    val seriesShots = data.shots.filter { it.seriesId == seriesId }
    val completed = seriesShots.size
    val tally = Stats.tally(seriesShots)

    var selectedZone by remember { mutableStateOf<ShotZone?>(null) }
    var hint by remember { mutableStateOf(false) }
    var showCancel by remember { mutableStateOf(false) }
    var navigated by remember { mutableStateOf(false) }

    // Auto-advance to the summary once the target is met.
    LaunchedEffect(completed) {
        if (completed >= safeTarget && !navigated) {
            navigated = true
            onFinished(seriesId)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(
            title = "$safeTarget-Shot Series",
            subtitle = if (completed >= safeTarget) "Series complete" else "Shot ${completed + 1} of $safeTarget",
            onBack = { showCancel = true }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard {
                Text(
                    text = "Progress: $completed / $safeTarget",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { if (safeTarget == 0) 0f else (completed.toFloat() / safeTarget).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = BrightOrange
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(tally.goals.toString(), "Goals", GoalGreen, Modifier.weight(1f))
                    StatTile(tally.misses.toString(), "Misses", MaterialTheme.colorScheme.error, Modifier.weight(1f))
                    StatTile(tally.saved.toString(), "Saved", SavedBlue, Modifier.weight(1f))
                }
            }

            SectionCard {
                Text(
                    text = selectedZone?.let { "Zone: ${it.label}" } ?: "Tap a zone for this shot",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selectedZone == null) SecondaryGrayText else BrightOrange
                )
                Spacer(Modifier.height(12.dp))
                GoalZoneBoard(
                    stats = boardStats(seriesShots),
                    selectedZone = selectedZone,
                    onZoneClick = { selectedZone = it; hint = false }
                )
            }

            SectionCard {
                Text("Record result", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                ResultSelector(
                    selected = null,
                    onSelect = { result: ShotResult ->
                        val zone = selectedZone
                        if (zone == null) {
                            hint = true
                        } else if (completed < safeTarget) {
                            appViewModel.addShot(zone = zone, result = result, seriesId = seriesId)
                            selectedZone = null
                        }
                    }
                )
                if (hint) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Select a zone first, then choose the result.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            OutlinedButton(
                onClick = { showCancel = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (completed > 0) "Finish early" else "Cancel series")
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showCancel) {
        AlertDialog(
            onDismissRequest = { showCancel = false },
            title = { Text(if (completed > 0) "Finish series early?" else "Cancel series?") },
            text = {
                Text(
                    if (completed > 0)
                        "You have recorded $completed of $safeTarget shots. They will be saved and you'll see the summary."
                    else
                        "No shots recorded yet. This series will be discarded."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancel = false
                        if (completed > 0) {
                            navigated = true
                            onFinished(seriesId)
                        } else {
                            appViewModel.discardSeriesIfEmpty(seriesId)
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightOrange, contentColor = WhiteText)
                ) { Text(if (completed > 0) "Finish" else "Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showCancel = false }) { Text("Keep going") }
            }
        )
    }
}
