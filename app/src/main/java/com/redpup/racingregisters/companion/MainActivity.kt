package com.redpup.racingregisters.companion

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.redpup.racingregisters.companion.timer.TimerViewModel
import com.redpup.racingregisters.companion.ui.theme.RacingRegistersCompanionTheme

/** Different screens in the app. */
sealed class Screen(val route: String) {
  data object Home : Screen("home")
  data object Game : Screen("game")
  data object Settings : Screen("settings")
}

/** Main activity of the whole app. */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    val numBackgroundBars = baseContext.resources.getInteger(R.integer.num_background_bars)

    val homeState = HomeState(
      TimerViewModel(0, countDown = false),
      coroutineScope = lifecycleScope,
      numBackgroundBars = numBackgroundBars
    )

    val settingsState = SettingsState(
      baseContext.resources,
      TimerViewModel(0, countDown = false),
      coroutineScope = lifecycleScope,
      numBackgroundBars = numBackgroundBars
    )

    val gameState = GameState(
      TimerViewModel(settingsState.getTimerDuration()),
      settingsState.getHurryUpTime(),
      TimerViewModel(
        settingsState.getTransitionDuration(),
        completeAtIncrements = 1,
        completionMessage = "GO!"
      ),
      music = BackgroundMusic(baseContext),
      soundEffects = SoundEffects(baseContext),
      coroutineScope = lifecycleScope,
      numBackgroundBars = numBackgroundBars
    )
    gameState.setupMusic(baseContext.resources.getFloat(R.dimen.music_volume_master))
    gameState.setupSound()

    enableEdgeToEdge()
    setContent {
      RacingRegistersCompanionTheme {
        val navController = rememberNavController()
        NavHost(navController, startDestination = Screen.Home.route) {
          composable(Screen.Home.route) {
            HomeScreen(homeState, navController)
          }
          composable(Screen.Game.route) {
            GameScreen(gameState, navController, lifecycleScope)
          }
          composable(Screen.Settings.route) {
            SettingsScreen(settingsState, navController)
          }
        }
      }
    }
  }
}
