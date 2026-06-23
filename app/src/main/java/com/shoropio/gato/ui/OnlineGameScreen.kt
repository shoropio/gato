package com.shoropio.gato.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoropio.gato.data.FirebaseManager
import com.shoropio.gato.data.GameMatch
import com.shoropio.gato.ui.theme.NeonAmber
import com.shoropio.gato.ui.theme.NeonCyan
import com.shoropio.gato.ui.theme.NeonEmerald
import com.shoropio.gato.ui.theme.NeonMagenta
import kotlinx.coroutines.launch

@Composable
fun OnlineGameScreen(
    matchId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val matchData by FirebaseManager.observeMatch(matchId).collectAsState(initial = null)
    val currentUid = FirebaseManager.getCurrentUid() ?: ""
    var mySymbol by remember { mutableStateOf("") }
    var opponentName by remember { mutableStateOf("") }
    var gameOver by remember { mutableStateOf(false) }

    LaunchedEffect(matchData) {
        matchData?.let { match ->
            mySymbol = when {
                match.playerX == currentUid -> "X"
                match.playerO == currentUid -> "O"
                else -> ""
            }
            opponentName = when {
                mySymbol == "X" -> match.playerOName
                mySymbol == "O" -> match.playerXName
                else -> ""
            }
            if (match.status == "finished") {
                gameOver = true
            }
        }
    }

    if (matchData == null) {
        CyberGridBackground(modifier = modifier) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NeonText(
                    text = "CARGANDO PARTIDA...",
                    color = NeonCyan,
                    fontSize = 16.sp
                )
            }
        }
        return
    }

    val match = matchData!!
    val board = match.board
    val currentPlayer = match.currentPlayer
    val isMyTurn = currentPlayer == mySymbol
    val isWaiting = match.status == "waiting"

    val primaryStyleColor = if (mySymbol == "X") NeonCyan else NeonMagenta
    val secondaryStyleColor = if (mySymbol == "X") NeonMagenta else NeonCyan

    CyberGridBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberButton(
                        text = "SALIR",
                        onClick = {
                            scope.launch {
                                if (!gameOver && match.id.isNotEmpty()) {
                                    FirebaseManager.removeMatch(match.id)
                                }
                            }
                            onNavigateBack()
                        },
                        color = Color.LightGray,
                        modifier = Modifier.width(80.dp).height(38.dp),
                        fontSize = 11.sp
                    )

                    NeonText(
                        text = "ONLINE PvP",
                        color = primaryStyleColor.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )

                    if (gameOver) {
                        CyberButton(
                            text = "SALIR",
                            onClick = {
                                scope.launch {
                                    if (match.id.isNotEmpty()) FirebaseManager.removeMatch(match.id)
                                }
                                onNavigateBack()
                            },
                            color = NeonAmber,
                            modifier = Modifier.width(80.dp).height(38.dp),
                            fontSize = 11.sp
                        )
                    } else {
                        Spacer(modifier = Modifier.width(80.dp).height(38.dp))
                    }
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = primaryStyleColor.copy(alpha = 0.25f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NeonText(
                                text = if (mySymbol == "X") "TÚ ($mySymbol)" else opponentName.ifEmpty { "RIVAL" },
                                color = primaryStyleColor,
                                fontSize = 11.sp
                            )
                            NeonText(text = match.playerXName, color = Color.White, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NeonText(text = "VS", color = Color.LightGray, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NeonText(
                                text = if (mySymbol == "O") "TÚ ($mySymbol)" else opponentName.ifEmpty { "RIVAL" },
                                color = secondaryStyleColor,
                                fontSize = 11.sp
                            )
                            NeonText(text = match.playerOName, color = Color.White, fontSize = 10.sp)
                        }
                    }
                }

                // Turn indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(36.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    if (isMyTurn) NeonEmerald.copy(alpha = 0.15f) else Color(0x1AFFFFFF),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val statusText = when {
                        gameOver -> {
                            when (match.winner) {
                                mySymbol -> "¡VICTORIA!"
                                "draw" -> "EMPATE"
                                else -> "DERROTA"
                            }
                        }
                        isWaiting -> "ESPERANDO CONEXIÓN..."
                        isMyTurn -> "TU TURNO ($mySymbol)"
                        else -> "ESPERANDO JUGADA DE $opponentName..."
                    }
                    val statusColor = when {
                        gameOver && match.winner == mySymbol -> NeonEmerald
                        gameOver && match.winner == "draw" -> Color.LightGray
                        gameOver -> NeonMagenta
                        isMyTurn -> NeonEmerald
                        else -> Color.Gray
                    }
                    NeonText(text = statusText, color = statusColor, fontSize = 12.sp, glowColor = statusColor)
                }
            }

            // Game Board
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val boardW = constraints.maxWidth.toFloat()
                val boardH = constraints.maxHeight.toFloat()
                val cellW = boardW / 3f
                val cellH = boardH / 3f

                GlassCard(
                    modifier = Modifier.fillMaxSize(),
                    borderColor = if (isMyTurn) primaryStyleColor.copy(alpha = 0.3f) else Color(0x33FFFFFF),
                    glowColor = if (isMyTurn) primaryStyleColor.copy(alpha = 0.08f) else Color.Transparent
                ) {
                    // Grid lines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val s = 4.dp.toPx()
                        val w = size.width; val h = size.height
                        val gridColor = primaryStyleColor.copy(alpha = 0.25f)
                        drawLine(gridColor, Offset(w * 0.33f, 0f), Offset(w * 0.33f, h), s)
                        drawLine(gridColor, Offset(w * 0.66f, 0f), Offset(w * 0.66f, h), s)
                        drawLine(gridColor, Offset(0f, h * 0.33f), Offset(w, h * 0.33f), s)
                        drawLine(gridColor, Offset(0f, h * 0.66f), Offset(w, h * 0.66f), s)
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        for (row in 0..2) {
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                for (col in 0..2) {
                                    val index = row * 3 + col
                                    val symbol = board[index]

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .clickable(enabled = isMyTurn && symbol.isEmpty() && !gameOver) {
                                                scope.launch {
                                                    FirebaseManager.makeMove(match.id, index, mySymbol)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (symbol.isNotEmpty()) {
                                            Canvas(modifier = Modifier.size(48.dp)) {
                                                val cw = size.width; val ch = size.height
                                                val strokeThick = 5.dp.toPx()
                                                val symColor = if (symbol == "X") NeonCyan else NeonMagenta
                                                if (symbol == "X") {
                                                    drawLine(symColor, Offset(0f, 0f), Offset(cw, ch), strokeThick)
                                                    drawLine(symColor, Offset(cw, 0f), Offset(0f, ch), strokeThick)
                                                } else {
                                                    drawArc(symColor, -90f, 360f, false, style = Stroke(strokeThick))
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

            // Bottom status
            Box(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (gameOver) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(0.9f).height(52.dp),
                        borderColor = NeonAmber,
                        glowColor = NeonAmber.copy(0.05f),
                        contentPadding = 8.dp
                    ) {
                        val endText = when (match.winner) {
                            mySymbol -> "¡VICTORIA CÓSMICA!"
                            "draw" -> "EMPATE ELECTRÓNICO"
                            else -> "DERROTA TOTAL"
                        }
                        NeonText(
                            text = endText,
                            color = NeonAmber,
                            fontSize = 14.sp,
                            glowColor = NeonAmber,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    NeonText(
                        text = if (isMyTurn) "TOCA UNA CASILLA" else "ESPERANDO...",
                        color = if (isMyTurn) NeonEmerald else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
