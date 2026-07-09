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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.penaltycoach.app.data.model.MatchScheduleSettings
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.MatchViewModel
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.GoalGreen
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.ui.theme.WhiteText
import com.penaltycoach.app.util.DateUtils

@Composable
fun MatchSettingsScreen(
    appViewModel: AppViewModel,
    matchViewModel: MatchViewModel,
    onBack: () -> Unit
) {
    val data by appViewModel.state.collectAsStateWithLifecycle()
    val current = data.settings.matchSchedule

    var apiEnabled by remember { mutableStateOf(current.apiEnabled) }
    var useDemo by remember { mutableStateOf(current.useDemoData) }
    var dateFrom by remember { mutableStateOf(current.dateFrom) }
    var dateTo by remember { mutableStateOf(current.dateTo) }
    var competition by remember { mutableStateOf(current.competitionCode) }
    var error by remember { mutableStateOf<String?>(null) }
    var savedMsg by remember { mutableStateOf(false) }

    fun validate(): String? {
        if (!DateUtils.isValidOrEmptyDate(dateFrom)) return "dateFrom must be empty or a valid YYYY-MM-DD date."
        if (!DateUtils.isValidOrEmptyDate(dateTo)) return "dateTo must be empty or a valid YYYY-MM-DD date."
        if (!DateUtils.isRangeValid(dateFrom, dateTo)) return "dateTo must not be earlier than dateFrom."
        return null
    }

    fun persist() {
        savedMsg = false
        val err = validate()
        if (err != null) { error = err; return }
        error = null
        appViewModel.updateMatchSettings(
            MatchScheduleSettings(
                apiEnabled = apiEnabled,
                useDemoData = useDemo,
                dateFrom = dateFrom.trim(),
                dateTo = dateTo.trim(),
                competitionCode = competition.trim()
            )
        )
        matchViewModel.resetLoadFlag()
        savedMsg = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(title = "Match Schedule Settings", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard {
                ToggleRow("Match API enabled", "Load matches from football-data.org.", apiEnabled) { apiEnabled = it }
                Spacer(Modifier.height(8.dp))
                ToggleRow("Always use demo data", "Skip the network and show sample matches.", useDemo) { useDemo = it }
            }

            SectionCard {
                Text("Date window", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Leave both empty to use the default 10-day window (today + 9 days).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrayText
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = dateFrom,
                    onValueChange = { dateFrom = it },
                    label = { Text("dateFrom (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = dateTo,
                    onValueChange = { dateTo = it },
                    label = { Text("dateTo (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        dateFrom = ""; dateTo = ""; error = null; savedMsg = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reset to default 10-day window") }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Default: ${DateUtils.today()} → ${DateUtils.defaultDateTo()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryGrayText
                )
            }

            SectionCard {
                Text("Competition filter", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Optional competition code (e.g. PL, PD). Leave empty for all.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrayText
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = competition,
                    onValueChange = { competition = it },
                    label = { Text("Competition code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            error?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            if (savedMsg) {
                Text("Settings saved.", style = MaterialTheme.typography.bodyMedium, color = GoalGreen)
            }

            Button(
                onClick = { persist() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrightOrange, contentColor = WhiteText)
            ) { Text("Save settings", style = MaterialTheme.typography.titleMedium) }

            OutlinedButton(
                onClick = {
                    appViewModel.clearMatchCache()
                    matchViewModel.resetLoadFlag()
                    savedMsg = false
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Clear match cache") }

            SectionCard {
                Text(
                    "Match data is provided by football-data.org. Availability, accuracy, competitions, and update frequency depend on the API provider and the current API plan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrayText
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = SecondaryGrayText)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
