package com.jp.privacyscanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jp.privacyscanner.data.model.RiskLevel
import com.jp.privacyscanner.ui.theme.colorFor

/** Medidor circular do score, com a cor a refletir o nível de risco. */
@Composable
fun ScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Int = 120
) {
    val level = RiskLevel.fromScore(score)
    val color = colorFor(level)
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size.dp)) {
        CircularProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.size(size.dp),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeWidth = (size / 12).dp
        )
        Text(
            text = "$score",
            fontSize = (size / 3).sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/** Chip compacto que rotula um nível de risco. */
@Composable
fun RiskChip(level: RiskLevel, modifier: Modifier = Modifier) {
    val color = colorFor(level)
    val label = when (level) {
        RiskLevel.LOW -> "Baixo"
        RiskLevel.MEDIUM -> "Médio"
        RiskLevel.HIGH -> "Alto"
        RiskLevel.CRITICAL -> "Crítico"
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium.copy(color = Color.Unspecified)
        )
    }
}
