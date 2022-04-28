package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import timber.log.Timber

fun DccData<out DccV1.MetaData>.calculateCoordinatesToHash(
    type: RevocationHashType
): Pair<RevocationEntryCoordinates, ByteString> {
    Timber.tag(TAG).d("revocationCoordinates(type=%s)", type)
    val hash = calculateRevocationEntryForType(type)
    return Pair(
        first = RevocationEntryCoordinates(
            kid = kidHash(),
            type = type,
            x = hash.substring(0, 1), // First byte as ByteString
            y = hash.substring(1, 2), // Second byte as ByteString
        ),
        second = hash
    ).also { Timber.tag(TAG).d("revocationCoordinates=%s", it) }
}

fun DccData<out DccV1.MetaData>.calculateRevocationEntryForType(type: RevocationHashType): ByteString {
    Timber.tag(TAG).d("calculateRevocationEntryForType(type=%s)", type)
    return when (type) {
        RevocationHashType.SIGNATURE -> calculateRevocationEntryTypeSIGNATURE()
        RevocationHashType.UCI -> calculateRevocationEntryTypeUCI()
        RevocationHashType.COUNTRYCODEUCI -> calculateRevocationEntryTypeCOUNTRYCODEUCI()
    }.also { Timber.tag(TAG).d("revocationEntry=%s", it) }
}

fun DccData<out DccV1.MetaData>.kidHash(): ByteString {
    val kidHash = kid.decodeBase64()
    Timber.tag(TAG).d("kidHash($kidHash)")
    return kidHash ?: error("Bad KID!")
}

private fun DccData<out DccV1.MetaData>.calculateRevocationEntryTypeUCI(): ByteString = certificate
    .payload.uniqueCertificateIdentifier
    .hash256(endIndex = BYTE_COUNT)

private fun DccData<out DccV1.MetaData>.calculateRevocationEntryTypeCOUNTRYCODEUCI(): ByteString = header.issuer
    .plus(certificate.payload.uniqueCertificateIdentifier)
    .hash256(endIndex = BYTE_COUNT)

private fun DccData<out DccV1.MetaData>.calculateRevocationEntryTypeSIGNATURE(): ByteString {
    val (alg, signature) = with(dscMessage) { algorithm to signature }
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
