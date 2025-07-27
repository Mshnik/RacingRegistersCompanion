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
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_100bpm_c)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_105bpm_c)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_110bpm_c)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_115bpm_c)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_120bpm_c)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_125bpm_c)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_130bpm_c)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_135bpm_c)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_140bpm_c)),
      LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_145bpm_c))
    )
  )

private fun breakMusic(context: Context) =
  LoopMediaPlayer(ForwardingMediaPlayer(context, R.raw.music_background_100bpm_c))

/** Returns a progression media player of transition out of break music. */
private fun transitionMusic(context: Context) =
  ProgressionMediaPlayer(
    listOf(
      ForwardingMediaPlayer(context, R.raw.music_background_transition_100bpm_c_105bpm_c),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_100bpm_c_110bpm_c),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_100bpm_c_115bpm_c),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_100bpm_c_120bpm_c),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_100bpm_c_125bpm_c),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_100bpm_c_130bpm_c),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_100bpm_c_135bpm_c),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_100bpm_c_140bpm_c),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_100bpm_c_145bpm_c),
    )
  )


/** Returns a progression media player of transition to hurry up music. */
private fun transitionToHurryUpMusic(context: Context) =
  ProgressionMediaPlayer(
    listOf(
      ForwardingMediaPlayer(context, R.raw.music_background_transition_key_100bpm_c_105bpm_cs),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_key_105bpm_c_110bpm_cs),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_key_110bpm_c_115bpm_cs),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_key_115bpm_c_120bpm_cs),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_key_120bpm_c_125bpm_cs),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_key_125bpm_c_130bpm_cs),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_key_130bpm_c_135bpm_cs),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_key_135bpm_c_140bpm_cs),
      ForwardingMediaPlayer(context, R.raw.music_background_transition_key_140bpm_c_145bpm_cs),
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
    // Half a step higher (2^1/12).
    .setPitch(2.0.pow(1.0 / 12.0).toFloat())
    // Skip the first track.
    .advanceAndCap()
  private val breakMusic = breakMusic(context)
  // TODO - Have to also have transition back in music in C# post hurry up.
  private val transitionMusic = transitionMusic(context)
  private val transitionToHurryUpMusic = transitionToHurryUpMusic(context)
  private fun all() =
    listOf(mainMusic, hurryUpMusic, breakMusic, transitionMusic, transitionToHurryUpMusic)

  fun prepareAsync(listener: () -> Unit) {
    val all = all()
    val fork = ForkedListener<Unit>(all.size, {}, { listener() })
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
  fun startBreak(state: MainActivityState) {
    mainMusic.current().softReset()
    hurryUpMusic.current().softReset()
    transitionToHurryUpMusic.current().softReset()

    breakMusic.start()

    mainMusic.advanceAndCap()
    hurryUpMusic.advanceAndCap()
    transitionToHurryUpMusic.advanceAndCap()

    transitionMusic.current().softReset()
    // if (state.isHurryUp()) {
    //   transitionMusic.setupLeaveTransition(hurryUpMusic)
    // } else {
    //   transitionMusic.setupLeaveTransition(mainMusic)
    // }
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
    transitionToHurryUpMusic.setupLeaveTransition(hurryUpMusic)
    transitionToHurryUpMusic.start()
  }

  /** Resets the game music back to th  e initial state. */
  fun reset(state: MainActivityState) {
    mainMusic.softReset()
    hurryUpMusic.softReset()
    breakMusic.softReset()
    transitionMusic.softReset()
    transitionToHurryUpMusic.softReset()
    state.transitionTimer.setSpeed(1000, 1)
  }
}
