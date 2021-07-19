package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.covidcertificate.common.decoder.RawCOSEObject

data class DccData<CertT : DccV1.MetaData>(
    val header: DccHeader,
    val certificate: CertT,
    val certificateJson: String,
    val kid: String,
    val signature: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DccData<*>

        if (header != other.header) return false
        if (certificate != other.certificate) return false
        if (certificateJson != other.certificateJson) return false
        if (kid != other.kid) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + certificate.hashCode()
        result = 31 * result + certificateJson.hashCode()
        result = 31 * result + kid.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}
