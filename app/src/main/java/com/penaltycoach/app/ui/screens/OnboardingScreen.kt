package com.penaltycoach.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.DeepOrange
import com.penaltycoach.app.ui.theme.GoalFrameDark
import com.penaltycoach.app.ui.theme.PitchLineWhite
import com.penaltycoach.app.ui.theme.SecondaryGrayText
import com.penaltycoach.app.ui.theme.WhiteText

/**
 * First-launch welcome screen. Introduces the core penalty-training purpose and
 * clearly states the disclaimers (manual log, not official, secondary matches).
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepOrange)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            MiniGoalMark()
            Spacer(Modifier.height(20.dp))
            Text(
                text = "PenaltyCoach",
                style = MaterialTheme.typography.displaySmall,
                color = WhiteText
            )
            Text(
                text = "Penalty training tracker",
                style = MaterialTheme.typography.titleMedium,
                color = WhiteText.copy(alpha = 0.9f)
            )

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(WhiteText)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Feature("Track your penalty practice", "Choose a zone, record the result, and review your stats.")
                Feature("Goal zone board", "A 3x3 goal grid is the heart of the app — tap a zone to log a shot.")
                Feature("Series training", "Practice 5-shot or 10-shot series and see a summary at the end.")
                Feature("Statistics & history", "Watch your conversion and zone performance improve over time.")
                Feature("Match Schedule (extra)", "View football matches as an optional reference. It is a secondary feature.")
                Feature("Private by design", "No account. No ads. No betting. No official logos. Your data stays on this device.")
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "PenaltyCoach is a manual football penalty training log. Penalty shots, results, zones, and notes are added by you. The app is not an official football tool and does not provide professional coaching or medical advice.",
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteText.copy(alpha = 0.92f)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoalFrameDark,
                    contentColor = WhiteText
                )
            ) {
                Text("Start Training", style = MaterialTheme.typography.titleMedium)
            }
            TextButton(onClick = onFinish) {
                Text("Skip", color = WhiteText)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Feature(title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp, end = 12.dp)
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(BrightOrange)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryGrayText
            )
        }
    }
}

/** Small stylized goal mark (frame + net + penalty dot) built from shapes. */
@Composable
private fun MiniGoalMark() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(GoalFrameDark)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .background(PitchLineWhite)
                .padding(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    repeat(3) { c ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (it == 1 && c == 2) BrightOrange else BrightOrange.copy(alpha = 0.18f))
                        )
                    }
                }
            }
        }
    }
}
