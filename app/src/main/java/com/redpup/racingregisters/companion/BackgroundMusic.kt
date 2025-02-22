package com.redpup.racingregisters.companion

import android.content.Context
import android.media.MediaPlayer
import com.redpup.racingregisters.companion.sound.AbstractMediaPlayer
import com.redpup.racingregisters.companion.sound.ForwardingMediaPlayer
import com.redpup.racingregisters.companion.sound.LoopMediaPlayer
import com.redpup.racingregisters.companion.sound.MultiTrackMediaPlayer

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
        Track.DRUMS_1 to ForwardingMediaPlayer(context, R.raw.music_drums_1),
        Track.DRUMS_2 to ForwardingMediaPlayer(context, R.raw.music_drums_2),
        Track.LEAD to ForwardingMediaPlayer(context, R.raw.music_lead),
        Track.PAD to ForwardingMediaPlayer(context, R.raw.music_pad),
        Track.BASS to ForwardingMediaPlayer(context, R.raw.music_bass)
      )
    )
  )

/** Allows enabling tracks based on enum declaration order. */
fun MultiTrackMediaPlayer<Track, ForwardingMediaPlayer>.enableNextTrack() {
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

fun scaleTransitionTimerToMusic(transitionMusic : MediaPlayer, state: MainActivityState) {
  val musicDurationMillis = transitionMusic.duration
  val timerIntervalDuration = (musicDurationMillis / 4.0).toLong()
  state.transitionTimer.setSpeed(timerIntervalDuration, 1)
}