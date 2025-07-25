package com.redpup.racingregisters.companion

import com.google.common.truth.Truth.assertThat
import com.redpup.racingregisters.companion.proto.example
import org.junit.Test

class ProtoUnitTest {
  @Test
  fun hasProto_withContent() {
    val example = example { field = 1}
    assertThat(example.field).isEqualTo(1)
  }
}