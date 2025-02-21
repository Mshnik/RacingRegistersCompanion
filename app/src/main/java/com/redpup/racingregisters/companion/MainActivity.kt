package com.redpup.racingregisters.companion

import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redpup.racingregisters.companion.Event as StateEvent
import com.redpup.racingregisters.companion.sound.LoopMediaPlayer
import com.redpup.racingregisters.companion.sound.LoopMusic
import com.redpup.racingregisters.companion.timer.Event as TimerEvent
import com.redpup.racingregisters.companion.timer.Timer
import com.redpup.racingregisters.companion.ui.theme.Green90
import com.redpup.racingregisters.companion.ui.theme.Grey50
import com.redpup.racingregisters.companion.ui.theme.Grey90
import com.redpup.racingregisters.companion.ui.theme.RacingRegistersCompanionTheme
import com.redpup.racingregisters.companion.ui.theme.White90
import com.redpup.racingregisters.companion.ui.theme.mPlus1Code
import com.redpup.racingregisters.companion.ui.theme.sixtyFour
import kotlin.math.hypot

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val timerDuration = baseContext.resources.getInteger(R.integer.timer_duration_seconds)
    val transitionDuration = baseContext.resources.getInteger(R.integer.transition_duration_seconds)
    val state = MainActivityState(
      Timer(timerDuration),
      Timer(transitionDuration, completeAtIncrements = 1, completionMessage = "GO!")
    )

    setupMusic(baseContext, state)
    setupSound(baseContext, state)

    val numBackgroundBars = baseContext.resources.getInteger(R.integer.num_background_bars)

    enableEdgeToEdge()
    setContent {
      RacingRegistersCompanionTheme {
        Scaffold(topBar = { RenderTopBar(state) }) { innerPadding ->
          RenderBackground(state, numBackgroundBars)
          RenderScreen(state, Modifier.padding(innerPadding))
        }
      }
    }
  }

  private fun setupMusic(context: Context, state: MainActivityState) {
    val music = LoopMusic(context)
    music.masterVolume = context.resources.getFloat(R.dimen.music_volume_master)
    music.setAutoAdvanceSpeedIncrement(0.05f)

    state.eventHandler.subscribe(StateEvent.START, StateEvent.CONTINUE) {
      music.musicActive = true
      music.enableNextTrack()
    }
    state.eventHandler.subscribe(StateEvent.BREAK) {
      music.musicActive = false
    }

    music.start()
  }

  private fun setupSound(context: Context, state: MainActivityState) {
    val soundEffectStart = MediaPlayer.create(context, R.raw.effect_start)
    val soundEffectBreak = MediaPlayer.create(context, R.raw.effect_break)

    state.eventHandler.subscribe(StateEvent.START, StateEvent.CONTINUE) {
      soundEffectStart.start()
    }
    state.eventHandler.subscribe(StateEvent.BREAK) {
      soundEffectBreak.start()
    }
  }
}

@Composable
fun RenderTopBar(state: MainActivityState) {
  val size = 50.dp
  var enabled by remember { mutableStateOf(false) }

  state.eventHandler.subscribe(StateEvent.RESET) { enabled = false }
  state.eventHandler.subscribe(StateEvent.START) { enabled = true }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .offset(0.dp, 40.dp)
      .padding(20.dp),
    horizontalArrangement = Arrangement.End
  ) {
    Button(
      onClick = { state.reset() },
      enabled = enabled,
      colors = ButtonColors(
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black
      ),
      shape = CircleShape,
      border = BorderStroke(width = 3.dp, color = Grey90),
      modifier = Modifier.size(size),
      contentPadding = PaddingValues(size * 0.2F)
    ) {
      Image(
        painter = painterResource(R.drawable.reset),
        contentDescription = "Reset icon",
        colorFilter = if (enabled) ColorFilter.tint(White90) else ColorFilter.tint(Grey50)
      )
    }
  }
}

@Composable
fun RenderScreen(state: MainActivityState, modifier: Modifier) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(10.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    RenderedTimer(
      state = state
    )
    Spacer(Modifier.height(40.dp))
    RenderBreakContinueButton(state)
  }
}

@Composable
fun RenderBackground(state: MainActivityState, numBars: Int) {
  val numBarsTimes2 = numBars * 2
  var shift by remember { mutableFloatStateOf(0.0F) }
  var shiftFactor by remember { mutableFloatStateOf(0.0F) }
  var previousShift by remember { mutableFloatStateOf(0.0F) }
  var previousTotal by remember { mutableFloatStateOf(0.0F) }

  state.timer.eventHandler.subscribe(TimerEvent.TICK) {
    shift = state.timer.elapsedMillis() / 1000F
  }
  state.eventHandler.subscribe(StateEvent.START, StateEvent.CONTINUE) {
    shiftFactor = state.timer.numResumes.toFloat()
  }
  state.eventHandler.subscribe(StateEvent.BREAK) {
    previousTotal += (shift - previousShift) * shiftFactor
    previousShift = shift
  }
  state.eventHandler.subscribe(StateEvent.RESET) {
    shift = 0.0F
    shiftFactor = 0.0F
    previousShift = 0.0F
  }

  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val halfW = w * 0.5F
    val threeQuartersW = w * 0.75F
    val h = size.height
    val hypotenuse = hypot(h, w)
    rotate(degrees = -45F) {
      val barWidth = hypotenuse / numBarsTimes2
      val xShift = previousTotal + (shift - previousShift) * shiftFactor
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RenderedTimer(state: MainActivityState) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    var renderedTime by remember { mutableStateOf(state.timer.toString()) }
    var timeColor by remember { mutableStateOf(Grey50) }

    state.timer.eventHandler.subscribe(TimerEvent.SECOND) { renderedTime = state.timer.toString() }
    state.transitionTimer.eventHandler.subscribe(TimerEvent.SECOND) {
      renderedTime = state.transitionTimer.toString()
    }
    state.eventHandler.subscribe(StateEvent.RESET) { renderedTime = state.timer.toString() }
    state.eventHandler.subscribe(
      StateEvent.TRANSITION_TO_START,
      StateEvent.TRANSITION_TO_CONTINUE
    ) {
      timeColor = Green90
      renderedTime = state.transitionTimer.toString()
    }
    state.eventHandler.subscribe(StateEvent.START, StateEvent.CONTINUE) {
      timeColor = White90
      renderedTime = state.timer.toString()
    }
    state.eventHandler.subscribe(StateEvent.BREAK) {
      timeColor = Grey50
    }

    val timerFont = TextStyle(
      fontFamily = mPlus1Code,
      fontWeight = FontWeight.Bold,
      fontSize = 100.sp,
      lineHeight = 0.sp,
      letterSpacing = 4.sp,
      shadow = Shadow(color = Color.Black, offset = Offset(10f, 10f), blurRadius = 10f)
    )

    Box {
      Text(
        text = renderedTime,
        modifier = Modifier
          .semantics { invisibleToUser() }
          .padding(5.dp),
        style = timerFont.copy(drawStyle = Stroke(width = 30F), color = Color.Black)
      )
      Text(
        text = renderedTime,
        modifier = Modifier.padding(5.dp),
        style = timerFont,
        color = timeColor
      )
    }
  }
}

@Composable
fun RenderBreakContinueButton(
  state: MainActivityState,
  modifier: Modifier = Modifier,
  initialState: MainButtonState = MainButtonState.START,
) {
  var buttonState by remember { mutableStateOf(initialState) }
  var textColor by remember { mutableStateOf(Color.Black) }
  var backgroundColor by remember { mutableStateOf(Color.Black) }
  var borderColor by remember { mutableStateOf(Color.Black) }
  var buttonEnabled by remember { mutableStateOf(true) }

  fun updateColors() {
    val isBreak = buttonState == MainButtonState.BREAK
    textColor = if (isBreak) Color.Black else Green90
    backgroundColor = if (isBreak) White90 else Color.Black
    borderColor = if (isBreak) White90 else Green90
  }
  updateColors()

  state.eventHandler.subscribe(StateEvent.TRANSITION_TO_START, StateEvent.TRANSITION_TO_CONTINUE) {
    buttonEnabled = false
  }
  state.eventHandler.subscribe(StateEvent.START, StateEvent.BREAK, StateEvent.CONTINUE) {
    buttonState = buttonState.toggle()
    updateColors()
    buttonEnabled = true
  }
  state.eventHandler.subscribe(StateEvent.RESET) {
    buttonState = initialState
    updateColors()
    buttonEnabled = true
  }

  val buttonFont = TextStyle(
    fontFamily = sixtyFour,
    fontWeight = FontWeight.Bold,
    fontSize = 23.sp,
    lineHeight = 0.sp,
    letterSpacing = 2.sp,
    shadow = Shadow(color = Grey50, offset = Offset(6F, 6F), blurRadius = 0f)
  )

  val borderThickness = 3.dp

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(borderThickness * 2))
      .background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    Button(
      onClick = { state.action(buttonState) },
      enabled = buttonEnabled,
      border = BorderStroke(
        width = borderThickness, color = borderColor
      ),
      colors = ButtonColors(backgroundColor, textColor, backgroundColor, textColor),
      shape = RoundedCornerShape(borderThickness),
      modifier = modifier.padding(borderThickness)
    ) {
      Text(
        buttonState.name, style = buttonFont, modifier = modifier.padding(0.dp, 15.dp)
      )
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewRenderTopBar() {
  val state = MainActivityState(Timer(900), Timer(3))
  RacingRegistersCompanionTheme {
    Surface {
      RenderTopBar(state)
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewRenderedTimer() {
  val state = MainActivityState(Timer(900), Timer(3))
  RacingRegistersCompanionTheme {
    Surface {
      RenderedTimer(state = state)
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewRenderedStartButton() {
  val state = MainActivityState(Timer(900), Timer(3))
  RacingRegistersCompanionTheme {
    Surface {
      RenderBreakContinueButton(state = state, Modifier, MainButtonState.START)
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewRenderedContinueButton() {
  val state = MainActivityState(Timer(900), Timer(3))
  RacingRegistersCompanionTheme {
    Surface {
      RenderBreakContinueButton(state = state, Modifier, MainButtonState.CONTINUE)
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewRenderedBreakButton() {
  val state = MainActivityState(Timer(900), Timer(3))
  RacingRegistersCompanionTheme {
    Surface {
      RenderBreakContinueButton(state = state, Modifier, MainButtonState.BREAK)
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewRenderBackground() {
  val state = MainActivityState(Timer(900), Timer(3))
  RacingRegistersCompanionTheme {
    Surface {
      RenderBackground(state, 20)
    }
  }
}
