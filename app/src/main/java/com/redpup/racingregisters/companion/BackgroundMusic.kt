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

private fun breakMusic(context: Context) =
  LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_1))

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

/** Attaches this player to other, for seamless transition out of break. */
private fun ProgressionMediaPlayer<ForwardingMediaPlayer>.setupLeaveTransition(
  other: ProgressionMediaPlayer<LoopMediaPlayer<ForwardingMediaPlayer>>,
) {
  current().setNextMediaPlayer(other.current().attachAndGetCurrent())
}

/** Wrapper on all music that makes up the background music, including breaks and transitions. */
class BackgroundMusic(context: Context) {
  private val mainMusic = mainMusic(context)
  private val hurryUpMusic = mainMusic(context)
    // 5% faster
    .setSpeed(1.05F)
    // Half a step higher (2^1/12).
    .setPitch(2.0.pow(1.0 / 12.0).toFloat())
  private val breakMusic = breakMusic(context)
  private val transitionMusic = transitionMusic(context)
  private fun all() = listOf(mainMusic, hurryUpMusic, breakMusic, transitionMusic)

  fun prepareAsync(listener: () -> Unit) {
    val all = all()
    val fork = ForkedListener<Unit>(all.size, {}, { listener()  })
    all.forEach { it.prepareAsync { fork.handle(Unit) } }
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

  /** Starts main game music. */
  fun start(state: MainActivityState) {
    state.scaleTransitionTimerToMusic(transitionMusic.duration())
    mainMusic.start()
  }

  /** Starts a break, transitioning to break music. */
  fun startBreak(state : MainActivityState) {
    mainMusic.current().softReset()
    hurryUpMusic.current().softReset()
    breakMusic.start()
    mainMusic.advanceAndCap()
    hurryUpMusic.advanceAndCap()
    transitionMusic.current().softReset()
    if (state.isHurryUp()) {
      transitionMusic.setupLeaveTransition(hurryUpMusic)
    } else {
      transitionMusic.setupLeaveTransition(mainMusic)
    }
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
    }
    transitionMusic.advanceAndCap()
    state.scaleTransitionTimerToMusic(transitionMusic.duration())
  }

  /** Starts the hurry up music. */
  fun startHurryUp() {
    mainMusic.current().softReset()
    hurryUpMusic.start()
  }

  /** Resets the game music back to th  e initial state. */
  fun reset(state: MainActivityState) {
    mainMusic.softReset()
    hurryUpMusic.softReset()
    breakMusic.softReset()
    transitionMusic.softReset()
    state.transitionTimer.setSpeed(1000L, 1)
  }
}
