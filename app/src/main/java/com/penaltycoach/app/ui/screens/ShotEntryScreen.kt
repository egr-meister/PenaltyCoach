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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.penaltycoach.app.data.model.ShotFoot
import com.penaltycoach.app.data.model.ShotPower
import com.penaltycoach.app.data.model.ShotResult
import com.penaltycoach.app.data.model.ShotZone
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.components.GoalZoneBoard
import com.penaltycoach.app.ui.components.OptionChips
import com.penaltycoach.app.ui.components.OptionOutlinedChips
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.ResultSelector
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.components.boardStats
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.MissRed
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.ui.theme.WhiteText
import com.penaltycoach.app.util.DateUtils
import com.penaltycoach.app.util.Stats

/**
 * Standalone shot recording. Pick a zone on the full board, choose a result,
 * optionally set foot/power/note, then save. Zone + result are required; the
 * validation message appears only after the user tries to save.
 */
@Composable
fun ShotEntryScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val data by appViewModel.state.collectAsStateWithLifecycle()
    val today = DateUtils.today()
    val todayShots = Stats.shotsToday(data.shots, today)

    var selectedZone by remember { mutableStateOf<ShotZone?>(null) }
    var selectedResult by remember { mutableStateOf<ShotResult?>(null) }
    var foot by remember { mutableStateOf(ShotFoot.Unknown) }
    var power by remember { mutableStateOf(ShotPower.Medium) }
    var note by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(title = "Record Shot", subtitle = "Tap a zone, then choose a result", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard {
                Text("Goal zone", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selectedZone?.let { "Selected: ${it.label}" } ?: "No zone selected yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedZone == null) SecondaryGrayText else BrightOrange
                )
                Spacer(Modifier.height(12.dp))
                GoalZoneBoard(
                    stats = boardStats(todayShots),
                    selectedZone = selectedZone,
                    onZoneClick = { selectedZone = it; showError = false },
                    showStats = true
                )
            }

            SectionCard {
                Text("Result", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                ResultSelector(
                    selected = selectedResult,
                    onSelect = { selectedResult = it; showError = false }
                )
            }

            SectionCard {
                Text("Foot", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                OptionChips(
                    options = ShotFoot.entries,
                    selected = foot,
                    label = { it.label },
                    onSelect = { foot = it }
                )
                Spacer(Modifier.height(16.dp))
                Text("Shot power", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                OptionOutlinedChips(
                    options = ShotPower.entries,
                    selected = power,
                    label = { it.label },
                    onSelect = { power = it }
                )
            }

            SectionCard {
                Text("Note (optional)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. clean strike, keeper guessed right") },
                    minLines = 2
                )
            }

            if (showError) {
                Text(
                    text = "Please select both a zone and a result before saving.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MissRed
                )
            }

            Button(
                onClick = {
                    val zone = selectedZone
                    val result = selectedResult
                    if (zone == null || result == null) {
                        showError = true
                    } else {
                        appViewModel.addShot(zone, result, foot, power, note.trim())
                        onSaved()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrightOrange, contentColor = WhiteText)
            ) {
                Text("Save Shot", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
