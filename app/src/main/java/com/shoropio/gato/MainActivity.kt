package com.shoropio.gato

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shoropio.gato.ui.CreditsScreen
import com.shoropio.gato.ui.GameScreen
import com.shoropio.gato.ui.MainMenuScreen
import com.shoropio.gato.ui.SettingsScreen
import com.shoropio.gato.ui.SplashScreen
import com.shoropio.gato.ui.StatsScreen
import com.shoropio.gato.ui.theme.GatoTheme
import com.shoropio.gato.viewmodel.GameViewModel

/**
 * Main activity for Gato (Tic-Tac-Toe) Matrix Edition.
 * Manages Edge-to-Edge displays and sets up the central Navigation Graph.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Instantiate our GameViewModel using our customized Factoring provider
            val gameViewModel: GameViewModel = viewModel(
                factory = GameViewModel.createFactory(applicationContext)
            )

            val settingsState by gameViewModel.settings.collectAsState()
            
            // Toggle dark Mode dynamically based on the stored database preference
            val isDarkTheme = settingsState?.darkThemeOn ?: true

            GatoTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Navigation NavHost
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

    NavHost(
        navController = navController,
        startDestination = "splash",
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
                onNavigateToCredits = { navController.navigate("credits") }
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
    }
}
