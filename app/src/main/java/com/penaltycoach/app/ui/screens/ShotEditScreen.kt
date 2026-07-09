package com.penaltycoach.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.penaltycoach.app.data.model.ShotFoot
import com.penaltycoach.app.data.model.ShotPower
import com.penaltycoach.app.data.model.ShotResult
import com.penaltycoach.app.data.model.ShotZone
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.components.EmptyState
import com.penaltycoach.app.ui.components.GoalZoneBoard
import com.penaltycoach.app.ui.components.OptionChips
import com.penaltycoach.app.ui.components.OptionOutlinedChips
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.ResultSelector
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.ui.theme.WhiteText
import com.penaltycoach.app.util.DateUtils

@Composable
fun ShotEditScreen(
    appViewModel: AppViewModel,
    shotId: String,
    onBack: () -> Unit
) {
    val data by appViewModel.state.collectAsStateWithLifecycle()
    val shot = data.shots.firstOrNull { it.id == shotId }

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(title = "Edit Shot", onBack = onBack)

        if (shot == null) {
            EmptyState(
                title = "Shot not found",
                message = "This shot may have been deleted. Your other data is safe."
            )
            return@Column
        }

        var zone by remember { mutableStateOf(shot.zone) }
        var result by remember { mutableStateOf(shot.result) }
        var foot by remember { mutableStateOf(shot.foot) }
        var power by remember { mutableStateOf(shot.shotPower) }
        var note by remember { mutableStateOf(shot.note) }
        var showDelete by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Recorded ${DateUtils.displayDate(shot.date)} at ${shot.time}",
                style = MaterialTheme.typography.labelLarge,
                color = SecondaryGrayText
            )

            SectionCard {
                Text("Zone: ${zone.label}", style = MaterialTheme.typography.titleMedium, color = BrightOrange)
                Spacer(Modifier.height(12.dp))
                GoalZoneBoard(
                    stats = emptyMap(),
                    selectedZone = zone,
                    onZoneClick = { zone = it },
                    showStats = false
                )
            }

            SectionCard {
                Text("Result", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                ResultSelector(selected = result, onSelect = { result = it })
            }

            SectionCard {
                Text("Foot", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                OptionChips(ShotFoot.entries, foot, { it.label }, { foot = it })
                Spacer(Modifier.height(16.dp))
                Text("Shot power", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                OptionOutlinedChips(ShotPower.entries, power, { it.label }, { power = it })
            }

            SectionCard {
                Text("Note", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            Button(
                onClick = {
                    appViewModel.updateShot(shot.id, zone, result, foot, power, note.trim())
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrightOrange, contentColor = WhiteText)
            ) { Text("Save changes", style = MaterialTheme.typography.titleMedium) }

            OutlinedButton(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete shot") }

            Spacer(Modifier.height(8.dp))
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                title = { Text("Delete shot?") },
                text = { Text("This penalty shot will be permanently removed.") },
                confirmButton = {
                    Button(
                        onClick = { appViewModel.deleteShot(shot.id); showDelete = false; onBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = WhiteText)
                    ) { Text("Delete") }
                },
                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
            )
        }
    }
}
