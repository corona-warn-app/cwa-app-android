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
        ES256("SHA256withECDSA"),
        PS256("SHA256WithRSA")
    }
}
