package com.redpup.racingregisters.companion.sound

import android.content.Context
import androidx.annotation.GuardedBy
import com.redpup.racingregisters.companion.R

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

/**
 * A combined music that can be looped and tracked.
 */
class LoopMusic(context: Context) {
  private val tracks = mapOf(
    Track.DRUMS_1 to LoopMediaPlayer(context, R.raw.music_drums_1),
    Track.DRUMS_2 to LoopMediaPlayer(context, R.raw.music_drums_2),
    Track.LEAD to LoopMediaPlayer(context, R.raw.music_lead),
    Track.PAD to LoopMediaPlayer(context, R.raw.music_pad),
    Track.BASS to LoopMediaPlayer(context, R.raw.music_bass)
  )

  private val trackVolumes = mapOf(
    Track.DRUMS_1 to context.resources.getFloat(R.dimen.music_volume_drums_1),
    Track.DRUMS_2 to context.resources.getFloat(R.dimen.music_volume_drums_2),
    Track.LEAD to context.resources.getFloat(R.dimen.music_volume_lead),
    Track.PAD to context.resources.getFloat(R.dimen.music_volume_pad),
    Track.BASS to context.resources.getFloat(R.dimen.music_volume_bass)
  )

  private val trackLock = Object()

  @GuardedBy("trackLock")
  private var trackEnabled: MutableSet<Track> = mutableSetOf()

  var musicActive = false; set(value) {
    field = value
    Track.entries.forEach { updateVolume(it) }
  }

  var masterVolume: Float = 1.0F; set(value) {
    field = value
    Track.entries.forEach { updateVolume(it) }
  }

  init {
    synchronized(trackLock) {
      Track.entries.forEach { t ->
        tracks[t]!!.setVolume(0.0F)
      }
    }
  }

  /** Starts all tracks. Tracks at 0 volume will be inaudible. */
  fun start() {
    synchronized(trackLock) {
      tracks.values.forEach { it.start() }
    }
  }

  /** Pauses all tracks. */
  fun pause() {
    synchronized(trackLock) {
      tracks.values.forEach { it.pause() }
    }
  }

  /** Returns true iff the given track is currently playing.  */
  fun isPlaying(track: Track): Boolean {
    synchronized(trackLock) {
      return trackEnabled.contains(track) && tracks[track]!!.isPlaying
    }
  }

  /** Updates track volume based on set volume args. */
  private fun updateVolume(track: Track) {
    synchronized(trackLock) {
      // TODO: Extract Drums1 being the background music from here to something more generic.
      if ((musicActive || track == Track.DRUMS_1) && trackEnabled.contains(track)) {
        tracks[track]!!.setVolume(trackVolumes[track]!! * masterVolume)
      } else {
        tracks[track]!!.setVolume(0.0F)
      }
    }
  }

  /** Sets the speed increment to apply on auto advance on all tracks. */
  fun setAutoAdvanceSpeedIncrement(speedIncrement: Float) {
    synchronized(trackLock) {
      Track.entries.forEach { tracks[it]!!.setAutoAdvanceSpeedIncrement(speedIncrement) }
    }
  }

  /** Enables the next track in the sequence. */
  fun enableNextTrack() {
    synchronized(trackLock) {
      for (t in Track.entries) {
        if (!trackEnabled.contains(t)) {
          trackEnabled.add(t)
          updateVolume(t)
          return
        }
      }
    }
  }
}