package de.rki.coronawarnapp.covidcertificate.test.core.storage.types

import com.google.gson.annotations.SerializedName
import org.joda.time.Instant

data class GenericTestCertificateData(
    @SerializedName("identifier")
    override val identifier: String,

    @SerializedName("registeredAt")
    override val registeredAt: Instant,

    @SerializedName("certificateReceivedAt")
    override val certificateReceivedAt: Instant? = null,

    @SerializedName("testCertificateQrCode")
    override val testCertificateQrCode: String? = null,
) : ScannedTestCertificate() {

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this(
        identifier = "",
        registeredAt = Instant.EPOCH
    )
}
