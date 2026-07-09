package com.penaltycoach.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.penaltycoach.app.data.model.ShotResult
import com.penaltycoach.app.ui.theme.CardDarkText
import com.penaltycoach.app.ui.theme.WhiteText

/** Big three-way result selector (Goal / Miss / Saved) with fixed result colors. */
@Composable
fun ResultSelector(
    selected: ShotResult?,
    onSelect: (ShotResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ShotResult.entries.forEach { result ->
            val color = resultColor(result)
            val isSelected = selected == result
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) color else color.copy(alpha = 0.12f))
                    .clickable { onSelect(result) }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = result.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) WhiteText else color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Generic single-choice chip row used for foot and power selectors. */
@Composable
fun <T> OptionChips(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) activeColor else Color(0xFFF0F0F0))
                    .clickable { onSelect(option) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) WhiteText else CardDarkText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Outlined-style row selector variant (used where an unselected look fits better). */
@Composable
fun <T> OptionOutlinedChips(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val border = BorderStroke(1.5.dp, if (isSelected) activeColor else Color(0xFFDDDDDD))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) activeColor.copy(alpha = 0.14f) else Color.White)
                    .border(border, RoundedCornerShape(10.dp))
                    .clickable { onSelect(option) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) activeColor else CardDarkText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
