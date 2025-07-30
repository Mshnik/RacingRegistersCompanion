package com.redpup.racingregisters.companion

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.os.Bundle
import android.view.WindowManager
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redpup.racingregisters.companion.timer.TimerViewModel
import com.redpup.racingregisters.companion.ui.theme.DarkRed90
import com.redpup.racingregisters.companion.ui.theme.Green90
import com.redpup.racingregisters.companion.ui.theme.Grey50
import com.redpup.racingregisters.companion.ui.theme.Grey90
import com.redpup.racingregisters.companion.ui.theme.RacingRegistersCompanionTheme
import com.redpup.racingregisters.companion.ui.theme.Red90
import com.redpup.racingregisters.companion.ui.theme.White90
import com.redpup.racingregisters.companion.ui.theme.mPlus1Code
import com.redpup.racingregisters.companion.ui.theme.sixtyFour
import kotlin.math.hypot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    val timerDuration = baseContext.resources.getInteger(R.integer.timer_duration_seconds)
    val hurryUpTime = baseContext.resources.getInteger(R.integer.timer_hurry_up_seconds)
    val transitionDuration = baseContext.resources.getInteger(R.integer.transition_duration_seconds)

    val state = MainActivityState(
      TimerViewModel(timerDuration),
      hurryUpTime,
      TimerViewModel(transitionDuration, completeAtIncrements = 1, completionMessage = "GO!"),
      music = BackgroundMusic(baseContext),
      soundEffects = SoundEffects(baseContext),
      coroutineScope = lifecycleScope
    )
    state.setupMusic(baseContext.resources.getFloat(R.dimen.music_volume_master))
    state.setupSound()

    val numBackgroundBars = baseContext.resources.getInteger(R.integer.num_background_bars)
    enableEdgeToEdge()
    setContent {
      RacingRegistersCompanionTheme {
        Scaffold(topBar = { RenderTopBar(state) }) { innerPadding ->
          RenderBackground(state, numBackgroundBars)
          RenderScreen(state, lifecycleScope, Modifier.padding(innerPadding))
        }
      }
    }

  }
}

@Composable
fun RenderTopBar(state: MainActivityState) {
  val size = 50.dp
  val enabled = state.resetButtonEnabled.collectAsState()

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .offset(0.dp, 40.dp)
      .padding(20.dp),
    horizontalArrangement = Arrangement.End
  ) {
    Button(
      onClick = { state.reset() },
      enabled = enabled.value,
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
        colorFilter = if (enabled.value) ColorFilter.tint(White90) else ColorFilter.tint(Grey50)
      )
    }
  }
}

@Composable
fun RenderScreen(state: MainActivityState, coroutineScope: CoroutineScope, modifier: Modifier) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(10.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    RenderTimer(
      state = state
    )
    Spacer(Modifier.height(40.dp))
    RenderBreakContinueButton(state, coroutineScope)
  }
}

@Composable
fun RenderBackground(state: MainActivityState, numBars: Int) {
  val numBarsTimes2 = numBars * 2
  val hurryUpBarColor = state.isHurryUp.map { if (it) DarkRed90 else Grey90 }.collectAsState(Grey90)
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
          color = if (i % 2 == 0) hurryUpBarColor.value else Grey90,
          topLeft = Offset(x = xOffset, y = -halfW),
          size = Size(barWidth, h + w)
        )
      }
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RenderTimer(state: MainActivityState) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val renderedTime = state.timerText.collectAsState("")
    val timeColor = state.runState.map {
      when (it) {
        RunState.PAUSED -> Grey50
        RunState.RUNNING -> Green90
        RunState.HURRY_UP -> Red90
        RunState.COMPLETE -> Green90
      }
    }.collectAsState(Grey50)

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
        text = renderedTime.value,
        modifier = Modifier
          .semantics { invisibleToUser() }
          .padding(5.dp),
        style = timerFont.copy(drawStyle = Stroke(width = 30F), color = Color.Black)
      )
      Text(
        text = renderedTime.value,
        modifier = Modifier.padding(5.dp),
        style = timerFont,
        color = timeColor.value
      )
    }
  }
}

@Composable
fun RenderBreakContinueButton(
  state: MainActivityState,
  coroutineScope: CoroutineScope,
  modifier: Modifier = Modifier,
  initialState: MainButtonState = MainButtonState.START,
) {
  val buttonState = state.buttonState.collectAsState(initialState)
  val buttonClickableFlow =
    state.isReady.combine(state.buttonEnabled) { ready, enabled -> ready && enabled }
  val buttonClickable = buttonClickableFlow.collectAsState(false)

  val isBreakFlow = state.buttonState.map { it == MainButtonState.BREAK }

  val textColor = isBreakFlow.combine(buttonClickableFlow)
  { isBreak, isClickable ->
    if (isBreak && isClickable) Color.Black
    else if (isBreak) Grey90
    else if (isClickable) Green90
    else Grey50
  }.collectAsState(Color.Black)

  val backgroundColor = isBreakFlow.combine(buttonClickableFlow)
  { isBreak, isClickable ->
    if (isBreak && isClickable) White90
    else if (isBreak) Grey50
    else if (isClickable) Color.Black
    else Grey90
  }.collectAsState(White90)

  val borderColor = isBreakFlow.combine(buttonClickableFlow)
  { isBreak, isClickable ->
    if (isBreak && isClickable) White90
    else if (isBreak) Grey50
    else if (isClickable) Green90
    else Grey50
  }.collectAsState(White90)

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
      onClick = {
        coroutineScope.launch {
          state.clickButton()
        }
      },
      enabled = buttonClickable.value,
      border = BorderStroke(
        width = borderThickness, color = borderColor.value
      ),
      colors = ButtonColors(backgroundColor.value, textColor.value, Grey90, textColor.value),
      shape = RoundedCornerShape(borderThickness),
      modifier = modifier.padding(borderThickness)
    ) {
      Text(
        buttonState.value.name, style = buttonFont, modifier = modifier.padding(0.dp, 15.dp)
      )
    }
  }
}
