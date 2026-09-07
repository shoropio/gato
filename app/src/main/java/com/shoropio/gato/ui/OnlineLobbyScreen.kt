package com.shoropio.gato.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoropio.gato.data.FirebaseManager
import com.shoropio.gato.ui.theme.NeonAmber
import com.shoropio.gato.ui.theme.NeonCyan
import com.shoropio.gato.ui.theme.NeonEmerald
import com.shoropio.gato.ui.theme.NeonMagenta
import kotlinx.coroutines.launch

@Composable
fun OnlineLobbyScreen(
    onNavigateBack: () -> Unit,
    onGameStarted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var showNameDialog by remember { mutableStateOf(FirebaseManager.getCurrentUid() == null) }
    var userName by remember { mutableStateOf("") }

    CyberGridBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberButton(
                    text = "< ATRÁS",
                    onClick = onNavigateBack,
                    color = NeonCyan,
                    modifier = Modifier.width(96.dp).height(38.dp),
                    testTag = "btn_lobby_back"
                )
                Spacer(modifier = Modifier.weight(0.2f))
                NeonText(text = "LOBBY ONLINE", color = NeonCyan, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(32.dp))
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan.copy(alpha = 0.3f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    NeonText(
                        text = "PARTIDA ONLINE",
                        color = NeonCyan,
                        fontSize = 16.sp
                    )
                    NeonText(
                        text = "Juega contra amigos en tiempo real.\nInvita a un amigo desde la lista y espera su jugada.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 3
                    )
                }
            }

            CyberButton(
                text = "MIS AMIGOS",
                onClick = { onNavigateBack() },
                color = NeonMagenta,
                modifier = Modifier.fillMaxWidth(0.95f),
                testTag = "btn_go_friends"
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
