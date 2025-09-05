package com.redpup.racingregisters.companion.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.lifecycle.ViewModel
import com.redpup.racingregisters.companion.flow.asState
import com.redpup.racingregisters.companion.timer.TimerViewModel
import com.redpup.racingregisters.companion.ui.theme.Grey90
import kotlin.math.hypot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** A view model of the state of the background. */
class BackgroundViewModel(
  val numBackgroundBars: Int,
  val timer: TimerViewModel,
) : ViewModel() {
  val shift = timer.elapsedMilliIncrements.map { it / 1000F }
  val shiftFactor = timer.numResumes.map { it.toFloat() }
  val previousShift = MutableStateFlow(0F)
  val previousTotal = MutableStateFlow(0F)

  /** Accumulates values from shift and shiftFactor into previousShift and previousTotal. */
  suspend fun accumulate() {
    val s = shift.first()
    val sF = shiftFactor.first()
    previousTotal.value += (s - previousShift.value) * sF
    previousShift.value = s
  }
}

/** Renders the background with moving bars. */
// TODO: Fix issue on reset. (Bars go way off screen?)
@Composable
fun RenderBackground(
  viewModel: BackgroundViewModel,
  hurryUpBarColor: State<Color> = Grey90.asState(),
) {
  val shift = viewModel.shift.collectAsState(0F)
  val shiftFactor = viewModel.shiftFactor.collectAsState(0F)
  val previousShift = viewModel.previousShift.collectAsState(0F)
  val previousTotal = viewModel.previousTotal.collectAsState(0F)
  val numBackgroundBarsTimes2 = viewModel.numBackgroundBars * 2

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
      val barWidth = hypotenuse / numBackgroundBarsTimes2
      val xShift = previousTotal.value + (shift.value - previousShift.value) * shiftFactor.value
      for (i in 0..numBackgroundBarsTimes2) {
        val xOffset =
          ((i * 2 + xShift) % numBackgroundBarsTimes2) * barWidth - threeQuartersW
        drawRect(
          color = if (i % 2 == 0) hurryUpBarColor.value else Grey90,
          topLeft = Offset(x = xOffset, y = -halfW),
          size = Size(barWidth, h + w)
        )
      }
    }
  }
}
