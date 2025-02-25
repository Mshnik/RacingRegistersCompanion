package com.redpup.racingregisters.companion

import android.content.Context
import android.media.MediaPlayer
import com.redpup.racingregisters.companion.sound.AbstractMediaPlayer
import com.redpup.racingregisters.companion.sound.ForwardingMediaPlayer
import com.redpup.racingregisters.companion.sound.LoopMediaPlayer
import com.redpup.racingregisters.companion.sound.MultiTrackMediaPlayer
import kotlin.math.pow

/**
 * Different tracks in the looped music.
 *
 * Enum order determines order tracks are added to the song.
 */
enum class Track {
  DRUMS_1,
  LEAD,
  BASS,
  DRUMS_2,
  PAD
}

/** Returns a multi track background of all the tracks. */
fun backgroundMusic(context: Context) =
  LoopMediaPlayer(
    MultiTrackMediaPlayer(
      mapOf(
        Track.DRUMS_1 to ForwardingMediaPlayer(context, R.raw.music_drums_1).setVolume(1.0F),
        Track.DRUMS_2 to ForwardingMediaPlayer(context, R.raw.music_drums_2).setVolume(0.9F),
        Track.LEAD to ForwardingMediaPlayer(context, R.raw.music_lead).setVolume(1.0F),
        Track.PAD to ForwardingMediaPlayer(context, R.raw.music_pad).setVolume(0.6F),
        Track.BASS to ForwardingMediaPlayer(context, R.raw.music_bass).setVolume(0.6F)
      )
    )
  )

/** Returns a single track transition to use when returning to the game from break. */
fun transitionMusic(context: Context): ForwardingMediaPlayer {
  return ForwardingMediaPlayer(context, R.raw.music_continue_transition)
}

/** Allows enabling tracks based on enum declaration order. */
fun MultiTrackMediaPlayer<Track, *>.enableNextTrack() {
  for (t in Track.entries) {
    if (!this.isTrackEnabled(t)) {
      this.setTrackEnabled(t, true)
      return
    }
  }
}

/** Allows enabling tracks based on enum declaration order. */
fun LoopMediaPlayer<MultiTrackMediaPlayer<Track, ForwardingMediaPlayer>>.enableNextTrack() {
  this.applyToPlayers { it.enableNextTrack() }
}

/** Scales the transition timer in state to match the duration of transitionMusic. */
fun scaleTransitionTimerToMusic(transitionMusic: ForwardingMediaPlayer, state: MainActivityState) {
  val musicDurationMillis = transitionMusic.duration()
  val timerIntervalDuration = (musicDurationMillis / 4.0).toLong()
  state.transitionTimer.reset()
  state.transitionTimer.setSpeed(timerIntervalDuration, 1)
}

/** Returns the pitch ratio of adding the given number of half steps, in equal temperament. */
fun pitchRatio(halfSteps: Int) = 2.0.pow(halfSteps / 12.0).toFloat()
