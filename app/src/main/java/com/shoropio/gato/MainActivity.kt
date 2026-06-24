package com.shoropio.gato

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.shoropio.gato.notification.GatoMessagingService
import com.shoropio.gato.notification.NotificationHelper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shoropio.gato.data.FirebaseManager
import com.shoropio.gato.ui.CreditsScreen
import com.shoropio.gato.ui.FriendsScreen
import com.shoropio.gato.ui.GameScreen
import com.shoropio.gato.ui.MainMenuScreen
import com.shoropio.gato.ui.NeonText
import com.shoropio.gato.ui.OnlineGameScreen
import com.shoropio.gato.ui.SettingsScreen
import com.shoropio.gato.ui.SplashScreen
import com.shoropio.gato.ui.StatsScreen
import com.shoropio.gato.ui.CyberButton
import com.shoropio.gato.ui.CyberGridBackground
import com.shoropio.gato.ui.theme.GatoTheme
import com.shoropio.gato.ui.theme.NeonCyan
import com.shoropio.gato.ui.theme.NeonMagenta
import com.shoropio.gato.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    var pendingNavRoute: String? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not, we proceed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel
        NotificationHelper.createChannel(this)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Handle deep link from notification
        val navRoute = intent?.getStringExtra("navigate_to")
        if (navRoute != null) {
            pendingNavRoute = navRoute
        }

        setContent {
            val gameViewModel: GameViewModel = viewModel(
                factory = GameViewModel.createFactory(applicationContext)
            )

            val settingsState by gameViewModel.settings.collectAsState()
            val isDarkTheme = settingsState?.darkThemeOn ?: true
            val isDynamicColor = settingsState?.dynamicColorsOn ?: false

            GatoTheme(darkTheme = isDarkTheme, dynamicColor = isDynamicColor) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    GatoNavigationHost(
                        viewModel = gameViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GatoNavigationHost(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val activity = androidx.compose.ui.platform.LocalContext.current as? MainActivity
    val pendingRoute = activity?.pendingNavRoute
    if (pendingRoute != null) {
        activity?.pendingNavRoute = null
    }

    NavHost(
        navController = navController,
        startDestination = if (pendingRoute != null) pendingRoute else "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("menu") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("menu") {
            MainMenuScreen(
                viewModel = viewModel,
                onNavigateToGame = { navController.navigate("game") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToStats = { navController.navigate("stats") },
                onNavigateToCredits = { navController.navigate("credits") },
                onNavigateToOnline = { navController.navigate("online_login") }
            )
        }

        composable("game") {
            GameScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("stats") {
            StatsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("credits") {
            CreditsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("online_login") {
            OnlineLoginScreen(
                onSignedIn = { navController.navigate("friends") { popUpTo("online_login") { inclusive = true } } },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("friends") {
            FriendsScreen(
                onNavigateBack = { navController.popBackStack() },
                onChallengeFriend = { matchId, _ ->
                    navController.navigate("online_game/$matchId")
                }
            )
        }

        composable(
            route = "online_game/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: return@composable
            OnlineGameScreen(
                matchId = matchId,
                onNavigateBack = {
                    navController.popBackStack("menu", false)
                }
            )
        }
    }
}

@Composable
fun OnlineLoginScreen(
    onSignedIn: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    CyberGridBackground(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NeonText(
                    text = "JUEGO ONLINE",
                    color = NeonCyan,
                    fontSize = 28.sp,
                    glowColor = NeonCyan
                )
                NeonText(
                    text = "Ingresa tu nombre para conectarte:",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = userName,
                    onValueChange = { if (it.length <= 20) userName = it },
                    placeholder = {
                        Text(
                            "Tu nombre",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
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

                if (error.isNotEmpty()) {
                    NeonText(text = error, color = NeonMagenta, fontSize = 12.sp)
                }

                CyberButton(
                    text = if (isLoading) "CONECTANDO..." else "CONECTAR",
                    onClick = {
                        if (userName.isBlank()) {
                            error = "Ingresa un nombre"
                            return@CyberButton
                        }
                        isLoading = true
                        error = ""
                        scope.launch {
                            val result = FirebaseManager.signInAnonymously()
                            result.onSuccess {
                                FirebaseManager.createUserProfile(userName.trim())
                                isLoading = false
                                onSignedIn()
                            }.onFailure { e ->
                                isLoading = false
                                error = "Error de conexión: ${e.message}"
                            }
                        }
                    },
                    color = NeonCyan,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    fontSize = 16.sp
                )

                CyberButton(
                    text = "CANCELAR",
                    onClick = onNavigateBack,
                    color = Color.LightGray,
                    modifier = Modifier.fillMaxWidth(0.6f).height(40.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}
