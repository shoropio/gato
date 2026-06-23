package com.shoropio.gato.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoropio.gato.feedback.FeedbackSettings
import com.shoropio.gato.ui.theme.CyberObsidian
import com.shoropio.gato.ui.theme.NeonAmber
import com.shoropio.gato.ui.theme.NeonCyan
import com.shoropio.gato.ui.theme.NeonEmerald
import com.shoropio.gato.ui.theme.NeonMagenta
import com.shoropio.gato.ui.theme.NeonPurple
import com.shoropio.gato.viewmodel.GamePlayState
import com.shoropio.gato.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Debris details for fireworks/particles
data class CoreParticle(
    val cellIndex: Int,
    var offsetX: Float,
    var offsetY: Float,
    val vx: Float,
    val vy: Float,
    var alpha: Float,
    val size: Float,
    val color: Color
)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // 1. Gather States from VM
    val boardData by viewModel.board.collectAsState()
    val currentPlayer by viewModel.currentPlayer.collectAsState()
    val gameState by viewModel.gamePlayState.collectAsState()
    val mode by viewModel.gameMode.collectAsState()
    val difficulty by viewModel.aiDifficulty.collectAsState()
    val settingsState by viewModel.settings.collectAsState()

    val p1Score by viewModel.p1Score.collectAsState()
    val p2Score by viewModel.p2Score.collectAsState()
    val draws by viewModel.drawScore.collectAsState()

    // 2. Resolve Customized Theme Styles
    val boardStyle = settingsState?.boardStyle ?: "default"
    val (primaryStyleColor, secondaryStyleColor) = when (boardStyle) {
        "vaporwave" -> Pair(NeonMagenta, NeonPurple)
        "gold" -> Pair(NeonAmber, Color(0xFFE2E8F0))
        "emerald" -> Pair(NeonEmerald, NeonCyan)
        else -> Pair(NeonCyan, NeonMagenta) // default cyan/magenta Cyborg
    }

    // 3. Faux 3D rotation States
    val rotateX = remember { Animatable(0f) }
    val rotateY = remember { Animatable(0f) }

    // 4. Live Floating Particle Debris System
    val particles = remember { mutableStateListOf<CoreParticle>() }

    // Emit particles when game is won
    LaunchedEffect(Unit) {
        viewModel.particleTrigger.collect { winCombo ->
            if (FeedbackSettings.isVibrationEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            // Spawn 45 neon particles spread around winning cells
            for (i in 0..45) {
                val pColor = if (Random.nextBoolean()) primaryStyleColor else secondaryStyleColor
                val cellIdx = winCombo[i % winCombo.size]
                particles.add(
                    CoreParticle(
                        cellIndex = cellIdx,
                        offsetX = Random.nextFloat() * 160f - 80f,
                        offsetY = Random.nextFloat() * 160f - 80f,
                        vx = Random.nextFloat() * 14f - 7f,
                        vy = Random.nextFloat() * 14f - 7f,
                        alpha = 1f,
                        size = Random.nextFloat() * 10f + 6f,
                        color = pColor
                    )
                )
            }
        }
    }

    // Particle updater loop
    LaunchedEffect(particles.size) {
        if (particles.isNotEmpty()) {
            while (particles.any { it.alpha > 0.02f }) {
                delay(16)
                particles.forEach { p ->
                    p.offsetX += p.vx
                    p.offsetY += p.vy
                    p.alpha = maxOf(0f, p.alpha - 0.022f)
                }
            }
            particles.clear()
        }
    }

    CyberGridBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // HUD Top-Banner (Score & Turn Indicators)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Return to Main Menu Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberButton(
                        text = "VUELTA",
                        onClick = onNavigateBack,
                        color = Color.LightGray,
                        modifier = Modifier
                            .width(88.dp)
                            .height(42.dp),
                        testTag = "btn_back_to_menu"
                    )

                    val modeLabel = when (mode) {
                        "pvp" -> "MODO: JvJ LOCAL"
                        "demo" -> "DEMO: IA vs IA"
                        else -> "VS IA: ${difficulty.uppercase()}"
                    }
                    NeonText(
                        text = modeLabel,
                        color = primaryStyleColor.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )

                    CyberButton(
                        text = "REINICIAR",
                        onClick = {
                            viewModel.resetBoard()
                            if (FeedbackSettings.isVibrationEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        color = secondaryStyleColor,
                        modifier = Modifier
                            .width(102.dp)
                            .height(42.dp),
                        testTag = "btn_reset_board"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scoreboard card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = primaryStyleColor.copy(alpha = 0.25f),
                    glowColor = secondaryStyleColor.copy(alpha = 0.05f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NeonText(
                                text = if (mode == "pvp") "JUGADOR X" else "TÚ (X)",
                                color = primaryStyleColor,
                                fontSize = 11.sp
                            )
                            NeonText(
                                text = p1Score.toString(),
                                color = Color.White,
                                fontSize = 24.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NeonText(text = "EMPATES", color = Color.LightGray, fontSize = 11.sp)
                            NeonText(
                                text = draws.toString(),
                                color = Color.LightGray,
                                fontSize = 24.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NeonText(
                                text = if (mode == "pvp") "JUGADOR O" else if (mode == "demo") "IA O" else "CÓDIGO (O)",
                                color = secondaryStyleColor,
                                fontSize = 11.sp
                            )
                            NeonText(
                                text = p2Score.toString(),
                                color = Color.White,
                                fontSize = 24.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Turn Indicator Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(42.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    if (currentPlayer == "X") primaryStyleColor.copy(alpha = 0.15f) else secondaryStyleColor.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val labelText = when (gameState) {
                        is GamePlayState.Won -> "FIN DE CANAL"
                        is GamePlayState.Draw -> "EMPATADO"
                        is GamePlayState.DemoRunning -> "IA ANALIZANDO..."
                        else -> if (mode == "pvp") "TURNO: $currentPlayer" else "TURNO AUTOMÁTICO: $currentPlayer"
                    }
                    val labelColor = if (currentPlayer == "X") primaryStyleColor else secondaryStyleColor
                    NeonText(
                        text = labelText,
                        color = if (gameState is GamePlayState.Active) labelColor else Color.White,
                        fontSize = 12.sp,
                        glowColor = labelColor,
                        maxLines = 2
                    )
                }
            }

            // 3D Fake Tilting Interactive Game Grid Board
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        rotationX = rotateX.value
                        rotationY = rotateY.value
                        cameraDistance = 14f * density
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val boardW = constraints.maxWidth.toFloat()
                val boardH = constraints.maxHeight.toFloat()
                val cellW = boardW / 3f
                val cellH = boardH / 3f

                // Background of the grid card
                GlassCard(
                    modifier = Modifier.fillMaxSize(),
                    borderColor = primaryStyleColor.copy(alpha = 0.3f),
                    glowColor = primaryStyleColor.copy(alpha = 0.08f)
                ) {
                    // Draw lines of the 3x3 Tic-Tac-Toe layout
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 4.dp.toPx()
                        val w = size.width
                        val h = size.height

                        // Grid lines
                        drawLine(
                            color = primaryStyleColor.copy(alpha = 0.25f),
                            start = Offset(w * 0.33f, 16.dp.toPx()),
                            end = Offset(w * 0.33f, h - 16.dp.toPx()),
                            strokeWidth = strokeW
                        )
                        drawLine(
                            color = primaryStyleColor.copy(alpha = 0.25f),
                            start = Offset(w * 0.66f, 16.dp.toPx()),
                            end = Offset(w * 0.66f, h - 16.dp.toPx()),
                            strokeWidth = strokeW
                        )
                        drawLine(
                            color = primaryStyleColor.copy(alpha = 0.25f),
                            start = Offset(16.dp.toPx(), h * 0.33f),
                            end = Offset(w - 16.dp.toPx(), h * 0.33f),
                            strokeWidth = strokeW
                        )
                        drawLine(
                            color = primaryStyleColor.copy(alpha = 0.25f),
                            start = Offset(16.dp.toPx(), h * 0.66f),
                            end = Offset(w - 16.dp.toPx(), h * 0.66f),
                            strokeWidth = strokeW
                        )
                    }

                    // Render grid cells layered above board
                    Column(modifier = Modifier.fillMaxSize()) {
                        for (row in 0..2) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                for (col in 0..2) {
                                    val index = row * 3 + col
                                    val symbol = boardData[index]

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .testTag("cell_$index")
                                            .clickable {
                                                if (gameState == GamePlayState.Active && symbol.isEmpty()) {
                                                    // Trigger tactile 3D rotational tilt offset
                                                    scope.launch {
                                                        if (FeedbackSettings.isVibrationEnabled) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        }
                                                        val tiltX = when (row) {
                                                            0 -> -11f
                                                            2 -> 11f
                                                            else -> 0f
                                                        }
                                                        val tiltY = when (col) {
                                                            0 -> -11f
                                                            2 -> 11f
                                                            else -> 0f
                                                        }

                                                        rotateX.animateTo(tiltX, tween(70, easing = LinearEasing))
                                                        rotateY.animateTo(tiltY, tween(70, easing = LinearEasing))
                                                        
                                                        // Bounce back to stable state
                                                        rotateX.animateTo(0f, spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioHighBouncy))
                                                        rotateY.animateTo(0f, spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioHighBouncy))
                                                    }
                                                    viewModel.onCellClicked(index)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Highlight winning cells
                                        var isWinningCell = false
                                        if (gameState is GamePlayState.Won) {
                                            if ((gameState as GamePlayState.Won).winningLine.contains(index)) {
                                                isWinningCell = true
                                            }
                                        }

                                        val glowCellBg = if (isWinningCell) {
                                            Brush.radialGradient(
                                                listOf(
                                                    if (symbol == "X") primaryStyleColor.copy(alpha = 0.16f) else secondaryStyleColor.copy(alpha = 0.16f),
                                                    Color.Transparent
                                                )
                                            )
                                        } else {
                                            null
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(0.9f)
                                                .then(if (glowCellBg != null) Modifier.background(glowCellBg) else Modifier),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (symbol.isNotEmpty()) {
                                                // Vectored Progressive Drawing
                                                val drawProgress = remember { Animatable(0f) }
                                                LaunchedEffect(symbol) {
                                                    drawProgress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
                                                }

                                                Canvas(modifier = Modifier.size(52.dp)) {
                                                    val cellW = size.width
                                                    val cellH = size.height
                                                    val strokeThick = 5.dp.toPx()

                                                    if (symbol == "X") {
                                                        // Draw glowing 'X' lines sequentially
                                                        val p = drawProgress.value
                                                        if (p > 0.01f) {
                                                            drawLine(
                                                                color = primaryStyleColor,
                                                                start = Offset(0f, 0f),
                                                                end = Offset(cellW * p, cellH * p),
                                                                strokeWidth = strokeThick
                                                            )
                                                        }
                                                        if (p > 0.5f) {
                                                            val p2 = (p - 0.5f) * 2f
                                                            drawLine(
                                                                color = primaryStyleColor,
                                                                start = Offset(cellW, 0f),
                                                                end = Offset(cellW - (cellW * p2), cellH * p2),
                                                                strokeWidth = strokeThick
                                                            )
                                                        }
                                                    } else {
                                                        // Draw glowing circle 'O' with stroke path
                                                        drawArc(
                                                            color = secondaryStyleColor,
                                                            startAngle = -90f,
                                                            sweepAngle = 360f * drawProgress.value,
                                                            useCenter = false,
                                                            style = Stroke(width = strokeThick)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Shimmering particle overlays
                Canvas(modifier = Modifier.fillMaxSize()) {
                    particles.forEach { p ->
                        val row = p.cellIndex / 3
                        val col = p.cellIndex % 3
                        val cx = col * cellW + cellW / 2f + p.offsetX
                        val cy = row * cellH + cellH / 2f + p.offsetY
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha),
                            radius = p.size,
                            center = Offset(cx, cy)
                        )
                    }
                }
            }

            // Results bottom sheet overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp),
                contentAlignment = Alignment.Center
            ) {
                this@Column.AnimatedVisibility(
                    visible = (gameState is GamePlayState.Won || gameState is GamePlayState.Draw),
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(250))
                ) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(76.dp),
                        borderColor = NeonAmber,
                        glowColor = NeonAmber.copy(0.05f),
                        contentPadding = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val winText = when (gameState) {
                                is GamePlayState.Won -> {
                                    val winSymbol = (gameState as GamePlayState.Won).winner
                                    if (mode == "pvp") "¡VICTORIA PARA JUGADOR $winSymbol!"
                                    else if (winSymbol == "X") "¡SISTEMA DERROTADO! (X GANA)"
                                    else "¡IA VENCIÓ AL USUARIO! (O GANA)"
                                }
                                else -> "EMPATE ELECTRÓNICO"
                            }
                            val bannerColor = if (gameState is GamePlayState.Draw) Color.LightGray else NeonAmber
                            
                            NeonText(
                                text = winText,
                                color = bannerColor,
                                fontSize = 10.sp,
                                glowColor = bannerColor,
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )

                            CyberButton(
                                text = "SIG.",
                                onClick = {
                                    viewModel.resetBoard()
                                    if (FeedbackSettings.isVibrationEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                },
                                color = NeonAmber,
                                modifier = Modifier
                                    .width(78.dp)
                                    .height(36.dp),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
