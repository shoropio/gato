package com.shoropio.gato.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoropio.gato.ui.theme.NeonCyan
import com.shoropio.gato.ui.theme.NeonEmerald
import com.shoropio.gato.ui.theme.NeonMagenta

private val openSourceCredits = listOf(
    "AndroidX Core, Activity, Lifecycle y Navigation",
    "Jetpack Compose UI y Material 3",
    "Room Database",
    "Kotlin Coroutines",
    "Kotlin Symbol Processing",
    "Moshi",
    "OkHttp",
    "Retrofit",
    "Robolectric",
    "Roborazzi"
)

@Composable
fun CreditsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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

            NeonText(
                text = "CÓDIGO ABIERTO",
                color = NeonMagenta.copy(alpha = 0.9f),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    openSourceCredits.forEach { name ->
                        OpenSourceCredit(name = name)
                    }
                }
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonEmerald.copy(alpha = 0.3f),
                glowColor = NeonEmerald.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NeonText(text = "SHOROPIO CORPORATION", color = NeonEmerald, fontSize = 14.sp)
                    Text(
                        text = "© 2026 Shoropio Corporation. Todos los derechos reservados.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun OpenSourceCredit(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NeonText(text = ">", color = NeonCyan, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        Text(
            text = name,
            color = Color.LightGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
