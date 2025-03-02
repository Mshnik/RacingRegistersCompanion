package com.redpup.racingregisters.companion

import android.content.Context
import android.media.MediaPlayer
import com.redpup.racingregisters.companion.event.ForkedListener
import com.redpup.racingregisters.companion.sound.AbstractMediaPlayer
import com.redpup.racingregisters.companion.sound.ForwardingMediaPlayer
import com.redpup.racingregisters.companion.sound.LoopMediaPlayer
import com.redpup.racingregisters.companion.sound.MultiTrackMediaPlayer
import com.redpup.racingregisters.companion.sound.ProgressionMediaPlayer
import kotlin.math.pow

/** Returns a progression media player of the background music. */
private fun mainMusic(context: Context) =
  ProgressionMediaPlayer(
    listOf(
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_1)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_2)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_3)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_4)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_5)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_6)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_7)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_8)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_9)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_10))
    )
  )

/** Returns a progression media player of transition out of break music. */
private fun transitionMusic(context: Context) =
  ProgressionMediaPlayer(
    listOf(
      ForwardingMediaPlayer(context, R.raw.music_background_2t),
      ForwardingMediaPlayer(context, R.raw.music_background_3t),
      ForwardingMediaPlayer(context, R.raw.music_background_4t),
      ForwardingMediaPlayer(context, R.raw.music_background_5t),
      ForwardingMediaPlayer(context, R.raw.music_background_6t),
      ForwardingMediaPlayer(context, R.raw.music_background_7t),
      ForwardingMediaPlayer(context, R.raw.music_background_8t),
      ForwardingMediaPlayer(context, R.raw.music_background_9t),
      ForwardingMediaPlayer(context, R.raw.music_background_10t)
    )
  )

/** Wrapper on all music that makes up the background music, including breaks and transitions. */
class BackgroundMusic(context: Context) {
  private val mainMusic = mainMusic(context)
  private val breakMusic = LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_1))
  private val transitionMusic = transitionMusic(context)

  private fun all() = listOf(mainMusic, breakMusic, transitionMusic)

  fun prepareAsync(listener: () -> Unit) {
    val fork = ForkedListener<Unit>(3, {}, { listener() })
    all().forEach { it.prepareAsync { fork.handle(Unit) } }
  }

  fun setVolume(volume: Float) {
    all().forEach { it.setVolume(volume) }
  }

  /** Exits this music, cleaning up all resources. */
  fun exit() {
    all().forEach {
      it.reset()
      it.release()
    }
  }

  /** Starts a break, transitioning to break music. */
  fun startBreak() {
    mainMusic.pause()
    breakMusic.start()
    mainMusic.advanceAndCap()
    transitionMusic.softReset()
  }

  /** Begins a transition back to main game music. */
  fun startTransitionIn() {
    breakMusic.pause()
    transitionMusic.start()
    breakMusic.softReset()
  }

  /** Continues with main game music. */
  fun startContinue(state: MainActivityState) {
    if (transitionMusic.isPlaying()) {
      transitionMusic.pause()
      transitionMusic.advanceAndCap()
      scaleTransitionTimerToMusic(state)
    }
    mainMusic.start()
  }

  /** Scales the transition timer in state to match the duration of transitionMusic. */
  private fun scaleTransitionTimerToMusic(state: MainActivityState) {
    val musicDurationMillis = transitionMusic.duration()
    val timerIntervalDuration = (musicDurationMillis / 4.0).toLong()
    state.transitionTimer.reset()
    state.transitionTimer.setSpeed(timerIntervalDuration, 1)
  }
}
