package app.shakenback.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.shakenback.core.LevelRules
import app.shakenback.data.SessionRecord
import app.shakenback.data.Settings
import app.shakenback.ui.theme.RaceNavy
import app.shakenback.ui.theme.RaceOrange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

/** Single-screen layout: the progress card absorbs whatever height is left. */
@Composable
fun HomeScreen(
    settings: Settings,
    history: List<SessionRecord>,
    speechReady: Boolean?,
    shakeHint: Boolean,
    onLevelChange: (Int) -> Unit,
    onAutoProgressChange: (Boolean) -> Unit,
    onStart: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var showHowToPlay by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Shake-N-Back",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { showHowToPlay = true }) {
                    Icon(Icons.Default.Info, contentDescription = "How to play")
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            LevelHero(settings, onLevelChange, onAutoProgressChange)
            StatsRow(history)
            ProgressCard(
                history = history,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            if (speechReady == false) SpeechWarning()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    Text("🏁", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Start your engines",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
                if (shakeHint) {
                    Text(
                        text = "…or give your phone a good shake",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }

    if (showHowToPlay) {
        HowToPlayDialog(level = settings.level, onDismiss = { showHowToPlay = false })
    }
}

@Composable
private fun LevelHero(
    settings: Settings,
    onLevelChange: (Int) -> Unit,
    onAutoProgressChange: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(colors.primary, RaceOrange)))
            .drawBehind { drawCheckeredFlag(Color.White.copy(alpha = 0.12f)) }
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Column {
            Text(
                text = "Dual N-Back · position + sound",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LevelStepButton("−", "Lower level", enabled = settings.level > LevelRules.MIN_LEVEL) {
                    onLevelChange(settings.level - 1)
                }
                RaceNumber(settings.level, Modifier.padding(horizontal = 10.dp))
                LevelStepButton("+", "Raise level", enabled = settings.level < LevelRules.MAX_LEVEL) {
                    onLevelChange(settings.level + 1)
                }
                Text(
                    text = "-back",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Level up automatically",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                    )
                    Text(
                        text = when {
                            !settings.autoProgress -> "Cruising at ${settings.level}-back"
                            settings.strikes > 0 ->
                                "${settings.strikes} of ${LevelRules.STRIKES_TO_FALLBACK} low scores at this level"
                            else -> "Up at ${pct(LevelRules.ADVANCE_THRESHOLD)}, down after " +
                                "${LevelRules.STRIKES_TO_FALLBACK} under ${pct(LevelRules.FALLBACK_THRESHOLD)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                Switch(
                    checked = settings.autoProgress,
                    onCheckedChange = onAutoProgressChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.primary,
                        checkedTrackColor = Color.White,
                        checkedBorderColor = Color.White,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
                        uncheckedBorderColor = Color.White.copy(alpha = 0.6f),
                    ),
                )
            }
        }
    }
}

/** The level in a white roundel, like the number on a stock car door. */
@Composable
private fun RaceNumber(level: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = level,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInVertically { it * direction } + fadeIn()) togetherWith
                    (slideOutVertically { -it * direction } + fadeOut())
            },
            label = "level",
        ) { value ->
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                ),
                color = RaceNavy,
            )
        }
    }
}

@Composable
private fun LevelStepButton(label: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = Color.White.copy(alpha = if (enabled) 0.25f else 0.08f),
        contentColor = Color.White.copy(alpha = if (enabled) 1f else 0.4f),
        modifier = Modifier
            .size(40.dp)
            .semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

/** A checkered flag fading in from the card's right edge. */
private fun DrawScope.drawCheckeredFlag(color: Color) {
    val cell = 16.dp.toPx()
    val columns = 6
    val rows = (size.height / cell).toInt() + 1
    val left = size.width - columns * cell
    for (row in 0 until rows) {
        for (column in 0 until columns) {
            if ((row + column) % 2 == 0) {
                drawRect(
                    color = color.copy(alpha = color.alpha * (column + 1) / columns),
                    topLeft = Offset(left + column * cell, row * cell),
                    size = Size(cell, cell),
                )
            }
        }
    }
}

@Composable
private fun SpeechWarning() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "No text-to-speech voice, so letters will be silent. " +
                    "Install an English voice in system settings.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun StatsRow(history: List<SessionRecord>) {
    val today = remember(history) {
        val zone = ZoneId.systemDefault()
        val date = LocalDate.now(zone)
        history.count { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() == date }
    }
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(
            emoji = "🔥",
            label = "Today",
            value = today.toString(),
            container = colors.tertiaryContainer,
            content = colors.onTertiaryContainer,
            modifier = Modifier.weight(1f),
        )
        StatTile(
            emoji = "🏁",
            label = "Laps",
            value = history.size.toString(),
            container = colors.secondaryContainer,
            content = colors.onSecondaryContainer,
            modifier = Modifier.weight(1f),
        )
        StatTile(
            emoji = "🏆",
            label = "Peak",
            value = history.maxOfOrNull { it.n }?.let { "$it-back" } ?: "—",
            container = colors.primaryContainer,
            content = colors.onPrimaryContainer,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(
    emoji: String,
    label: String,
    value: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = content.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun ProgressCard(history: List<SessionRecord>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            val recent = history.takeLast(CHART_SESSIONS)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Your race line", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (recent.isNotEmpty()) {
                    Text(
                        text = "last ${pct(recent.last().overall)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (recent.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "🏎️\nYour first lap awaits",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
                LevelChart(
                    records = recent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun LevelChart(records: List<SessionRecord>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val line = colors.secondary
    val dot = colors.tertiary
    val gridColor = colors.outlineVariant
    val maxLevel = maxOf(records.maxOf { it.n } + 1, 3)
    val minLevel = maxOf(records.minOf { it.n } - 1, LevelRules.MIN_LEVEL - 1)

    Canvas(modifier) {
        inset(8.dp.toPx()) {
            val range = (maxLevel - minLevel).toFloat()
            fun y(level: Int) = size.height - (level - minLevel) / range * size.height
            val stepX = if (records.size > 1) size.width / (records.size - 1) else 0f
            val points = records.mapIndexed { i, record -> Offset(i * stepX, y(record.n)) }

            for (level in minLevel..maxLevel) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y(level)),
                    end = Offset(size.width, y(level)),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val path = Path().apply {
                points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
            }
            val area = Path().apply {
                addPath(path)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            drawPath(area, Brush.verticalGradient(listOf(line.copy(alpha = 0.3f), Color.Transparent)))
            drawPath(
                path = path,
                color = line,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            points.forEach {
                drawCircle(line, radius = 5.dp.toPx(), center = it)
                drawCircle(dot, radius = 3.dp.toPx(), center = it)
            }
        }
    }
}

@Composable
private fun HowToPlayDialog(level: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to play") },
        text = {
            Text(
                "Each turn a square lights up and a letter is spoken.\n\n" +
                    "Tap Position when the square is where it was $level " +
                    "${if (level == 1) "turn" else "turns"} ago, and Sound when the letter " +
                    "matches the one $level back. Both can match at once.\n\n" +
                    "Score ${pct(LevelRules.ADVANCE_THRESHOLD)} to level up. Shake to start or restart.",
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Let's go") } },
    )
}

private fun pct(value: Double) = "${(value * 100).roundToInt()}%"

private const val CHART_SESSIONS = 30
