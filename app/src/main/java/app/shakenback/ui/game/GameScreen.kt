package app.shakenback.ui.game

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import app.shakenback.ButtonFeedback
import app.shakenback.GameUiState
import app.shakenback.core.Modality
import app.shakenback.core.Stimuli
import app.shakenback.ui.theme.LocalFeedbackColors

@Composable
fun GameScreen(
    state: GameUiState,
    haptics: Boolean,
    shakeHint: Boolean,
    onRespond: (Modality) -> Unit,
    onQuit: () -> Unit,
) {
    val view = LocalView.current
    val focusRequester = remember { FocusRequester() }

    // Keep the display awake mid-session, and abandon the session if the app is
    // backgrounded: a session with a gap in it isn't a fair measurement.
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { onQuit() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val respond: (Modality) -> Unit = { modality ->
        if (state.acceptingResponses) {
            if (haptics) view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onRespond(modality)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .focusRequester(focusRequester)
            .focusable()
            // Hardware keyboard support, using Brain Workshop's keys.
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.A -> { respond(Modality.POSITION); true }
                    Key.L -> { respond(Modality.AUDIO); true }
                    else -> false
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GameHeader(state = state, shakeHint = shakeHint, onQuit = onQuit)
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            StimulusGrid(activeCell = state.activeCell, modifier = Modifier.fillMaxSize())
            Countdown(value = state.countdown)
        }
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ResponseButton(
                label = "Position",
                keyHint = "A",
                feedback = state.feedbackFor(Modality.POSITION),
                enabled = state.acceptingResponses,
                onClick = { respond(Modality.POSITION) },
                modifier = Modifier.weight(1f),
            )
            ResponseButton(
                label = "Sound",
                keyHint = "L",
                feedback = state.feedbackFor(Modality.AUDIO),
                enabled = state.acceptingResponses,
                onClick = { respond(Modality.AUDIO) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun GameHeader(state: GameUiState, shakeHint: Boolean, onQuit: () -> Unit) {
    val progress by animateFloatAsState(
        targetValue = if (state.trialCount == 0 || state.countdown != null) 0f
        else (state.trial + 1f) / state.trialCount,
        animationSpec = tween(400),
        label = "progress",
    )
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onQuit) {
                Icon(Icons.Default.Close, contentDescription = "Quit session")
            }
            Text(
                text = "${state.n}-back",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            if (state.countdown == null) {
                Text(
                    text = "${state.trial + 1} / ${state.trialCount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .height(6.dp),
        )
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when {
                    state.countdown != null -> "Get ready"
                    state.inWarmUp -> "Memorise the first ${state.n}"
                    else -> "Same as ${state.n} back?"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (shakeHint) {
                Text(
                    text = "Shake to restart",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun StimulusGrid(activeCell: Int?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(3) { row ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                repeat(3) { column ->
                    val index = row * 3 + column
                    val cellModifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                    if (index == Stimuli.CENTER_CELL) {
                        FixationPoint(cellModifier)
                    } else {
                        GridCell(active = index == activeCell, modifier = cellModifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun GridCell(active: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val color by animateColorAsState(
        targetValue = if (active) colors.primary else colors.surfaceContainerHigh,
        animationSpec = tween(if (active) 90 else 260),
        label = "cellColor",
    )
    val scale by animateFloatAsState(
        targetValue = if (active) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "cellScale",
    )
    val glow by animateDpAsState(
        targetValue = if (active) 18.dp else 0.dp,
        animationSpec = tween(if (active) 90 else 260),
        label = "cellGlow",
    )
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(glow, shape, ambientColor = colors.primary, spotColor = colors.primary)
            .clip(shape)
            .background(color),
    )
}

@Composable
private fun FixationPoint(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        )
    }
}

@Composable
private fun Countdown(value: Int?) {
    AnimatedContent(
        targetState = value,
        transitionSpec = { (scaleIn(initialScale = 1.6f) + fadeIn()) togetherWith fadeOut() },
        label = "countdown",
    ) { count ->
        if (count != null) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun ResponseButton(
    label: String,
    keyHint: String,
    feedback: ButtonFeedback,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val feedbackColors = LocalFeedbackColors.current
    val (targetContainer, targetContent) = when (feedback) {
        ButtonFeedback.IDLE -> colors.surfaceContainerHigh to colors.onSurface
        ButtonFeedback.PRESSED -> colors.primaryContainer to colors.onPrimaryContainer
        ButtonFeedback.CORRECT -> feedbackColors.correct to feedbackColors.onCorrect
        ButtonFeedback.WRONG -> feedbackColors.wrong to feedbackColors.onWrong
    }
    val container by animateColorAsState(targetContainer, tween(150), label = "buttonContainer")
    val content by animateColorAsState(targetContent, tween(150), label = "buttonContent")
    val pressedScale by animateFloatAsState(
        targetValue = if (feedback == ButtonFeedback.IDLE) 1f else 0.96f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "buttonScale",
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(28.dp),
        color = container,
        contentColor = content,
        modifier = modifier
            .height(108.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .graphicsLayer {
                scaleX = pressedScale
                scaleY = pressedScale
            },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(label, style = MaterialTheme.typography.titleLarge)
            Text(
                text = keyHint,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = content.copy(alpha = 0.55f),
            )
        }
    }
}
