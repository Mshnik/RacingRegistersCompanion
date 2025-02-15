package com.redpup.racingregisters.companion

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redpup.racingregisters.companion.timer.Event
import com.redpup.racingregisters.companion.timer.Timer
import com.redpup.racingregisters.companion.ui.theme.Green90
import com.redpup.racingregisters.companion.ui.theme.Grey90
import com.redpup.racingregisters.companion.ui.theme.RacingRegistersCompanionTheme
import com.redpup.racingregisters.companion.ui.theme.White90
import com.redpup.racingregisters.companion.ui.theme.mPlus1Code
import com.redpup.racingregisters.companion.ui.theme.sixtyFour
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val timer = Timer(baseContext.resources.getInteger(R.integer.default_duration_seconds))

    enableEdgeToEdge()
    setContent {
      RacingRegistersCompanionTheme {
        Scaffold { innerPadding ->
          RenderBackground(timer)
          RenderScreen(timer, Modifier.padding(innerPadding))
        }
      }
    }
  }
}

@Composable
fun RenderScreen(timer: Timer, modifier: Modifier) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(10.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    RenderedTimer(
      timer = timer
    )
    Spacer(Modifier.height(40.dp))
    RenderBreakContinueButton(timer)
  }
}

@Composable
fun RenderBackground(timer: Timer) {
  val numBars = 10
  val numBarsTimes2 = numBars * 2
  var shift by remember { mutableFloatStateOf(0.0F) }
  var shiftFactor by remember { mutableFloatStateOf(0.0F) }
  var previousShift by remember { mutableFloatStateOf(0.0F) }
  var previousShiftWithFactor by remember { mutableFloatStateOf(0.0F) }

  timer.subscribe(Event.TICK) { shift = timer.elapsedMillis() / 1000F }
  timer.subscribe(Event.ACTIVATE) { shiftFactor = timer.numResumes.toFloat() }
  timer.subscribe(Event.DEACTIVATE) {
    previousShiftWithFactor += (shift - previousShift) * shiftFactor
    previousShift = shift
  }

  Canvas(modifier = Modifier.fillMaxSize()) {
    rotate(degrees = -45F) {
      val barWidth =
        (sqrt(size.width * size.width + size.height * size.height.toDouble()) / (numBars * 2)).toFloat()
      val xShift = previousShiftWithFactor + (shift - previousShift) * shiftFactor
      println("xOffset computation: $xShift = ($previousShiftWithFactor, $shift, $previousShift, $shiftFactor)")
      for (i in 0..numBarsTimes2) {
        val xOffset =
          ((i * 2 + xShift) % numBarsTimes2) * barWidth - size.width * 0.75F
        drawRect(
          color = Grey90,
          topLeft = Offset(x = xOffset, y = -size.width / 2),
          size = Size(barWidth, size.height + size.width)
        )
      }
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RenderedTimer(timer: Timer) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val currentTime = remember { mutableStateOf(timer.toString()) }
    val timerFont = TextStyle(
      fontFamily = mPlus1Code,
      fontWeight = FontWeight.Bold,
      fontSize = 100.sp,
      lineHeight = 0.sp,
      letterSpacing = 4.sp,
    )
    timer.subscribe(Event.SECOND) { currentTime.value = timer.toString() }
    Box {
      Text(text = currentTime.value,
           modifier = Modifier
             .semantics { invisibleToUser() }
             .padding(5.dp),
           style = timerFont.copy(drawStyle = Stroke(width = 30F), color = Color.Black))
      Text(text = currentTime.value, modifier = Modifier.padding(5.dp), style = timerFont)
    }
  }
}

@Composable
fun RenderBreakContinueButton(
  timer: Timer,
  modifier: Modifier = Modifier,
  initialText: String = "CONTINUE",
) {
  var text by remember { mutableStateOf(initialText) }
  var textColor by remember { mutableStateOf(Color.Black) }
  var backgroundColor by remember { mutableStateOf(Color.Black) }
  var borderColor by remember { mutableStateOf(Color.Black) }

  fun updateColors() {
    val isBreak = text == "BREAK"
    textColor = if (isBreak) Color.Black else Green90
    backgroundColor = if (isBreak) White90 else Color.Black
    borderColor = if (isBreak) White90 else Green90
  }
  updateColors()

  val buttonFont = TextStyle(
    fontFamily = sixtyFour,
    fontWeight = FontWeight.Bold,
    fontSize = 23.sp,
    lineHeight = 0.sp,
    letterSpacing = 2.sp
  )

  val borderThickness = 3.dp

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(borderThickness * 2))
      .background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    Button(
      onClick = {
        timer.toggle()
        text = if (text == "BREAK") "CONTINUE" else "BREAK"
        updateColors()
      },
      border = BorderStroke(
        width = borderThickness, color = borderColor
      ),
      colors = ButtonColors(backgroundColor, textColor, backgroundColor, textColor),
      shape = RoundedCornerShape(borderThickness),
      modifier = modifier.padding(borderThickness)
    ) {
      Text(
        text, style = buttonFont, modifier = modifier.padding(0.dp, 15.dp)
      )
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewRenderedTimer() {
  val timer = Timer(900)
  RacingRegistersCompanionTheme {
    Surface {
      RenderedTimer(timer = timer)
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewRenderedContinueButton() {
  val timer = Timer(900)
  RacingRegistersCompanionTheme {
    Surface {
      RenderBreakContinueButton(timer = timer, Modifier, "CONTINUE")
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewRenderedBreakButton() {
  val timer = Timer(900)
  RacingRegistersCompanionTheme {
    Surface {
      RenderBreakContinueButton(timer = timer, Modifier, "BREAK")
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewRenderBackground() {
  val timer = Timer(900)
  RacingRegistersCompanionTheme {
    Surface {
      RenderBackground(timer)
    }
  }
}
