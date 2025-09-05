package com.redpup.racingregisters.companion

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
import kotlinx.coroutines.launch

/** Wrapper on mutable state visually displayed in this screen.*/
class SettingsState(
  val timer: TimerViewModel,
  coroutineScope: CoroutineScope,
  numBackgroundBars: Int,
  val backgroundViewModel: BackgroundViewModel = BackgroundViewModel(numBackgroundBars, timer),
) : ViewModel() {
  init {
    coroutineScope.launch {
      timer.start()
    }
  }
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