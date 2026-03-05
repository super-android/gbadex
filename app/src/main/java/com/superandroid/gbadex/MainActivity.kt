package com.superandroid.gbadex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.superandroid.gbadex.ui.screens.LibraryScreen
import com.superandroid.gbadex.ui.theme.GBADexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GBADexTheme {
                GBADexApp()
            }
        }
    }
}

@Composable
fun GBADexApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "library") {

        composable("library") {
            LibraryScreen(
                onGameLaunch = { game ->
                    // Phase 3: navigate to emulator screen
                    // navController.navigate("emulator/${game.id}")
                },
                onChangeBoxArt = { game ->
                    // Phase 5: navigate to box art picker screen
                    // navController.navigate("boxart/${game.id}")
                }
            )
        }

        // Placeholder routes — we'll fill these in as we build each phase
        // composable("emulator/{gameId}") { ... }
        // composable("boxart/{gameId}") { ... }
    }
}
