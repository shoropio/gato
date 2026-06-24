package com.shoropio.gato.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoropio.gato.data.GameStatsEntity
import com.shoropio.gato.ui.theme.CyberObsidian
import com.shoropio.gato.ui.theme.NeonAmber
import com.shoropio.gato.ui.theme.NeonCyan
import com.shoropio.gato.ui.theme.NeonEmerald
import com.shoropio.gato.ui.theme.NeonMagenta
import com.shoropio.gato.ui.theme.NeonPurple
import com.shoropio.gato.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onNavigateToGame: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToCredits: () -> Unit,
    onNavigateToOnline: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val settingsState by viewModel.settings.collectAsState()
    val statsList by viewModel.allStats.collectAsState()
    
    var showDifficultyDialog by remember { mutableStateOf(false) }

    // Resolve details
    val avatarName = settingsState?.selectedAvatar ?: "avatar_cyber_cat"
    val avatarLabel = avatarName.replace("avatar_", "").replace("_", " ").uppercase()
    val activeStreak = statsList.maxOfOrNull { it.currentStreak } ?: 0

    val scrollState = rememberScrollState()

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

            // 1. Futuristic Branding Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                NeonText(
                    text = "GATO",
                    color = NeonCyan,
                    fontSize = 52.sp,
                    glowColor = NeonCyan
                )
                NeonText(
                    text = "MATRIX EDITION",
                    color = NeonMagenta,
                    fontSize = 12.sp,
                    glowColor = NeonMagenta
                )
            }

            // 2. Cyber HUD Profile Summary
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = Color(0x33FF007F),
                glowColor = Color(0x06FF007F)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile avatar placeholder drawn with custom futuristic lines
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RectangleShape)
                            .background(Color(0x1F00F0FF))
                            .drawBehind {
                                drawRect(
                                    color = NeonCyan,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                // Draw horizontal digital mesh vectors
                                drawLine(
                                    color = NeonCyan.copy(alpha = 0.4f),
                                    start = Offset(0f, size.height * 0.4f),
                                    end = Offset(size.width, size.height * 0.4f),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = NeonCyan.copy(alpha = 0.4f),
                                    start = Offset(0f, size.height * 0.6f),
                                    end = Offset(size.width, size.height * 0.6f),
                                    strokeWidth = 1f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        NeonText(
                            text = if (avatarName.contains("cat")) "🐱" else if (avatarName.contains("gamer")) "🎮" else if (avatarName.contains("bot")) "🤖" else "🥷",
                            color = NeonCyan,
                            fontSize = 28.sp,
                            glowColor = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        NeonText(
                            text = "CYBERPAYER #901",
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        NeonText(
                            text = "AVATAR: $avatarLabel",
                            color = NeonCyan.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            NeonText(
                                text = "RACHA ACTIVA: $activeStreak",
                                color = NeonAmber,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Navigation Buttons Menu Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CyberButton(
                    text = "JUGAR VS IA",
                    onClick = { showDifficultyDialog = true },
                    color = NeonCyan,
                    modifier = Modifier.fillMaxWidth(0.95f),
                    testTag = "btn_play_vs_ai"
                )

                CyberButton(
                    text = "MULTIJUGADOR local",
                    onClick = {
                        viewModel.startNewSession("pvp")
                        onNavigateToGame()
                    },
                    color = NeonMagenta,
                    modifier = Modifier.fillMaxWidth(0.95f),
                    testTag = "btn_play_pvp"
                )

                CyberButton(
                    text = "MODO ESPECTADOR (IA vs IA)",
                    onClick = {
                        viewModel.startNewSession("demo")
                        onNavigateToGame()
                    },
                    color = NeonPurple,
                    modifier = Modifier.fillMaxWidth(0.95f),
                    isSecondary = true,
                    testTag = "btn_play_demo"
                )

                CyberButton(
                    text = "ONLINE PvP (AMIGOS)",
                    onClick = onNavigateToOnline,
                    color = NeonEmerald,
                    modifier = Modifier.fillMaxWidth(0.95f),
                    testTag = "btn_play_online"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(0.95f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CyberButton(
                        text = "AJUSTES",
                        onClick = onNavigateToSettings,
                        color = NeonCyan,
                        modifier = Modifier.weight(1f),
                        testTag = "btn_settings"
                    )

                    CyberButton(
                        text = "RECORDS/LOGROS",
                        onClick = onNavigateToStats,
                        color = NeonAmber,
                        modifier = Modifier.weight(1f),
                        testTag = "btn_stats"
                    )
                }

                CyberButton(
                    text = "CRÉDITOS",
                    onClick = onNavigateToCredits,
                    color = NeonEmerald,
                    modifier = Modifier.fillMaxWidth(0.95f),
                    testTag = "btn_credits"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Futuristic version tag
            NeonText(
                text = "© 2026 Shoropio Corporation. Todos los derechos reservados.",
                color = Color.Gray.copy(alpha = 0.8f),
                fontSize = 8.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // 4. Overlap Dialog for Selecting Difficulty Mode
        AnimatedVisibility(
            visible = showDifficultyDialog,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberObsidian.copy(alpha = 0.94f))
                    .clickable { showDifficultyDialog = false },
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clickable(enabled = false) {}, // Block tap clicks passing through
                    borderColor = NeonCyan,
                    glowColor = Color(0x1F00F0FF),
                    backgroundColor = CyberObsidian.copy(alpha = 0.96f),
                    contentPadding = 18.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        NeonText(
                            text = "SECTOR DE DIFICULTAD",
                            color = NeonCyan,
                            fontSize = 18.sp
                        )
                        NeonText(
                            text = "Selecciona la red de la IA:",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Difficulty option buttons
                        val difficulties = listOf(
                            Triple("easy", "IA FÁCIL", NeonEmerald),
                            Triple("normal", "IA MEZCLADA", NeonCyan),
                            Triple("hard", "IA CALCULADA", NeonPurple),
                            Triple("impossible", "ALGORITMO MINIMAX", NeonMagenta)
                        )

                        difficulties.forEach { (key, label, diffColor) ->
                            CyberButton(
                                text = label,
                                onClick = {
                                    showDifficultyDialog = false
                                    viewModel.startNewSession("vs_ai", key)
                                    onNavigateToGame()
                                },
                                color = diffColor,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp
                            )
                        }

                        CyberButton(
                            text = "CANCELAR",
                            onClick = { showDifficultyDialog = false },
                            color = Color.LightGray,
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .padding(top = 8.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
