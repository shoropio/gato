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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.shoropio.gato.data.FirebaseManager
import com.shoropio.gato.notification.GatoMessagingService
import com.shoropio.gato.notification.NotificationHelper
import com.shoropio.gato.ui.CreditsScreen
import com.shoropio.gato.ui.FriendsScreen
import com.shoropio.gato.ui.GameScreen
import com.shoropio.gato.ui.MainMenuScreen
import com.shoropio.gato.ui.NeonText
import com.shoropio.gato.ui.OnlineGameScreen
import com.shoropio.gato.ui.SettingsScreen
import com.shoropio.gato.ui.StatsScreen
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
    var pendingSync: Boolean = false

    val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken ?: return@registerForActivityResult
            kotlinx.coroutines.MainScope().launch {
                FirebaseManager.signInWithGoogle(idToken).onSuccess {
                    FirebaseManager.createUserProfile()
                    pendingNavRoute = "friends"
                    pendingSync = true
                    Toast.makeText(this@MainActivity, "Sesión iniciada con Google", Toast.LENGTH_SHORT).show()
                    recreate()
                }.onFailure { e ->
                    Log.e("GoogleSignIn", "Error: ${e.message}")
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Sign in failed: ${e.statusCode}")
        }
    }

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

            // Sync Firebase data to Room on fresh login
            if (pendingSync && FirebaseManager.isSignedIn()) {
                LaunchedEffect(Unit) {
                    FirebaseManager.pullAllFromFirebase(gameViewModel.repository)
                    pendingSync = false
                }
            }

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
            val activity = LocalContext.current as MainActivity
            if (FirebaseManager.isSignedIn()) {
                LaunchedEffect(Unit) {
                    navController.navigate("friends") { popUpTo("online_login") { inclusive = true } }
                }
            } else {
                OnlineLoginScreen(
                    onSignInWithGoogle = {
                        activity.googleSignInLauncher.launch(
                            FirebaseManager.getGoogleSignInClient(activity).signInIntent
                        )
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable("friends") {
            val activity = LocalContext.current as MainActivity
            FriendsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    FirebaseManager.signOut(activity)
                    navController.navigate("menu") { popUpTo("menu") { inclusive = true } }
                },
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
    onSignInWithGoogle: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }

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
                    text = "Inicia sesión con Google para jugar contra amigos:",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        color = NeonCyan,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp
                    )
                    NeonText(text = "INICIANDO SESIÓN...", color = NeonCyan, fontSize = 12.sp)
                }

                CyberButton(
                    text = if (isLoading) "INICIANDO..." else "INICIAR SESIÓN CON GOOGLE",
                    onClick = {
                        isLoading = true
                        onSignInWithGoogle()
                    },
                    color = if (isLoading) Color.Gray else NeonCyan,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    fontSize = 14.sp,
                    enabled = !isLoading
                )

                if (!isLoading) {
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
}
