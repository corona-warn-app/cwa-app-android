package de.rki.coronawarnapp.covidcertificate.common.certificate

import okio.ByteString

data class DccData<CertT : DccV1.MetaData>(
    val header: DccHeader,
    val certificate: CertT,
    val certificateJson: String,
    val kid: String,
    val dscMessage: DscMessage,
)

data class DscMessage(
    val protectedHeader: ByteString,
    val payload: ByteString,
    val signature: ByteString,
    val algorithm: Algorithm,
    val kid: String,
) {
    enum class Algorithm(val algName: String) {
        /**
         * Available since Android API 11+
         */
        ES256("SHA256withECDSA"),

        /**
         * Available since Android API 23+
         */
        PS256("SHA256withRSA/PSS")
    }
}
