package com.redpup.racingregisters.companion

import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.redpup.racingregisters.companion.timer.TimerViewModel
import com.redpup.racingregisters.companion.ui.theme.RacingRegistersCompanionTheme

/** Different screens in the app. */
sealed class Screen(val route: String) {
  data object Home : Screen("home")
  data object Game : Screen("game")
}

/** Main activity of the whole app. */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    val timerDuration = baseContext.resources.getInteger(R.integer.timer_duration_seconds)
    val hurryUpTime = baseContext.resources.getInteger(R.integer.timer_hurry_up_seconds)
    val transitionDuration = baseContext.resources.getInteger(R.integer.transition_duration_seconds)
    val numBackgroundBars = baseContext.resources.getInteger(R.integer.num_background_bars)

    val homeState = HomeState(
      TimerViewModel(0, countDown = false),
      coroutineScope = lifecycleScope,
      numBackgroundBars = numBackgroundBars
    )

    val gameState = GameState(
      TimerViewModel(timerDuration),
      hurryUpTime,
      TimerViewModel(transitionDuration, completeAtIncrements = 1, completionMessage = "GO!"),
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
        }
      }
    }
  }
}
