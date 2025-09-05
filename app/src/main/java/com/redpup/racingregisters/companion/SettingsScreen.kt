package com.redpup.racingregisters.companion

import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.redpup.racingregisters.companion.timer.TimerViewModel
import com.redpup.racingregisters.companion.ui.BackgroundViewModel
import com.redpup.racingregisters.companion.ui.RenderBackground
import com.redpup.racingregisters.companion.ui.RenderSecondaryButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/** Wrapper on mutable state visually displayed in this screen.*/
class SettingsState(
  resources: Resources,
  val timer: TimerViewModel,
  coroutineScope: CoroutineScope,
  numBackgroundBars: Int,
  val backgroundViewModel: BackgroundViewModel = BackgroundViewModel(numBackgroundBars, timer),
  private val timerDuration: Int = resources.getInteger(R.integer.timer_duration_seconds),
  private val hurryUpTime: Int = resources.getInteger(R.integer.timer_hurry_up_seconds),
  private val transitionDuration: Int = resources.getInteger(R.integer.transition_duration_seconds),
) : ViewModel() {
  init {
    coroutineScope.launch {
      timer.start()
    }
  }

  /** Returns [timerDuration]. */
  fun getTimerDuration(): StateFlow<Int> = MutableStateFlow(timerDuration)

  /** Returns [hurryUpTime]. */
  fun getHurryUpTime(): StateFlow<Int> = MutableStateFlow(hurryUpTime)

  /** Returns [transitionDuration]. */
  fun getTransitionDuration(): StateFlow<Int> = MutableStateFlow(transitionDuration)
}

@Composable
fun SettingsScreen(
  state: SettingsState,
  navController: NavController,
) {
  Scaffold(topBar = { RenderSettingsTopBar(navController) }) { innerPadding ->
    RenderBackground(state.backgroundViewModel)
    Box(Modifier.padding(innerPadding))
  }
}

@Composable
fun RenderSettingsTopBar(
  navController: NavController,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .offset(0.dp, 40.dp)
      .padding(20.dp),
    horizontalArrangement = Arrangement.Start
  ) {
    RenderSecondaryButton(
      R.drawable.back,
      "Back Button"
    ) {
      navController.navigateUp()
    }
  }
}