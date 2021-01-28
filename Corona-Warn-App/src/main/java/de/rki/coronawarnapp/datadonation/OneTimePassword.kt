package de.rki.coronawarnapp.datadonation

import android.util.Base64
import android.util.Base64.encode
import java.nio.ByteBuffer
import java.util.UUID

data class OneTimePassword(val uuid: UUID = UUID.randomUUID()) {

    val payloadForRequest: ByteArray
        get() = ByteBuffer.wrap(ByteArray(16)).apply {
            putLong(uuid.mostSignificantBits)
            putLong(uuid.leastSignificantBits)
        }.array().let { encode(it, Base64.DEFAULT) }
}
