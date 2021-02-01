package de.rki.coronawarnapp.datadonation

import org.joda.time.Instant
import java.util.UUID

data class OneTimePassword(
    val uuid: UUID = UUID.randomUUID(),
    val time: Instant = Instant.now()
) {

    val payloadForRequest = uuid.toString().toByteArray()
}
