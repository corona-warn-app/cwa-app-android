package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import okio.ByteString
import okio.ByteString.Companion.toByteString
import timber.log.Timber

fun CwaCovidCertificate.calculateRevocationEntryForType(type: RevocationEntryCoordinates.Type): String {
    Timber.tag(TAG).d("calculateRevocationEntryForType(type=%s)", type)
    return when (type) {
        RevocationEntryCoordinates.Type.SIGNATURE -> calculateRevocationEntryTypeSIGNATURE()
        RevocationEntryCoordinates.Type.UCI -> calculateRevocationEntryTypeUCI()
        RevocationEntryCoordinates.Type.COUNTRYCODEUCI -> calculateRevocationEntryTypeCOUNTRYCODEUCI()
    }.also { Timber.tag(TAG).d("revocationEntry=%s", it) }
}

private fun CwaCovidCertificate.calculateRevocationEntryTypeUCI(): String = uniqueCertificateIdentifier
    .hash256(endIndex = BYTE_COUNT)

private fun CwaCovidCertificate.calculateRevocationEntryTypeCOUNTRYCODEUCI(): String = headerIssuer
    .plus(uniqueCertificateIdentifier)
    .hash256(endIndex = BYTE_COUNT)

private fun CwaCovidCertificate.calculateRevocationEntryTypeSIGNATURE(): String {
    val (alg, signature) = with(dccData.dscMessage) { algorithm to signature }
    val byteSequenceToHash = when (alg) {
        DscMessage.Algorithm.ES256 -> signature.bisect()
        DscMessage.Algorithm.PS256 -> signature
    }
    return byteSequenceToHash.hash256(endIndex = BYTE_COUNT)
}

private fun String.hash256(beginIndex: Int = 0, endIndex: Int) = toByteArray()
    .toByteString()
    .hash256(beginIndex, endIndex)

private fun ByteString.hash256(beginIndex: Int = 0, endIndex: Int) = sha256()
    .substring(beginIndex, endIndex)
    .hex()

private fun ByteString.bisect(): ByteString = when (size > 0) {
    true -> {
        val half = size / 2
        substring(endIndex = half)
    }
    false -> this
}

private const val BYTE_COUNT = 16
private const val TAG = "RevocationCalculation"
