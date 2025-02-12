package com.redpup.racingregisters.companion

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redpup.racingregisters.companion.timer.Timer
import com.redpup.racingregisters.companion.ui.theme.RacingRegistersCompanionTheme
import com.redpup.racingregisters.companion.ui.theme.sixtyFour

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val timer = Timer(baseContext.resources.getInteger(R.integer.default_duration_seconds))

    enableEdgeToEdge()
    setContent {
      RacingRegistersCompanionTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Column {
            Spacer(Modifier.height(20.dp))
            RenderedTimer(
              timer = timer, modifier = Modifier.padding(innerPadding)
            )
            Spacer(Modifier.height(20.dp))
            BreakContinueButton(timer)
          }
        }
      }
    }
  }
}

@Composable
fun RenderedTimer(timer: Timer, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .padding(all = 12.dp)
      .border(1.5.dp, MaterialTheme.colorScheme.primary)
  ) {
    Box(
      modifier = modifier.padding(all = 12.dp)
    ) {
      val currentTime = remember { mutableStateOf(timer.toString()) }
      val timerFont = TextStyle(
        fontFamily = sixtyFour,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
      )
      timer.subscribe { currentTime.value = timer.toString() }
      Text(text = currentTime.value, style = timerFont)
    }
  }
}

@Composable
fun BreakContinueButton(
  timer: Timer,
  modifier: Modifier = Modifier,
  initialText: String = "CONTINUE",
) {
  var buttonText by remember { mutableStateOf(initialText) }
  Button(
    onClick = {
      timer.toggle()
      buttonText = if (buttonText == "BREAK") "CONTINUE" else "BREAK"
    },
    modifier = modifier,
    shape = RoundedCornerShape(5.dp)
  ) {
    Text(
      buttonText,
      style = MaterialTheme.typography.labelLarge
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
      RenderedTimer(timer = timer)
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewContinueButton() {
  val timer = Timer(900)
  RacingRegistersCompanionTheme {
    Surface {
      BreakContinueButton(timer = timer, Modifier, "CONTINUE")
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewBreakButton() {
  val timer = Timer(900)
  RacingRegistersCompanionTheme {
    Surface {
      BreakContinueButton(timer = timer, Modifier, "BREAK")
    }
  }
}
