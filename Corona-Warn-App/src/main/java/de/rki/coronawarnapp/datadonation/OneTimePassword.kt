package de.rki.coronawarnapp.datadonation

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import java.time.Instant
import java.util.UUID

data class OneTimePassword(
    @JsonProperty("uuid")
    val uuid: UUID = UUID.randomUUID(),
    @JsonProperty("time")
    val time: Instant = Instant.now()
) {
    @get:JsonIgnore
    val edusOneTimePassword: EdusOtp.EDUSOneTimePassword
        get() = EdusOtp.EDUSOneTimePassword.newBuilder()
            .setOtp(uuid.toString())
            .build()

    @get:JsonIgnore
    val payloadForRequest: ByteArray
        get() = edusOneTimePassword.toByteArray()
}
