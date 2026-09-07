package com.shoropio.gato.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoropio.gato.data.AchievementEntity
import com.shoropio.gato.data.GameStatsEntity
import com.shoropio.gato.ui.theme.NeonAmber
import com.shoropio.gato.ui.theme.NeonCyan
import com.shoropio.gato.ui.theme.NeonEmerald
import com.shoropio.gato.ui.theme.NeonMagenta
import com.shoropio.gato.ui.theme.NeonPurple
import com.shoropio.gato.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statsList by viewModel.allStats.collectAsState()
    val achievementsList by viewModel.allAchievements.collectAsState()

    val scrollState = rememberScrollState()
    var selectedStatsTab by remember { mutableStateOf("all") } // "all" or "achievements"

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

            // Back Navigation and Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberButton(
                    text = "< ATRÁS",
                    onClick = onNavigateBack,
                    color = NeonCyan,
                    modifier = Modifier
                        .width(96.dp)
                        .height(38.dp),
                    testTag = "btn_stats_back"
                )
                Spacer(modifier = Modifier.weight(0.2f))
                NeonText(
                    text = "RECORDS",
                    color = NeonCyan,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.width(32.dp))
            }

            // Tab toggler buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RectangleShape)
                    .background(Color(0x1F121824))
                    .border(width = 1.dp, color = Color(0x3300F0FF), shape = RectangleShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RectangleShape)
                        .background(if (selectedStatsTab == "all") NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { selectedStatsTab = "all" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NeonText(
                        text = "HISTORIAL",
                        color = if (selectedStatsTab == "all") NeonCyan else Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RectangleShape)
                        .background(if (selectedStatsTab == "achievements") NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { selectedStatsTab = "achievements" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NeonText(
                        text = "LOGROS",
                        color = if (selectedStatsTab == "achievements") NeonCyan else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TAB 1: Mode Statistics
            if (selectedStatsTab == "all") {
                statsList.forEach { modeStats ->
                    val modeLabel = when (modeStats.modeId) {
                        "pvp" -> "MULTIJUGADOR JvJ LOC"
                        "vs_ai_easy" -> "CÓDIGO VS IA FÁCIL"
                        "vs_ai_normal" -> "CÓDIGO VS IA NORMAL"
                        "vs_ai_hard" -> "CÓDIGO VS IA DIFÍCIL"
                        "vs_ai_impossible" -> "CÓDIGO VS IA IMPOSIBLE"
                        else -> "DUELOS ESPECTADOR DE IA"
                    }
                    val accentColor = when (modeStats.modeId) {
                        "pvp" -> NeonMagenta
                        "vs_ai_easy" -> NeonEmerald
                        "vs_ai_impossible" -> NeonAmber
                        else -> NeonCyan
                    }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = accentColor.copy(alpha = 0.3f),
                        glowColor = accentColor.copy(alpha = 0.05f)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            NeonText(
                                text = modeLabel,
                                color = accentColor,
                                fontSize = 13.sp
                            )
                            
                            // Stats grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    NeonText(text = "PARTIDAS", color = Color.Gray, fontSize = 9.sp)
                                    NeonText(text = modeStats.totalPlayed.toString(), color = Color.White, fontSize = 15.sp)
                                }
                                Column {
                                    NeonText(text = "VICTORIAS", color = NeonEmerald, fontSize = 9.sp)
                                    NeonText(text = modeStats.wins.toString(), color = NeonEmerald, fontSize = 15.sp)
                                }
                                Column {
                                    NeonText(text = "DERROTAS", color = NeonMagenta, fontSize = 9.sp)
                                    NeonText(text = modeStats.losses.toString(), color = NeonMagenta, fontSize = 15.sp)
                                }
                                Column {
                                    NeonText(text = "EMPATES", color = Color.LightGray, fontSize = 9.sp)
                                    NeonText(text = modeStats.draws.toString(), color = Color.LightGray, fontSize = 15.sp)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x0CFFFFFF))
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NeonText(text = "RACHA ACTUAL / MÁXIMA:", color = Color.Gray, fontSize = 10.sp)
                                NeonText(
                                    text = "${modeStats.currentStreak} / ${modeStats.maxStreak}",
                                    color = NeonAmber,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // TAB 2: System of Achievements (Logros List)
                achievementsList.forEach { ach ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (ach.isUnlocked) NeonEmerald.copy(alpha = 0.35f) else Color(0x1FFFFFFF),
                        glowColor = if (ach.isUnlocked) NeonEmerald.copy(alpha = 0.05f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge Icon
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RectangleShape)
                                    .background(if (ach.isUnlocked) NeonEmerald.copy(alpha = 0.15f) else Color(0x0DFFFFFF))
                                    .border(
                                        width = 1.5.dp,
                                        color = if (ach.isUnlocked) NeonEmerald else Color.DarkGray,
                                        shape = RectangleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                NeonText(
                                    text = if (ach.isUnlocked) "🏆" else "🔒",
                                    color = if (ach.isUnlocked) NeonEmerald else Color.DarkGray,
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                NeonText(
                                    text = ach.title,
                                    color = if (ach.isUnlocked) Color.White else Color.Gray,
                                    fontSize = 13.sp
                                )
                                NeonText(
                                    text = ach.description,
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Show either unlock timestamp or progress bar
                                if (ach.isUnlocked) {
                                    val dateStr = try {
                                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        sdf.format(Date(ach.timestamp))
                                    } catch (e: Exception) {
                                        "RECIENTE"
                                    }
                                    NeonText(
                                        text = "🔓 DESBLOQUEADO • $dateStr",
                                        color = NeonEmerald,
                                        fontSize = 9.sp
                                    )
                                } else {
                                    val progressFraction = if (ach.maxProgress > 0) {
                                        ach.progress.toFloat() / ach.maxProgress.toFloat()
                                    } else {
                                        0f
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        LinearProgressIndicator(
                                            progress = { progressFraction },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RectangleShape),
                                            color = NeonPurple,
                                            trackColor = Color(0x33FFFFFF)
                                        )
                                        NeonText(
                                            text = "PROGRESO: ${ach.progress}/${ach.maxProgress}",
                                            color = NeonPurple,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
