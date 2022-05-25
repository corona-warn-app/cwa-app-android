package de.rki.coronawarnapp.datadonation

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import java.time.Instant
import java.util.UUID

@Keep
data class OneTimePassword(
    @SerializedName("uuid")
    val uuid: UUID = UUID.randomUUID(),
    @SerializedName("time")
    val time: Instant = Instant.now()
) {

    val edusOneTimePassword: EdusOtp.EDUSOneTimePassword
        get() = EdusOtp.EDUSOneTimePassword.newBuilder()
            .setOtp(uuid.toString())
            .build()

    val payloadForRequest: ByteArray
        get() = edusOneTimePassword.toByteArray()
}
