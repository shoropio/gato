package com.shoropio.gato.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoropio.gato.ui.theme.NeonAmber
import com.shoropio.gato.ui.theme.NeonCyan
import com.shoropio.gato.ui.theme.NeonEmerald
import com.shoropio.gato.ui.theme.NeonMagenta
import com.shoropio.gato.ui.theme.NeonPurple
import com.shoropio.gato.viewmodel.GameViewModel

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settingsState by viewModel.settings.collectAsState()
    val cosmeticsList by viewModel.allCosmetics.collectAsState()

    val scrollState = rememberScrollState()

    val currentSound = settingsState?.soundOn ?: true
    val currentVib = settingsState?.vibrationOn ?: true
    val currentBoardStyle = settingsState?.boardStyle ?: "default"
    val currentAvatar = settingsState?.selectedAvatar ?: "avatar_cyber_cat"

    // Check if Golden Prestige is unlocked in DB
    val isGoldUnlocked = cosmeticsList.find { it.cosmeticId == "theme_golden_prestige" }?.isUnlocked ?: false

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
                    testTag = "btn_settings_back"
                )
                Spacer(modifier = Modifier.weight(0.2f))
                NeonText(
                    text = "AJUSTES",
                    color = NeonCyan,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.width(32.dp))
            }

            // SECTION 1: Hardware controls
            NeonText(
                text = "DISPOSITIVOS Y FEEDBACK",
                color = NeonMagenta.copy(alpha = 0.9f),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    // Sound Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            NeonText(text = "SONIDO SINTETIZADO", color = Color.White, fontSize = 14.sp)
                            NeonText(
                                text = "Tonos de audio generados proceduralmente al jugar",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = currentSound,
                            onCheckedChange = { viewModel.toggleSound() },
                            modifier = Modifier.testTag("switch_sound"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = Color(0x3300F0FF),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0x1AFFFFFF)
                            )
                        )
                    }

                    // Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            NeonText(text = "RIPPLE VIBRACIÓN HÁPTICA", color = Color.White, fontSize = 14.sp)
                            NeonText(
                                text = "Pequeño pulso mecánico al tocar casillas y botones",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = currentVib,
                            onCheckedChange = { viewModel.toggleVibration() },
                            modifier = Modifier.testTag("switch_vibration"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonMagenta,
                                checkedTrackColor = Color(0x33FF007F),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0x1AFFFFFF)
                            )
                        )
                    }
                }
            }

            // SECTION 2: Custom boards style
            NeonText(
                text = "ESTILO INTERFAZ Y NEÓN",
                color = NeonMagenta.copy(alpha = 0.9f),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    val stylesList = listOf(
                        Triple("default", "NEÓN CYBORG (Default)", Pair(NeonCyan, NeonMagenta)),
                        Triple("vaporwave", "SUNSET VAPORWAVE", Pair(NeonMagenta, NeonPurple)),
                        Triple("emerald", "GREEN ESCREVER (Emerald)", Pair(NeonEmerald, NeonCyan)),
                        Triple("gold", "PRESTIGIO DORADO", Pair(NeonAmber, Color(0xFFE2E8F0)))
                    )

                    stylesList.forEach { (styleKey, styleLabel, colors) ->
                        val isLocked = styleKey == "gold" && !isGoldUnlocked
                        val isSelected = currentBoardStyle == styleKey && !isLocked

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RectangleShape)
                                .background(if (isSelected) Color(0x1A00F0FF) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NeonCyan.copy(alpha = 0.4f) else Color.Transparent,
                                    shape = RectangleShape
                                )
                                .clickable(enabled = !isLocked) { viewModel.setBoardStyle(styleKey) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                NeonText(
                                    text = styleLabel,
                                    color = if (isLocked) Color.DarkGray else if (isSelected) Color.White else Color.LightGray,
                                    fontSize = 13.sp
                                )
                                if (isLocked) {
                                    NeonText(
                                        text = "🔓 Desbloqueo: Vencer a la IA Imposible VS IA",
                                        color = NeonAmber,
                                        fontSize = 10.sp
                                    )
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(RectangleShape)
                                                .background(colors.first)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(RectangleShape)
                                                .background(colors.second)
                                        )
                                    }
                                }
                            }

                            if (isLocked) {
                                NeonText(text = "🔒 LCK", color = NeonAmber, fontSize = 11.sp)
                            } else if (isSelected) {
                                NeonText(text = "✔ ACTIVO", color = NeonCyan, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // SECTION 3: Avatar selection
            NeonText(
                text = "SELEC IDENTIDAD CYBORG",
                color = NeonMagenta.copy(alpha = 0.9f),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val avatars = listOf(
                        Pair("avatar_cyber_cat", "🐱"),
                        Pair("avatar_glitch_gamer", "🎮"),
                        Pair("avatar_minimax_bot", "🤖"),
                        Pair("avatar_pixel_ninja", "🥷")
                    )

                    avatars.forEach { (avatarKey, avatarEmoji) ->
                        val isSelected = currentAvatar == avatarKey
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RectangleShape)
                                .clickable { viewModel.setAvatar(avatarKey) }
                                .padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RectangleShape)
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else Color(0x1AFFFFFF))
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) NeonCyan else Color.Transparent,
                                        shape = RectangleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = avatarEmoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            NeonText(
                                text = avatarKey.replace("avatar_", "").replace("_", " ").uppercase(),
                                color = if (isSelected) NeonCyan else Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
