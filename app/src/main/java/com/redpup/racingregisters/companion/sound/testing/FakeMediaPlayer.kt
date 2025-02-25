package com.redpup.racingregisters.companion.sound.testing

import com.redpup.racingregisters.companion.sound.AbstractMediaPlayer
import com.redpup.racingregisters.companion.timer.testing.FakeSystemTimer
import java.util.concurrent.atomic.AtomicInteger

/** See [State Diagram](https://developer.android.com/reference/android/media/MediaPlayer.html#StateDiagram). */
enum class State {
  IDLE,
  INITIALIZED,
  PREPARING,
  PREPARED,
  STARTED,
  PAUSED,
  STOPPED,
  PLAYBACK_COMPLETED,
  END,
  ERROR // Unused.
}

private val idCounter = AtomicInteger()

/** A fake media player for testing. */
class FakeMediaPlayer(
  private val timer: FakeSystemTimer,
  var prepareMillis: Long = 10L,
  var durationMillis: Long = 1000L,
) : AbstractMediaPlayer<FakeMediaPlayer> {
  private val id = idCounter.getAndIncrement()
  var state: State = State.INITIALIZED; private set

  var startTime: Long? = null; private set
  var playTask: String? = null; private set
  var remainingDurationMillis = -1L; private set

  var isMuted = false; private set
  var volume = 1.0F; private set
  var speed = 1.0F; private set
  var pitch = 1.0F; private set

  var nextMediaPlayer: FakeMediaPlayer? = null; private set
  var onCompletionListener: ((FakeMediaPlayer) -> Unit)? = null; private set

  override fun numMediaPlayers() = 1

  override fun copy(): FakeMediaPlayer {
    val player = FakeMediaPlayer(timer)
    player.prepareMillis = prepareMillis
    player.durationMillis = durationMillis
    player.isMuted = isMuted
    player.volume = volume
    player.speed = speed
    player.pitch = pitch
    return player
  }

  override fun prepareAsync(listener: () -> Unit): FakeMediaPlayer {
    check(state == State.INITIALIZED || state == State.STOPPED)
    state = State.PREPARING

    val tag = "FakeMediaPlayer-$id-PrepareAsync-${timer.currentTimeMillis}"
    timer.executeAt(tag, prepareMillis + timer.currentTimeMillis) {
      listener()
      state = State.PREPARED
    }
    return this
  }

  /** Synchronously prepares this player. */
  fun prepare(): FakeMediaPlayer {
    check(state == State.INITIALIZED || state == State.STOPPED)
    state = State.PREPARED
    return this
  }

  override fun start(): FakeMediaPlayer {
    check(
      state == State.STARTED
        || state == State.PREPARED
        || state == State.PAUSED
        || state == State.PLAYBACK_COMPLETED
    )
    if (state == State.STARTED) {
      return this
    }

    check(playTask == null)
    state = State.STARTED
    if (remainingDurationMillis < 0) {
      remainingDurationMillis = durationMillis
    }

    val tag = "FakeMediaPlayer-$id-Start-${timer.currentTimeMillis}"
    timer.executeAt(tag, remainingDurationMillis + timer.currentTimeMillis) {
      state = State.PLAYBACK_COMPLETED
      cancelAndReset()
      nextMediaPlayer?.start()
      onCompletionListener?.invoke(this)
    }
    startTime = timer.currentTimeMillis
    playTask = tag
    return this
  }

  override fun pause(): FakeMediaPlayer {
    check(state == State.PAUSED || state == State.STARTED)
    state = State.PAUSED
    remainingDurationMillis -= (timer.currentTimeMillis - startTime!!)
    cancel()
    return this
  }

  override fun stop(): FakeMediaPlayer {
    check(
      state == State.STOPPED
        || state == State.PREPARED
        || state == State.STARTED
        || state == State.PAUSED
        || state == State.PLAYBACK_COMPLETED
    )
    state = State.STOPPED
    cancelAndReset()
    return this
  }

  override fun reset(): FakeMediaPlayer {
    cancelAndReset()
    state = State.IDLE
    return this
  }

  override fun release(): FakeMediaPlayer {
    cancelAndReset()
    state = State.END
    return this
  }

  /** Cancels this media player from playing. */
  private fun cancel() {
    if (playTask != null) {
      timer.cancelEvents(playTask!!)
    }
    playTask = null
  }

  /** Cancels this media player from playing and resets the start time. */
  private fun cancelAndReset() {
    cancel()
    startTime = null
    remainingDurationMillis = -1L
  }

  override fun isPlaying() = state == State.STARTED

  override fun seekToStart(): FakeMediaPlayer {
    check(
      state == State.PREPARED
        || state == State.STARTED
        || state == State.PAUSED
        || state == State.PLAYBACK_COMPLETED
    )

    cancelAndReset()
    if (state == State.STARTED) {
      state = State.PAUSED
      start()
    }
    return this
  }

  override fun duration() = durationMillis.toInt()

  override fun setIsMuted(isMuted: Boolean): FakeMediaPlayer {
    this.isMuted = isMuted
    return this
  }

  override fun setVolume(volume: Float): FakeMediaPlayer {
    this.volume = volume
    return this
  }

  override fun multiplyVolume(ratio: Float): FakeMediaPlayer {
    setVolume(volume * ratio)
    return this
  }

  override fun setSpeed(speed: Float): FakeMediaPlayer {
    this.speed = speed
    return this
  }

  override fun multiplySpeed(ratio: Float): FakeMediaPlayer {
    setSpeed(speed * ratio)
    return this
  }

  override fun setPitch(pitch: Float): FakeMediaPlayer {
    this.pitch = pitch
    return this
  }

  override fun multiplyPitch(ratio: Float): FakeMediaPlayer {
    setPitch(pitch * ratio)
    return this
  }

  override fun setNextMediaPlayer(nextPlayer: FakeMediaPlayer): FakeMediaPlayer {
    this.nextMediaPlayer = nextPlayer
    return this
  }

  override fun setOnCompletionListener(listener: (FakeMediaPlayer) -> Unit): FakeMediaPlayer {
    this.onCompletionListener = listener
    return this
  }
}
