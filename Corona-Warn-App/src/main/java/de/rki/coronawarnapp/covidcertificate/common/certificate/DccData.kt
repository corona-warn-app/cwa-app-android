package de.rki.coronawarnapp.covidcertificate.common.certificate

data class DccData<CertT : DccV1.MetaData>(
    val header: DccHeader,
    val certificate: CertT,
    val certificateJson: String,
    val kid: String,
    val dscMessage: DscMessage,
)

data class DscMessage(
    val protectedHeader: ByteArray,
    val payload: ByteArray,
    val signature: ByteArray,
    val algorithm: Algorithm,
    val kid: String,
) {
    enum class Algorithm(val algName: String) {
        ES256("SHA256withECDSA"),
        PS256("SHA256WithRSA")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DscMessage

        if (!protectedHeader.contentEquals(other.protectedHeader)) return false
        if (!payload.contentEquals(other.payload)) return false
        if (!signature.contentEquals(other.signature)) return false
        if (algorithm != other.algorithm) return false
        if (kid != other.kid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = protectedHeader.contentHashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + kid.hashCode()
        return result
    }
}
