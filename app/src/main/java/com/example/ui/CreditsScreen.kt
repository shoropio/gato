package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple

@Composable
fun CreditsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val minimaxCodeSnippet = """
fun minimax(board: Array<String>, depth: Int, isMax: Boolean): Int {
    val score = evalBoard(board)
    if (score == 10) return score - depth
    if (score == -10) return score + depth
    if (!movesLeft(board)) return 0

    if (isMax) {
        var best = -1000
        for (i in 0..8) {
            if (board[i].isEmpty()) {
                board[i] = "O" // AI Symbol
                best = maxOf(best, minimax(board, d+1, false))
                board[i] = ""
            }
        }
        return best
    } else {
        var best = 1000
        for (i in 0..8) {
            if (board[i].isEmpty()) {
                board[i] = "X" // Human
                best = minOf(best, minimax(board, d+1, true))
                board[i] = ""
            }
        }
        return best
    }
}
    """.trimIndent()

    CyberGridBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Back button and Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberButton(
                    text = "< VOLVER",
                    onClick = onNavigateBack,
                    color = NeonCyan,
                    modifier = Modifier
                        .width(96.dp)
                        .height(38.dp),
                    testTag = "btn_credits_back"
                )
                Spacer(modifier = Modifier.weight(0.2f))
                NeonText(
                    text = "CRÉDITOS",
                    color = NeonCyan,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.width(32.dp))
            }

            // Architecture Highlights Card
            NeonText(
                text = "ARQUITECTURA DE SOFTWARE",
                color = NeonMagenta.copy(alpha = 0.9f),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeonText(text = "SISTEMA SEPARADO EN CAPAS", color = Color.White, fontSize = 14.sp)
                    Text(
                        text = "La aplicación adopta el estándar oficial de Google MVVM (Model-View-ViewModel) con Repositorios desacoplados:",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    BulletItem(title = "Room Database", desc = "SQLite reactivo para persistencia local de rachas, partidas y logros.")
                    BulletItem(title = "Procedural Audio", desc = "Generación de ondas de frecuencias (seno, rampa) en hilos de fondo mediante AudioTrack.")
                    BulletItem(title = "Coroutines Flow", desc = "Emisión asíncrona no bloqueante de eventos del tablero, IA, y estado de UI.")
                }
            }

            // Minimax Algorithmic Code Box
            NeonText(
                text = "CÓDIGO DE IA (MINIMAX)",
                color = NeonMagenta.copy(alpha = 0.9f),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonPurple,
                glowColor = NeonPurple.copy(alpha = 0.04f)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    NeonText(text = "SCORING RECURSIVO INTEGRADO", color = NeonAmber, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "El siguiente fragmento de código Kotlin implementa la IA Imposible libre de fallos matemáticos:",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Code terminal simulator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF030712))
                            .border(width = 1.dp, color = NeonPurple.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = minimaxCodeSnippet,
                            color = NeonCyan.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Creator Info
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonEmerald.copy(alpha = 0.3f),
                glowColor = NeonEmerald.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NeonText(text = "CREADO POR SHOROPIO", color = NeonEmerald, fontSize = 14.sp)
                    NeonText(text = "COM.SHOROPIO.GATO", color = Color.Gray, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gato Cyborg Matrix fue diseñado combinando la elegancia de Material 3 con experiencias dinámicas gaming.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun BulletItem(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NeonText(text = "⚡", color = NeonCyan, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        Column {
            NeonText(text = title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}
