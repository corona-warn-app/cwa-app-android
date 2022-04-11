package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import okio.ByteString
import okio.ByteString.Companion.toByteString
import timber.log.Timber

fun CwaCovidCertificate.calculateRevocationEntryForType(type: RevocationHashType): ByteString {
    Timber.tag(TAG).d("calculateRevocationEntryForType(type=%s)", type)
    return when (type) {
        RevocationHashType.SIGNATURE -> calculateRevocationEntryTypeSIGNATURE()
        RevocationHashType.UCI -> calculateRevocationEntryTypeUCI()
        RevocationHashType.COUNTRYCODEUCI -> calculateRevocationEntryTypeCOUNTRYCODEUCI()
    }.also { Timber.tag(TAG).d("revocationEntry=%s", it) }
}

private fun CwaCovidCertificate.calculateRevocationEntryTypeUCI(): ByteString = uniqueCertificateIdentifier
    .hash256(endIndex = BYTE_COUNT)

private fun CwaCovidCertificate.calculateRevocationEntryTypeCOUNTRYCODEUCI(): ByteString = headerIssuer
    .plus(uniqueCertificateIdentifier)
    .hash256(endIndex = BYTE_COUNT)

private fun CwaCovidCertificate.calculateRevocationEntryTypeSIGNATURE(): ByteString {
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

private fun ByteString.hash256(beginIndex: Int = 0, endIndex: Int): ByteString = sha256()
    .substring(beginIndex, endIndex)

private fun ByteString.bisect(): ByteString = when (size > 1) {
    true -> {
        val half = size / 2
        substring(endIndex = half)
    }
    false -> this
}

private const val BYTE_COUNT = 16
private const val TAG = "RevocationCalculation"
