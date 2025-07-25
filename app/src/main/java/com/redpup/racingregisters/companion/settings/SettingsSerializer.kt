package com.redpup.racingregisters.companion.settings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.redpup.racingregisters.companion.settings.proto.RRSettings
import com.redpup.racingregisters.companion.settings.proto.rRSettings
import java.io.InputStream
import java.io.OutputStream

/**
 * A serializer that decodes proto settings to and from storage.
 *
 * See [android docs](https://developer.android.com/topic/libraries/architecture/datastore#proto-create) for more.
 */
object SettingsSerializer : Serializer<RRSettings> {
  override val defaultValue: RRSettings = rRSettings{}

  override suspend fun readFrom(input: InputStream): RRSettings {
    try {
      return RRSettings.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
      throw CorruptionException("Cannot read proto.", exception)
    }
  }

  override suspend fun writeTo(
    t: RRSettings,
    output: OutputStream) = t.writeTo(output)
}

val Context.settingsDataStore: DataStore<RRSettings> by dataStore(
  fileName = "rrsettings.pb",
  serializer = SettingsSerializer
)
