package com.redpup.racingregisters.companion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.redpup.racingregisters.companion.timer.Event
import com.redpup.racingregisters.companion.timer.TimerViewModel
import com.redpup.racingregisters.companion.ui.BackgroundViewModel
import com.redpup.racingregisters.companion.ui.RenderBackground
import com.redpup.racingregisters.companion.ui.RenderPrimaryButton
import com.redpup.racingregisters.companion.ui.RenderSecondaryButton
import com.redpup.racingregisters.companion.ui.theme.Green90
import com.redpup.racingregisters.companion.ui.theme.Grey50
import com.redpup.racingregisters.companion.ui.theme.Grey90
import com.redpup.racingregisters.companion.ui.theme.Red90
import com.redpup.racingregisters.companion.ui.theme.White90
import com.redpup.racingregisters.companion.ui.theme.mPlus1Code
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** State of the main action button on the main activity. */
enum class MainButtonState {
  START,
  BREAK,
  CONTINUE;
}

/** Possible states of the timer. */
enum class RunState {
  PAUSED,
  RUNNING,
  HURRY_UP,
  COMPLETE
}

/** Wrapper on mutable state visually displayed in this screen.*/
class GameState(
  val timer: TimerViewModel,
  val hurryUp: Int,
  val transitionTimer: TimerViewModel,
  val music: BackgroundMusic,
  val soundEffects: SoundEffects,
  val coroutineScope: CoroutineScope,
  numBackgroundBars: Int,
  val backgroundViewModel: BackgroundViewModel = BackgroundViewModel(numBackgroundBars, timer),
) : ViewModel() {
  /** Whether the top reset button is currently enabled. */
  val resetButtonEnabled = MutableStateFlow(true)

  /** Text to show on the timer, based on which timer is running. */
  val timerText: Flow<String> =
    combine(
      transitionTimer.isRunning,
      timer.formattedTime,
      transitionTimer.formattedTime
    ) { transitionRunning, timer, transition ->
      if (transitionRunning) transition else timer
    }

  /**
   * Whether everything is loaded and ready to go.
   * This should start false and be set to true after all loading is done.
   */
  val isReady = MutableStateFlow(true)

  /** The current run state. */
  val isRunning = MutableStateFlow(false)

  /** Whether this is currently in hurry up state. */
  val isHurryUp = timer.remainingIncrements.map { it <= hurryUp }

  /** The current button state. */
  val buttonState = MutableStateFlow(MainButtonState.START)

  /** Whether the main button is enabled. This should be true unless we are in a transition. */
  val buttonEnabled = MutableStateFlow(true)

  /** The current run state of the timer. */
  val runState = combine(isRunning, isHurryUp)
  { running, hurryUp ->
    if (!running) RunState.PAUSED
    else if (hurryUp) RunState.HURRY_UP
    else RunState.RUNNING
  }

  /** Called when the main button is clicked. */
  suspend fun clickButton() {
    when (buttonState.value) {
      MainButtonState.START -> {
        soundEffects.beginEffect.start()
        transition { executeStart() }
      }

      MainButtonState.BREAK -> executeBreak()

      MainButtonState.CONTINUE -> {
        music.startTransitionIn()
        transition { executeContinue() }
      }
    }
  }

  /** Begins a transition, executing execute at the end of the transition. */
  private suspend fun transition(execute: suspend () -> Unit) {
    buttonEnabled.value = false
    transitionTimer.reset()
    coroutineScope.launch {
      transitionTimer.eventBus.subscribe(
        Event.FINISH,
        limit = 1
      ) {
        execute()
      }
    }
    transitionTimer.start()
    isRunning.value = true
  }

  /** Executes the start action, after transition. */
  private suspend fun executeStart() {
    music.start(this)
    timer.start()
    resetButtonEnabled.value = true
    buttonEnabled.value = true
    isRunning.value = true
    buttonState.value = MainButtonState.BREAK
  }

  /** Executes the break action, (maybe) after transition. */
  private suspend fun executeBreak() {
    soundEffects.breakEffect.start()
    music.startBreak(this)
    timer.pause()
    backgroundViewModel.accumulate()
    buttonEnabled.value = true
    isRunning.value = false
    buttonState.value = MainButtonState.CONTINUE
  }

  /** Executes the continue action, after transition. */
  private suspend fun executeContinue() {
    music.startContinue(this)
    timer.start()
    buttonEnabled.value = true
    isRunning.value = true
    buttonState.value = MainButtonState.BREAK
  }

  /** Invoked when the "Reset" button is pushed. */
  fun reset() {
    scaleTransitionTimerToMusic(soundEffects.beginEffect.duration)
    music.reset(this)
    timer.reset()
    transitionTimer.reset()
    buttonState.value = MainButtonState.START
    buttonEnabled.value = true
    resetButtonEnabled.value = false
    isRunning.value = false
  }

  /** Sets up music with event handling. */
  fun setupMusic(masterVolume: Float) {
    music.setVolume(masterVolume)
    music.prepareAsync { isReady.value = true }

    coroutineScope.launch {
      isHurryUp
        .filter { it }
        .collect {
          music.startHurryUp()
        }
    }
  }

  /** Sets up sound with event handling. */
  fun setupSound() {
    scaleTransitionTimerToMusic(soundEffects.beginEffect.duration)
    coroutineScope.launch {
      transitionTimer.isComplete
        .filter { it }
        .collect {
          soundEffects.resumeEffect.start()
        }
    }

    coroutineScope.launch {
      timer.remainingIncrements
        .filter { soundEffects.countdownEffects.containsKey(it) }
        .collect {
          soundEffects.countdownEffects[it]!!.start()
        }
    }
  }

  /**
   * Scales the transition timer in state to match the given duration in millis,
   * without changing the number of increments in the timer.
   */
  fun scaleTransitionTimerToMusic(durationMillis: Int) {
    transitionTimer.reset()
    transitionTimer.scaleSpeed(durationMillis)
  }
}

@Composable
fun GameScreen(
  state: GameState,
  navController: NavController,
  lifecycleScope: CoroutineScope,
) {
  Scaffold(topBar = { RenderGameTopBar(state, navController) }) { innerPadding ->
    RenderBackground(state.backgroundViewModel)
    RenderScreen(state, lifecycleScope, Modifier.padding(innerPadding))
  }
}

@Composable
fun RenderGameTopBar(
  state: GameState,
  navController: NavController,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .offset(0.dp, 40.dp)
      .padding(20.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    RenderSecondaryButton(
      R.drawable.back,
      "Back Button"
    ) {
      state.reset()
      navController.navigateUp()
    }
    RenderSecondaryButton(
      R.drawable.reset,
      "Reset Button",
      enabled = state.resetButtonEnabled.collectAsState()
    ) {
      state.reset()
    }
  }
}

@Composable
fun RenderScreen(state: GameState, coroutineScope: CoroutineScope, modifier: Modifier) {
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
fun RenderTimer(state: GameState) {
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
  state: GameState,
  coroutineScope: CoroutineScope,
  modifier: Modifier = Modifier,
  initialState: MainButtonState = MainButtonState.START,
) {
  val buttonText = state.buttonState.map { it.name }.collectAsState(initialState.name)
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

  RenderPrimaryButton(
    buttonText,
    enabled = buttonClickable,
    textColor = textColor,
    backgroundColor = backgroundColor,
    borderColor = borderColor
  ) {
    coroutineScope.launch {
      state.clickButton()
    }
  }
}
