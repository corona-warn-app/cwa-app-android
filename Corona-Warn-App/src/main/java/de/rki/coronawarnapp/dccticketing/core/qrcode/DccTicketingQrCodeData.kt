package de.rki.coronawarnapp.dccticketing.core.qrcode

import com.google.gson.annotations.SerializedName

data class DccTicketingQrCodeData(
    @SerializedName("protocol")
    val protocol: String,
    @SerializedName("protocolVersion")
    val protocolVersion: String,
    @SerializedName("serviceIdentity")
    val serviceIdentity: String,
    @SerializedName("privacyUrl")
    val privacyUrl: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("consent")
    val consent: String,
    @SerializedName("subject")
    val subject: String?,
    @SerializedName("serviceProvider")
    val serviceProvider: String?,
)
