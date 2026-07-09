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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.penaltycoach.app.data.model.PenaltyShot
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.components.EmptyState
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.Pill
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.components.StatTile
import com.penaltycoach.app.ui.components.resultColor
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.GoalGreen
import com.penaltycoach.app.ui.theme.SavedBlue
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.ui.theme.WhiteText
import com.penaltycoach.app.util.DateUtils
import com.penaltycoach.app.util.Stats

@Composable
fun DayDetailScreen(
    appViewModel: AppViewModel,
    date: String,
    onBack: () -> Unit,
    onEditShot: (String) -> Unit
) {
    val data by appViewModel.state.collectAsStateWithLifecycle()
    val shots = data.shots.filter { it.date == date }.sortedByDescending { it.time }
    val tally = Stats.tally(shots)
    val best = Stats.bestZone(shots)

    var showReset by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(title = "Day Detail", subtitle = DateUtils.displayDate(date), onBack = onBack)

        if (shots.isEmpty()) {
            EmptyState(
                title = "No shots for this day.",
                message = "There are no penalty shots recorded on this date."
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
                Text("Summary", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
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
                Spacer(Modifier.height(10.dp))
                Text("Best zone: ${best?.label ?: "—"}", style = MaterialTheme.typography.labelLarge, color = BrightOrange)
            }

            SectionCard {
                Text("Zone breakdown", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Stats.byZone(shots).filter { it.tally.total > 0 }.forEach { zs ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(zs.zone.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "${zs.tally.goals}/${zs.tally.total} • ${zs.tally.conversion}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryGrayText
                        )
                    }
                }
            }

            Text("Shots", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            shots.forEach { shot ->
                ShotRow(
                    shot = shot,
                    onEdit = { onEditShot(shot.id) },
                    onDelete = { pendingDelete = shot.id }
                )
            }

            OutlinedButton(
                onClick = { showReset = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Reset this day") }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset this day?") },
            text = { Text("This will remove all penalty shots for the selected day.") },
            confirmButton = {
                Button(
                    onClick = {
                        showReset = false
                        appViewModel.deleteShotsForDate(date)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = WhiteText)
                ) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { showReset = false }) { Text("Cancel") } }
        )
    }

    pendingDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete shot?") },
            text = { Text("This penalty shot will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = { appViewModel.deleteShot(id); pendingDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = WhiteText)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ShotRow(
    shot: PenaltyShot,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    SectionCard(modifier = Modifier.clickable(onClick = onEdit)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill(shot.result.label, resultColor(shot.result))
                    Text(
                        text = "   ${shot.zone.label}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${shot.time} • ${shot.foot.label} foot • ${shot.shotPower.label} power",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrayText
                )
                if (shot.note.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "\"${shot.note}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryGrayText
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit shot", tint = BrightOrange)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete shot", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
