package com.redpup.racingregisters.companion.sound

import android.media.MediaPlayer

/**
 * An AbstractMediaPlayer that wraps many MediaPlayers and operates on them in sync.
 */
data class MultiTrackMediaPlayer<K, T : AbstractMediaPlayer<T>>(val mediaPlayers: Map<K, T>) :
  AbstractMediaPlayer<MultiTrackMediaPlayer<K, T>> {

  private var isMuted = false
  private var masterVolume: Float = 1.0F

  private val tracksLock = Object()
  private val enabledTracks = mutableSetOf<K>()

  override fun copy(): MultiTrackMediaPlayer<K, T> {
    val player = MultiTrackMediaPlayer(mediaPlayers.mapValues { it.value.copy() })
    player.setIsMuted(isMuted)
    // Master volume is already applied by copying the volume of the underlying tracks.
    // Applying it again here would exponentially decay the overall volume over many copies.
    player.masterVolume = masterVolume
    enabledTracks.forEach { player.setTrackEnabled(it, true) }
    return player
  }

  override fun start() {
    mediaPlayers.entries.forEach {
      updateVolume(it.key)
      it.value.start()
    }
  }

  override fun pause() {
    mediaPlayers.values.forEach { it.pause() }
  }

  override fun stop() {
    mediaPlayers.values.forEach { it.stop() }
  }

  override fun reset() {
    mediaPlayers.values.forEach { it.reset() }
  }

  override fun isPlaying(): Boolean {
    return mediaPlayers.values.first().isPlaying()
  }

  override fun seekToStart() {
    mediaPlayers.values.forEach { it.seekToStart() }
  }

  override fun duration(): Int {
    // Assumes all input players have the same duration.
    return mediaPlayers.values.first().duration()
  }

  override fun setIsMuted(isMuted: Boolean) {
    this.isMuted = isMuted
    mediaPlayers.keys.forEach { updateVolume(it) }
  }

  /** Updates track volume based on set volume args. */
  private fun updateVolume(track: K) {
    synchronized(tracksLock) {
      mediaPlayers[track]!!.setIsMuted(isMuted || !enabledTracks.contains(track))
    }
  }

  override fun setVolume(volume: Float) {
    if (volume != masterVolume) {
      val ratioDelta = volume / masterVolume
      mediaPlayers.values.forEach { it.multiplyVolume(ratioDelta) }
      masterVolume = volume
    }
  }

  override fun multiplyVolume(ratio: Float) {
    setVolume(masterVolume * ratio)
  }

  override fun setNextMediaPlayer(nextPlayer: MultiTrackMediaPlayer<K, T>) {
    check(mediaPlayers.keys == nextPlayer.mediaPlayers.keys)
    for (key in mediaPlayers.keys) {
      mediaPlayers[key]!!.setNextMediaPlayer(nextPlayer.mediaPlayers[key]!!)
    }
  }

  override fun setOnCompletionListener(listener: (MediaPlayer) -> Unit) {
    var first = true
    for (player in mediaPlayers.values) {
      if (first) {
        // Only set on first listener, to avoid exploding listener invocations when this ends.
        player.setOnCompletionListener(listener)
        first = false
      } else {
        player.setOnCompletionListener {
          it.reset()
          it.release()
        }
      }
    }
  }

  /** Returns the number of enabled tracks. */
  fun numTracksEnabled(): Int {
    synchronized(tracksLock) {
      return enabledTracks.size
    }
  }

  /** Returns whether the given track is enabled. */
  fun isTrackEnabled(track: K): Boolean {
    synchronized(tracksLock) {
      return enabledTracks.contains(track)
    }
  }

  /** Enables or disables track. */
  fun setTrackEnabled(track: K, enabled: Boolean) {
    synchronized(tracksLock) {
      if (enabled) {
        enabledTracks.add(track)
      } else {
        enabledTracks.remove(track)
      }

      updateVolume(track)
    }
  }
}