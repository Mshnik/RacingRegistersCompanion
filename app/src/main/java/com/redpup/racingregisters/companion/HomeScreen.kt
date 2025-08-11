package com.redpup.racingregisters.companion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.redpup.racingregisters.companion.timer.TimerViewModel
import com.redpup.racingregisters.companion.ui.RenderPrimaryButton
import com.redpup.racingregisters.companion.ui.theme.Green90
import com.redpup.racingregisters.companion.ui.theme.Grey90
import com.redpup.racingregisters.companion.ui.theme.White90
import kotlin.math.hypot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Wrapper on mutable state visually displayed in this screen.*/
class HomeState(
  val timer: TimerViewModel,
  val backgroundViewModel: BackgroundViewModel = BackgroundViewModel(timer),
  coroutineScope: CoroutineScope,
  val numBackgroundBars: Int,
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
    RenderBackground(state)
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

@Composable
fun RenderBackground(state: HomeState) {
  val numBarsTimes2 = state.numBackgroundBars * 2
  val shift = state.backgroundViewModel.shift.collectAsState(0F)
  val shiftFactor = state.backgroundViewModel.shiftFactor.collectAsState(0F)
  val previousShift = state.backgroundViewModel.previousShift.collectAsState(0F)
  val previousTotal = state.backgroundViewModel.previousTotal.collectAsState(0F)

  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    val w = size.width
    val halfW = w * 0.5F
    val threeQuartersW = w * 0.75F
    val h = size.height
    val hypotenuse = hypot(h, w)
    rotate(degrees = -45F) {
      val barWidth = hypotenuse / numBarsTimes2
      val xShift = previousTotal.value + (shift.value - previousShift.value) * shiftFactor.value
      for (i in 0..numBarsTimes2) {
        val xOffset =
          ((i * 2 + xShift) % numBarsTimes2) * barWidth - threeQuartersW
        drawRect(
          color = Grey90,
          topLeft = Offset(x = xOffset, y = -halfW),
          size = Size(barWidth, h + w)
        )
      }
    }
  }
}
