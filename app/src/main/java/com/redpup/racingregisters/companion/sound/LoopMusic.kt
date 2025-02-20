package com.redpup.racingregisters.companion.sound

import android.content.Context
import androidx.annotation.GuardedBy
import com.redpup.racingregisters.companion.R

/** Different tracks in the looped music. */
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
    Track.DRUMS_1 to LoopMediaPlayer(context, R.raw.music_drums),
    Track.DRUMS_2 to LoopMediaPlayer(context, R.raw.music_drums_2),
    Track.LEAD to LoopMediaPlayer(context, R.raw.music_lead),
    Track.PAD to LoopMediaPlayer(context, R.raw.music_pad),
    Track.BASS to LoopMediaPlayer(context, R.raw.music_bass)
  )

  private val trackLock = Object()

  @GuardedBy("trackLock")
  private var trackVolumes: MutableMap<Track, Float> = mutableMapOf()

  var masterVolume: Float = 1.0F; set(value) {
    field = value
    Track.entries.forEach { updateVolume(it) }
  }

  init {
    synchronized(trackLock) {
      Track.entries.forEach { t ->
        trackVolumes[t] = 0.0F
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
      return trackVolumes[track]!! > 0 && tracks[track]!!.isPlaying
    }
  }

  /** Sets the volume of the given track. */
  fun setVolume(track: Track, volume: Float) {
    if (trackVolumes[track] != volume) {
      synchronized(trackLock) {
        if (trackVolumes[track] != volume) {
          trackVolumes[track] = volume
          updateVolume(track)
        }
      }
    }
  }

  /** Updates track volume based on set volume args. */
  private fun updateVolume(track: Track) {
    synchronized(trackLock) {
      tracks[track]!!.setVolume(trackVolumes[track]!! * masterVolume)
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
        print("Track $t: $trackVolumes[t]")
        if (trackVolumes[t] == 0.0f) {
          trackVolumes[t] = 1.0f
          updateVolume(t)
          return
        }
      }
    }
  }
}