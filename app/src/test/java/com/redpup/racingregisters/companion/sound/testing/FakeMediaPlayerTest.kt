package com.redpup.racingregisters.companion.sound.testing

import com.google.common.truth.Truth.assertThat
import com.redpup.racingregisters.companion.timer.testing.FakeSystemTimer
import org.junit.Before
import org.junit.Test

class FakeMediaPlayerTest {
  private val timer = FakeSystemTimer()

  @Before
  fun setup() {
    timer.currentTimeMillis = 10_000L
  }

  @Test
  fun initialState() {
    val player = FakeMediaPlayer(timer)

    assertThat(player.state).isEqualTo(State.INITIALIZED)

    assertThat(player.prepareMillis).isGreaterThan(0L)
    assertThat(player.durationMillis).isGreaterThan(0L)

    assertThat(player.startTime).isNull()
    assertThat(player.playTask).isNull()

    assertThat(player.isMuted).isEqualTo(false)
    assertThat(player.pitch).isEqualTo(1F)
    assertThat(player.volume).isEqualTo(1F)
    assertThat(player.speed).isEqualTo(1F)

    assertThat(player.nextMediaPlayer).isNull()
    assertThat(player.onCompletionListener).isNull()
  }

  @Test
  fun prepareAsync() {
    val player = FakeMediaPlayer(timer, prepareMillis = 10L)
    val output = mutableListOf<Boolean>()

    player.prepareAsync { output.add(true) }
    assertThat(player.state).isEqualTo(State.PREPARING)
    assertThat(output).isEmpty()

    // Insufficient time.
    timer.advance(5L)
    assertThat(player.state).isEqualTo(State.PREPARING)
    assertThat(output).isEmpty()

    // Sufficient time.
    timer.advance(25L)
    assertThat(player.state).isEqualTo(State.PREPARED)
    assertThat(output).containsExactly(true)
  }

  @Test
  fun start() {
    val player = FakeMediaPlayer(timer)
    player.prepare()

    player.start()

    assertThat(player.state).isEqualTo(State.STARTED)
    assertThat(player.isPlaying()).isTrue()
    assertThat(player.startTime).isNotNull()
    assertThat(player.playTask).isNotNull()
  }

  @Test
  fun pause() {
    val player = FakeMediaPlayer(timer)
    player.prepare()
    player.start()

    player.pause()

    assertThat(player.state).isEqualTo(State.PAUSED)
    assertThat(player.isPlaying()).isFalse()
    assertThat(player.startTime).isNotNull()
    assertThat(player.playTask).isNull()
  }

  @Test
  fun playbackComplete() {
    val player = FakeMediaPlayer(timer, durationMillis = 500L)
    player.prepare()
    player.start()

    // Insufficient time.
    timer.advance(100L)
    assertThat(player.state).isEqualTo(State.STARTED)
    assertThat(player.startTime).isNotNull()
    assertThat(player.playTask).isNotNull()

    // Sufficient time.
    timer.advance(450L)
    assertThat(player.state).isEqualTo(State.PLAYBACK_COMPLETED)
    assertThat(player.startTime).isNull()
    assertThat(player.playTask).isNull()
  }

  @Test
  fun pauseAndResume_playbackComplete() {
    val player = FakeMediaPlayer(timer, durationMillis = 500L)
    player.prepare()
    player.start()

    // Insufficient time.
    timer.advance(100L)
    assertThat(player.state).isEqualTo(State.STARTED)
    assertThat(player.startTime).isNotNull()
    assertThat(player.playTask).isNotNull()

    player.pause()

    // Irrelevant time.
    timer.advance(1000L)

    player.start()

    // Insufficient time.
    timer.advance(100L)
    assertThat(player.state).isEqualTo(State.STARTED)
    assertThat(player.startTime).isNotNull()
    assertThat(player.playTask).isNotNull()

    // Sufficient time.
    timer.advance(350L)
    assertThat(player.state).isEqualTo(State.PLAYBACK_COMPLETED)
    assertThat(player.startTime).isNull()
    assertThat(player.playTask).isNull()
  }

  @Test
  fun onCompletionListener() {
    val player = FakeMediaPlayer(timer, durationMillis = 500L)
    player.prepare()
    player.start()

    val output = mutableListOf<FakeMediaPlayer>()
    player.setOnCompletionListener { output.add(it) }

    timer.advance(1000L)
    assertThat(player.state).isEqualTo(State.PLAYBACK_COMPLETED)
    assertThat(player.startTime).isNull()
    assertThat(player.playTask).isNull()

    assertThat(output).containsExactly(player)
  }

  @Test
  fun nextPlayer() {
    val player = FakeMediaPlayer(timer, durationMillis = 500L)
    player.prepare()
    player.start()

    val nextPlayer = player.copy()
    nextPlayer.prepare()
    player.setNextMediaPlayer(nextPlayer)

    timer.advance(1000L)
    assertThat(player.state).isEqualTo(State.PLAYBACK_COMPLETED)
    assertThat(player.startTime).isNull()
    assertThat(player.playTask).isNull()

    assertThat(nextPlayer.state).isEqualTo(State.STARTED)
  }

  @Test
  fun stop() {
    val player = FakeMediaPlayer(timer)
    player.prepare()
    player.stop()

    assertThat(player.state).isEqualTo(State.STOPPED)
    assertThat(player.isPlaying()).isFalse()
    assertThat(player.startTime).isNull()
    assertThat(player.playTask).isNull()
  }

  @Test
  fun reset() {
    val player = FakeMediaPlayer(timer)
    player.prepare()
    player.reset()

    assertThat(player.state).isEqualTo(State.IDLE)
    assertThat(player.isPlaying()).isFalse()
    assertThat(player.startTime).isNull()
    assertThat(player.playTask).isNull()
  }

  @Test
  fun release() {
    val player = FakeMediaPlayer(timer)
    player.prepare()
    player.release()

    assertThat(player.state).isEqualTo(State.END)
    assertThat(player.isPlaying()).isFalse()
    assertThat(player.startTime).isNull()
    assertThat(player.playTask).isNull()
  }

  @Test
  fun seekToStart_whilePlaying() {
    val player = FakeMediaPlayer(timer, durationMillis = 500L)
    player.prepare()
    player.start()
    timer.advance(250L)
    player.seekToStart()

    assertThat(player.state).isEqualTo(State.STARTED)
    assertThat(player.isPlaying()).isTrue()
    assertThat(player.startTime).isEqualTo(timer.currentTimeMillis)
    assertThat(player.playTask).isNotNull()
    assertThat(player.remainingDurationMillis).isEqualTo(player.durationMillis)
  }

  @Test
  fun seekToStart_whilePaused() {
    val player = FakeMediaPlayer(timer, durationMillis = 500L)
    player.prepare()
    player.start()
    timer.advance(250L)
    player.pause()
    player.seekToStart()

    assertThat(player.state).isEqualTo(State.PAUSED)
    assertThat(player.isPlaying()).isFalse()
    assertThat(player.startTime).isNull()
    assertThat(player.playTask).isNull()
  }

  @Test
  fun volume() {
    val player = FakeMediaPlayer(timer)

    player.setVolume(2.0F)
    assertThat(player.volume).isEqualTo(2.0F)

    player.multiplyVolume(1.5F)
    assertThat(player.volume).isEqualTo(3.0F)
  }

  @Test
  fun pitch() {
    val player = FakeMediaPlayer(timer)

    player.setPitch(2.0F)
    assertThat(player.pitch).isEqualTo(2.0F)

    player.multiplyPitch(1.5F)
    assertThat(player.pitch).isEqualTo(3.0F)
  }

  @Test
  fun speed() {
    val player = FakeMediaPlayer(timer)

    player.setSpeed(2.0F)
    assertThat(player.speed).isEqualTo(2.0F)

    player.multiplySpeed(1.5F)
    assertThat(player.speed).isEqualTo(3.0F)
  }
}