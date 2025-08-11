package com.redpup.racingregisters.companion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.redpup.racingregisters.companion.timer.TimerViewModel
import com.redpup.racingregisters.companion.ui.BackgroundViewModel
import com.redpup.racingregisters.companion.ui.RenderBackground
import com.redpup.racingregisters.companion.ui.RenderPrimaryButton
import com.redpup.racingregisters.companion.ui.theme.Green90
import com.redpup.racingregisters.companion.ui.theme.White90
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Wrapper on mutable state visually displayed in this screen.*/
class HomeState(
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
fun HomeScreen(
  state: HomeState,
  navController: NavController,
) {
  Scaffold { innerPadding ->
    RenderBackground(state.backgroundViewModel)
    RenderScreen(navController, Modifier.padding(innerPadding))
  }
}

@Composable
fun RenderScreen(
  navController: NavController,
  modifier: Modifier,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    RenderPrimaryButton(
      "PLAY",
      textColor = Green90,
      backgroundColor = Color.Black,
      borderColor = Green90,
    ) {
      navController.navigate(Screen.Game.route)
    }
    RenderPrimaryButton(
      "SETTINGS",
      textColor = White90,
      backgroundColor = Color.Black,
      borderColor = White90,
    ) {
      navController.navigate(Screen.Settings.route)
    }
  }
}
