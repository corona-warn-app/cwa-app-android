package de.rki.coronawarnapp.datadonation

import java.util.UUID

data class OneTimePassword(val uuid: UUID = UUID.randomUUID()) {

    val payloadForRequest = uuid.toString().toByteArray()
}
