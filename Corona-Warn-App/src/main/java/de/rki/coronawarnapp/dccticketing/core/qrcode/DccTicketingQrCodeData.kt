package de.rki.coronawarnapp.dccticketing.core.qrcode

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingQrCodeData(
    @JsonProperty("protocol")
    val protocol: String,
    @JsonProperty("protocolVersion")
    val protocolVersion: String,
    @JsonProperty("serviceIdentity")
    val serviceIdentity: String,
    @JsonProperty("privacyUrl")
    val privacyUrl: String,
    @JsonProperty("token")
    val token: String,
    @JsonProperty("consent")
    val consent: String,
    @JsonProperty("subject")
    val subject: String,
    @JsonProperty("serviceProvider")
    val serviceProvider: String,
) : Parcelable
