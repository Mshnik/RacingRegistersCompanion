package com.redpup.racingregisters.companion

import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.redpup.racingregisters.companion.flow.asState
import com.redpup.racingregisters.companion.timer.TimerViewModel
import com.redpup.racingregisters.companion.ui.BackgroundViewModel
import com.redpup.racingregisters.companion.ui.RenderBackground
import com.redpup.racingregisters.companion.ui.RenderSecondaryButton
import com.redpup.racingregisters.companion.ui.theme.White90
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Wrapper on mutable state visually displayed in this screen.*/
class SettingsState(
  resources: Resources,
  val timer: TimerViewModel,
  coroutineScope: CoroutineScope,
  numBackgroundBars: Int,
  val backgroundViewModel: BackgroundViewModel = BackgroundViewModel(numBackgroundBars, timer),
  internal val initialTimerDuration: Int = resources.getInteger(R.integer.timer_duration_seconds),
  internal val initialHurryUpTime: Int = resources.getInteger(R.integer.timer_hurry_up_seconds),
  internal val initialTransitionDuration: Int = resources.getInteger(R.integer.transition_duration_seconds),
) : ViewModel() {
  init {
    coroutineScope.launch {
      timer.start()
    }
  }

  internal val _timerDuration = MutableStateFlow(initialTimerDuration)
  internal val _hurryUpTime = MutableStateFlow(initialHurryUpTime)
  internal val _transitionDuration = MutableStateFlow(initialTransitionDuration)

  /** Returns [_timerDuration]. */
  fun timerDuration(): StateFlow<Int> = _timerDuration

  /** Returns [hurryUpTime]. */
  fun hurryUpTime(): StateFlow<Int> = _hurryUpTime

  /** Returns [transitionDuration]. */
  fun transitionDuration(): StateFlow<Int> = _transitionDuration
}

@Composable
fun SettingsScreen(
  state: SettingsState,
  navController: NavController,
) {
  Scaffold(topBar = { RenderSettingsTopBar(navController) }) { innerPadding ->
    RenderBackground(state.backgroundViewModel)
    Box(Modifier.padding(innerPadding))
    RenderSettingsScreen(state)
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

@Composable
fun RenderSettingsScreen(state: SettingsState) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    RenderSlider(state._timerDuration, state.initialTimerDuration, 20, 1.0f..20.0f)
  }
}

@Composable
fun RenderSlider(
  state: MutableStateFlow<Int>,
  initial: Int,
  steps: Int,
  valueRange: ClosedFloatingPointRange<Float>,
) {
  val timerDuration = state.map { (it / 60).toFloat() }.collectAsState(initial.toFloat())
  val timerString = TimerViewModel.formatTime(state.collectAsState().value)
  Row {
    Text(text = timerString, color = White90)
    Slider(
      value = timerDuration.value,
      onValueChange = { newValue -> state.value = newValue.roundToInt() * 60 },
      steps = steps,
      valueRange = valueRange
    )
  }
}