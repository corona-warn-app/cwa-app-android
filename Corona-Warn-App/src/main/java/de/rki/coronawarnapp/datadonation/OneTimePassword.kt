package de.rki.coronawarnapp.datadonation

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
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
    val payloadForRequest = uuid.toString().toByteArray()
}
