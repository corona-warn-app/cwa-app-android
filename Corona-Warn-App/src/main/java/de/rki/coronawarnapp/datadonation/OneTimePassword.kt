package de.rki.coronawarnapp.datadonation

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import org.joda.time.Instant
import java.util.UUID

@Keep
data class OneTimePassword(
    @SerializedName("uuid")
    val uuid: UUID = UUID.randomUUID(),
    @SerializedName("time")
    val time: Instant = Instant.now()
) {

    @Transient
    val edusOneTimePassword: EdusOtp.EDUSOneTimePassword = EdusOtp.EDUSOneTimePassword.newBuilder()
        .setOtp(uuid.toString())
        .build()

    @Transient
    val payloadForRequest: ByteArray = edusOneTimePassword.toByteArray()
}
