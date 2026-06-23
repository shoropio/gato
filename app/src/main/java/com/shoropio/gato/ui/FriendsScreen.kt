package com.shoropio.gato.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoropio.gato.data.FirebaseManager
import com.shoropio.gato.data.GameMatch
import com.shoropio.gato.data.OnlineUser
import com.shoropio.gato.ui.theme.CyberObsidian
import com.shoropio.gato.ui.theme.NeonAmber
import com.shoropio.gato.ui.theme.NeonCyan
import com.shoropio.gato.ui.theme.NeonEmerald
import com.shoropio.gato.ui.theme.NeonMagenta
import kotlinx.coroutines.launch

@Composable
fun FriendsScreen(
    onNavigateBack: () -> Unit,
    onChallengeFriend: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val friendsList by FirebaseManager.observeFriends().collectAsState(initial = emptyList())
    val friendRequests by FirebaseManager.observeFriendRequests().collectAsState(initial = emptyList())
    val activeMatches by FirebaseManager.observeMyActiveMatches().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<OnlineUser>>(emptyList()) }
    var showSearch by remember { mutableStateOf(false) }

    fun doSearch() {
        scope.launch {
            searchResults = FirebaseManager.searchUsers(searchQuery)
        }
    }

    CyberGridBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberButton(
                    text = "< VOLVER",
                    onClick = onNavigateBack,
                    color = NeonCyan,
                    modifier = Modifier.width(96.dp).height(38.dp),
                    testTag = "btn_friends_back"
                )
                Spacer(modifier = Modifier.weight(0.2f))
                NeonText(text = "AMIGOS", color = NeonCyan, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(32.dp))
            }

            if (friendRequests.isNotEmpty()) {
                NeonText(
                    text = "SOLICITUDES PENDIENTES",
                    color = NeonAmber,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonAmber.copy(alpha = 0.3f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        friendRequests.forEach { req ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(0.dp))
                                            .background(NeonAmber.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) { Text(text = "👤", fontSize = 18.sp) }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    NeonText(
                                        text = req.displayName,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CyberButton(
                                        text = "✓",
                                        onClick = {
                                            scope.launch { FirebaseManager.acceptFriendRequest(req.uid) }
                                        },
                                        color = NeonEmerald,
                                        modifier = Modifier.size(42.dp),
                                        fontSize = 16.sp
                                    )
                                    CyberButton(
                                        text = "✗",
                                        onClick = {
                                            scope.launch { FirebaseManager.rejectFriendRequest(req.uid) }
                                        },
                                        color = NeonMagenta,
                                        modifier = Modifier.size(42.dp),
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Active matches section
            if (activeMatches.isNotEmpty()) {
                NeonText(
                    text = "PARTIDAS ACTIVAS",
                    color = NeonEmerald,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                activeMatches.forEach { match ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonEmerald.copy(alpha = 0.3f),
                        glowColor = NeonEmerald.copy(alpha = 0.05f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                NeonText(
                                    text = "${match.playerXName} vs ${match.playerOName}",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                NeonText(
                                    text = match.status.uppercase(),
                                    color = NeonEmerald,
                                    fontSize = 10.sp
                                )
                            }
                            CyberButton(
                                text = "UNIRSE",
                                onClick = { onChallengeFriend(match.id, "") },
                                color = NeonEmerald,
                                modifier = Modifier.width(80.dp).height(34.dp),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Friends list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonText(text = "MIS AMIGOS", color = NeonMagenta.copy(alpha = 0.9f), fontSize = 12.sp)
                CyberButton(
                    text = if (showSearch) "CERRAR" else "+ AGREGAR",
                    onClick = { showSearch = !showSearch; if (!showSearch) searchQuery = "" },
                    color = if (showSearch) NeonMagenta else NeonCyan,
                    modifier = Modifier.width(100.dp).height(32.dp),
                    fontSize = 10.sp
                )
            }

            if (showSearch) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Buscar por nombre...",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("field_search_users"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { doSearch() }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0x1A121824),
                                unfocusedContainerColor = Color(0x1A121824),
                                focusedIndicatorColor = NeonCyan,
                                unfocusedIndicatorColor = Color(0x3300F0FF),
                                cursorColor = NeonCyan,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CyberButton(
                            text = "BUSCAR",
                            onClick = { doSearch() },
                            color = NeonCyan,
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            fontSize = 12.sp
                        )
                        if (searchResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            searchResults.forEach { user ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    NeonText(text = user.displayName, color = Color.White, fontSize = 13.sp)
                                    CyberButton(
                                        text = "ENVIAR SOL.",
                                        onClick = {
                                            scope.launch {
                                                FirebaseManager.sendFriendRequest(user.uid)
                                                searchQuery = ""
                                                searchResults = emptyList()
                                                showSearch = false
                                            }
                                        },
                                        color = NeonCyan,
                                        modifier = Modifier.width(110.dp).height(32.dp),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        } else if (searchQuery.isNotBlank()) {
                            NeonText(
                                text = "Sin resultados",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (friendsList.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0x33FFFFFF)
                ) {
                    NeonText(
                        text = "No tienes amigos agregados.\nUsa 'AGREGAR' para buscar y enviar solicitudes.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp),
                        maxLines = 3
                    )
                }
            } else {
                friendsList.forEach { friend ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (friend.isOnline) NeonEmerald.copy(alpha = 0.3f) else Color(0x1FFFFFFF),
                        glowColor = if (friend.isOnline) NeonEmerald.copy(alpha = 0.05f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(0.dp))
                                        .background(if (friend.isOnline) NeonEmerald else Color.DarkGray)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                NeonText(
                                    text = friend.displayName,
                                    color = if (friend.isOnline) Color.White else Color.Gray,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                NeonText(
                                    text = if (friend.isOnline) "● EN LINEA" else "○ DESCONECTADO",
                                    color = if (friend.isOnline) NeonEmerald else Color.DarkGray,
                                    fontSize = 9.sp
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (friend.isOnline) {
                                    CyberButton(
                                        text = "JUGAR",
                                        onClick = { onChallengeFriend(friend.uid, friend.displayName) },
                                        color = NeonEmerald,
                                        modifier = Modifier.width(64.dp).height(32.dp),
                                        fontSize = 10.sp
                                    )
                                }
                                CyberButton(
                                    text = "✗",
                                    onClick = { scope.launch { FirebaseManager.removeFriend(friend.uid) } },
                                    color = NeonMagenta,
                                    modifier = Modifier.size(32.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
