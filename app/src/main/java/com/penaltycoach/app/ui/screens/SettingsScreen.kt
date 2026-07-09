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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.penaltycoach.app.BuildConfig
import com.penaltycoach.app.ui.AppViewModel
import com.penaltycoach.app.ui.MatchViewModel
import com.penaltycoach.app.ui.components.OrangeHeader
import com.penaltycoach.app.ui.components.Pill
import com.penaltycoach.app.ui.components.SectionCard
import com.penaltycoach.app.ui.theme.GoalGreen
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.ui.theme.WarningYellow
import com.penaltycoach.app.ui.theme.WhiteText
import com.penaltycoach.app.ui.theme.DarkGraphite

@Composable
fun SettingsScreen(
    appViewModel: AppViewModel,
    matchViewModel: MatchViewModel,
    onBack: () -> Unit,
    onOpenMatchSettings: () -> Unit,
    onShowOnboarding: () -> Unit
) {
    val data by appViewModel.state.collectAsStateWithLifecycle()
    var confirm by remember { mutableStateOf<ConfirmAction?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        OrangeHeader(title = "Settings", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Penalty preferences
            SectionCard {
                Text("Penalty tracking", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Compact mode", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Slightly denser cards and spacing.", style = MaterialTheme.typography.bodyMedium, color = SecondaryGrayText)
                    }
                    Switch(
                        checked = data.settings.compactMode,
                        onCheckedChange = { appViewModel.setCompactMode(it) }
                    )
                }
                Spacer(Modifier.height(8.dp))
                SettingRow("Show onboarding again", onClick = onShowOnboarding)
            }

            // Match schedule
            SectionCard {
                Text("Match Schedule", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("API status", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    if (matchViewModel.hasToken) Pill("Token configured", GoalGreen)
                    else Pill("Demo mode", WarningYellow, textColor = DarkGraphite)
                }
                Spacer(Modifier.height(8.dp))
                SettingRow("Match Schedule settings", onClick = onOpenMatchSettings)
                SettingRow("Clear match cache", onClick = {
                    appViewModel.clearMatchCache(); matchViewModel.resetLoadFlag()
                })
            }

            // Data management
            SectionCard {
                Text("Data", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                SettingRow("Delete all penalty shots", danger = true) { confirm = ConfirmAction.DeleteShots }
                SettingRow("Delete all penalty series", danger = true) { confirm = ConfirmAction.DeleteSeries }
                SettingRow("Reset all local data", danger = true) { confirm = ConfirmAction.ResetAll }
            }

            // App info + disclaimers
            SectionCard {
                Text("App information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text("PenaltyCoach v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text("A manual football penalty training log.", style = MaterialTheme.typography.bodyMedium, color = SecondaryGrayText)
            }

            SectionCard {
                Text("Penalty tracking disclaimer", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text(
                    "PenaltyCoach is a manual football penalty training log. Penalty shots, results, zones, and notes are added by the user. The app is not an official football tool and does not provide professional coaching or medical advice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrayText
                )
            }

            SectionCard {
                Text("Match schedule API disclaimer", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Match data is provided by football-data.org. Availability, accuracy, competitions, and update frequency depend on the API provider and the current API plan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrayText
                )
            }

            SectionCard {
                Text("Privacy", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text(
                    "PenaltyCoach stores penalty shots, penalty series, notes, settings, and cached match data on this device. The app uses internet only to load football match data from football-data.org. No account, no ads, no analytics, no payments, no Firebase, no location, no notifications, no sensors, no Google Fit, and no Health Connect.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrayText
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    confirm?.let { action ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(action.title) },
            text = { Text(action.message) },
            confirmButton = {
                Button(
                    onClick = {
                        when (action) {
                            ConfirmAction.DeleteShots -> appViewModel.deleteAllShots()
                            ConfirmAction.DeleteSeries -> appViewModel.deleteAllSeries()
                            ConfirmAction.ResetAll -> {
                                appViewModel.resetAllData()
                                matchViewModel.resetLoadFlag()
                            }
                        }
                        confirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = WhiteText)
                ) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Cancel") } }
        )
    }
}

private enum class ConfirmAction(val title: String, val message: String) {
    DeleteShots("Delete all penalty shots?", "This permanently removes every recorded penalty shot. Series summaries will have no shots."),
    DeleteSeries("Delete all penalty series?", "This permanently removes every penalty series. Individual shots are kept."),
    ResetAll("Reset all local data?", "This permanently removes all shots, series, settings, and cached match data.")
}

@Composable
private fun SettingRow(
    label: String,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (danger) FontWeight.Bold else FontWeight.Normal
        )
    }
}
