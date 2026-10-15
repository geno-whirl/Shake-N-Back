package app.shakenback.ui.results

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.shakenback.core.LevelChange
import app.shakenback.core.LevelOutcome
import app.shakenback.core.LevelRules
import app.shakenback.core.ModalityScore
import app.shakenback.core.SessionResult
import kotlin.math.roundToInt

/** Single-screen layout: the score ring shrinks to whatever height is left. */
@Composable
fun ResultsScreen(
    result: SessionResult,
    outcome: LevelOutcome,
    haptics: Boolean,
    shakeHint: Boolean,
    onPlayAgain: () -> Unit,
    onPlayAtLevel: (Int) -> Unit,
    onHome: () -> Unit,
) {
    val celebrate = outcome.change == LevelChange.UP || result.overall >= 1.0
    val colors = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (outcome.change == LevelChange.UP) {
                FistBump(haptics = haptics)
            } else {
                Text(
                    text = headline(result, outcome),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ScoreRing(
                    score = result.overall,
                    modifier = Modifier
                        .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
                        .fillMaxHeight()
                        .aspectRatio(1f, matchHeightConstraintsFirst = true),
                )
            }
            Text(
                text = outcomeDetail(result, outcome),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModalityCard(
                    emoji = "🟦",
                    label = "Position",
                    score = result.position,
                    container = colors.secondaryContainer,
                    content = colors.onSecondaryContainer,
                    bar = colors.secondary,
                    modifier = Modifier.weight(1f),
                )
                ModalityCard(
                    emoji = "🔊",
                    label = "Sound",
                    score = result.audio,
                    container = colors.tertiaryContainer,
                    content = colors.onTertiaryContainer,
                    bar = colors.tertiary,
                    modifier = Modifier.weight(1f),
                )
            }
            Button(
                onClick = onPlayAgain,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("🏁  Play ${outcome.level}-back", style = MaterialTheme.typography.titleMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Levelling up is a suggestion: let the player stay where they were.
                if (outcome.change == LevelChange.UP) {
                    OutlinedButton(
                        onClick = { onPlayAtLevel(result.n) },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    ) {
                        Text("Stay at ${result.n}-back", maxLines = 1)
                    }
                }
                OutlinedButton(
                    onClick = onHome,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Text("Home")
                }
            }
            if (shakeHint) {
                Text(
                    text = "Shake to go again",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        if (celebrate) Confetti(Modifier.fillMaxSize())
    }
}

private fun headline(result: SessionResult, outcome: LevelOutcome): String = when {
    outcome.change == LevelChange.DOWN -> "Shake it off 💪"
    result.overall >= 1.0 -> "Flawless lap! 🏆"
    result.overall >= 0.65 -> "Nice driving! 🏁"
    result.overall >= LevelRules.FALLBACK_THRESHOLD -> "Keep it rolling 🛞"
    else -> "Pit stop. Go again! 🔧"
}

private fun outcomeDetail(result: SessionResult, outcome: LevelOutcome): String = when (outcome.change) {
    LevelChange.UP -> "You cleared ${result.n}-back. Next up: ${outcome.level}-back."
    LevelChange.DOWN -> "Three low scores at ${result.n}-back. Rebuild at ${outcome.level}-back."
    LevelChange.SAME -> if (outcome.strikes > 0) {
        "${outcome.strikes} of ${LevelRules.STRIKES_TO_FALLBACK} low scores at ${result.n}-back."
    } else {
        "Reach ${pct(LevelRules.ADVANCE_THRESHOLD)} to move up from ${result.n}-back."
    }
}

@Composable
private fun ScoreRing(score: Double, modifier: Modifier = Modifier) {
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(score) {
        sweep.animateTo(score.toFloat(), tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    val colors = MaterialTheme.colorScheme
    val track = colors.surfaceContainerHigh
    val gradient = listOf(colors.primary, colors.tertiary, colors.secondary, colors.primary)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            val topLeft = Offset(inset, inset)
            drawArc(track, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
            drawArc(
                brush = Brush.sweepGradient(gradient),
                startAngle = -90f,
                sweepAngle = 360f * sweep.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(pct(sweep.value.toDouble()), style = MaterialTheme.typography.displaySmall)
            Text(
                text = "overall",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ModalityCard(
    emoji: String,
    label: String,
    score: ModalityScore,
    container: Color,
    content: Color,
    bar: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$emoji $label", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(pct(score.accuracy), style = MaterialTheme.typography.titleMedium)
            }
            LinearProgressIndicator(
                progress = { score.accuracy.toFloat() },
                color = bar,
                trackColor = content.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
            )
            Row {
                MiniStat("hit", score.hits, content, Modifier.weight(1f))
                MiniStat("missed", score.misses, content, Modifier.weight(1f))
                MiniStat("extra", score.falseAlarms, content, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: Int, content: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value.toString(), style = MaterialTheme.typography.titleSmall)
        Text(label, style = MaterialTheme.typography.labelSmall, color = content.copy(alpha = 0.75f), maxLines = 1)
    }
}

private fun pct(value: Double) = "${(value * 100).roundToInt()}%"
