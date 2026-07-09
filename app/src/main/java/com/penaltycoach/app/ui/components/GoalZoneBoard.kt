package com.penaltycoach.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.penaltycoach.app.data.model.ShotZone
import com.penaltycoach.app.ui.theme.BrightOrange
import com.penaltycoach.app.ui.theme.CardDarkText
import com.penaltycoach.app.ui.theme.GoalFrameDark
import com.penaltycoach.app.ui.theme.PitchLineWhite
import com.penaltycoach.app.ui.theme.WhiteText

/**
 * The signature "goal zone board": a football goal seen from the front, split
 * into a 3x3 grid of tappable zones. This is the main visual identity of the
 * app. Deliberately built from simple Compose shapes + borders — no images, no
 * complex animation.
 *
 * @param stats per-zone (total, goals, conversion) used for the heat highlight.
 * @param selectedZone currently selected zone (drawn with a bright orange ring).
 * @param showStats when true, each cell shows shots + conversion.
 * @param compact smaller preview used on the Home screen.
 */
@Composable
fun GoalZoneBoard(
    stats: Map<ShotZone, ZoneCellStat>,
    selectedZone: ShotZone?,
    onZoneClick: (ShotZone) -> Unit,
    modifier: Modifier = Modifier,
    showStats: Boolean = true,
    compact: Boolean = false
) {
    // Dark goal frame (the posts + crossbar).
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GoalFrameDark)
            .padding(if (compact) 6.dp else 10.dp)
    ) {
        // White net base — the 2dp gaps between cells read as net lines.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (compact) 1.6f else 1.35f)
                .clip(RoundedCornerShape(6.dp))
                .background(PitchLineWhite)
                .padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ShotZone.rows.forEach { rowZones ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    rowZones.forEach { zone ->
                        ZoneCell(
                            zone = zone,
                            stat = stats[zone] ?: ZoneCellStat(),
                            selected = zone == selectedZone,
                            showStats = showStats && !compact,
                            compact = compact,
                            onClick = { onZoneClick(zone) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/** Lightweight per-cell stat holder (keeps the board independent of Stats util). */
data class ZoneCellStat(
    val total: Int = 0,
    val goals: Int = 0,
    val conversion: Int = 0
)

@Composable
private fun ZoneCell(
    zone: ShotZone,
    stat: ZoneCellStat,
    selected: Boolean,
    showStats: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when {
        selected -> BrightOrange
        stat.total <= 0 -> Color(0xFFFDFDFD)
        else -> {
            // "Heat": hotter (more orange) as conversion rises.
            val alpha = 0.14f + 0.62f * (stat.conversion.coerceIn(0, 100) / 100f)
            BrightOrange.copy(alpha = alpha)
        }
    }
    val textColor = if (selected) WhiteText else CardDarkText

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .then(
                if (selected) Modifier.border(2.dp, GoalFrameDark, RoundedCornerShape(4.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (compact) zone.shortLabel else zone.shortLabel,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (showStats && stat.total > 0) {
                Text(
                    text = "${stat.goals}/${stat.total}",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${stat.conversion}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
