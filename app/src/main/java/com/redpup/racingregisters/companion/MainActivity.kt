package com.redpup.racingregisters.companion

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redpup.racingregisters.companion.timer.Timer
import com.redpup.racingregisters.companion.ui.theme.Green90
import com.redpup.racingregisters.companion.ui.theme.RacingRegistersCompanionTheme
import com.redpup.racingregisters.companion.ui.theme.White90
import com.redpup.racingregisters.companion.ui.theme.sixtyFour

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val timer = Timer(baseContext.resources.getInteger(R.integer.default_duration_seconds))

    enableEdgeToEdge()
    setContent {
      RacingRegistersCompanionTheme {
        Scaffold { innerPadding ->
          RenderScreen(timer, Modifier.padding(innerPadding))
        }
      }
    }
  }
}

@Composable
fun RenderScreen(timer: Timer, modifier: Modifier = Modifier) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(10.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    RenderedTimer(
      timer = timer, modifier = modifier.height(100.dp)
    )
    RenderBreakContinueButton(timer)
  }
}

@Composable
fun RenderedTimer(timer: Timer, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .border(5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
      .background(Color.Blue)
  ) {
    Box(
      modifier = modifier
        .background(Color.Red)
    ) {
      val currentTime = remember { mutableStateOf(timer.toString()) }
      val timerFont = TextStyle(
        fontFamily = sixtyFour,
        fontWeight = FontWeight.Bold,
        fontSize = 50.sp,
        lineHeight = 0.sp,
        letterSpacing = 4.sp
      )
      timer.subscribe { currentTime.value = timer.toString() }
      Text(text = currentTime.value, style = timerFont)
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
    textColor = if (text == "BREAK") Color.Black else Green90
    backgroundColor = if (text == "BREAK") White90 else Color.Black
    borderColor = if (text == "BREAK") White90 else Green90
  }
  updateColors()

  val buttonFont = TextStyle(
    fontFamily = sixtyFour,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 0.sp,
    letterSpacing = 2.sp
  )

  Button(
    onClick = {
      timer.toggle()
      text = if (text == "BREAK") "CONTINUE" else "BREAK"
      updateColors()
    },
    border = BorderStroke(
      width = 3.dp,
      color = borderColor
    ),
    colors = ButtonColors(backgroundColor, textColor, backgroundColor, textColor),
    shape = RoundedCornerShape(5.dp)
  ) {
    Text(
      text, style = buttonFont, modifier = modifier
    )
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
      RenderedTimer(timer = timer, Modifier)
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
