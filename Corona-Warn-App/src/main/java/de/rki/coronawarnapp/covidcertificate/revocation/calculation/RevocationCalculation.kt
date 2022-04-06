package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.tag
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import javax.inject.Inject

@Suppress("FunctionOnlyReturningConstant")
class RevocationCalculation @Inject constructor() {

    fun calculateRevocationEntryForType(
        dgc: CwaCovidCertificate,
        type: RevocationEntryCoordinates.Type
    ): String {
        Timber.tag(TAG).d("calculateRevocationEntryForType(type=%s)", type)
        return with(dgc) {
            when (type) {
                RevocationEntryCoordinates.Type.SIGNATURE -> calculateRevocationEntryTypeSIGNATURE()
                RevocationEntryCoordinates.Type.UCI -> calculateRevocationEntryTypeUCI()
                RevocationEntryCoordinates.Type.COUNTRYCODEUCI -> calculateRevocationEntryTypeCOUNTRYCODEUCI()
            }
        }.also { Timber.tag(TAG).d("revocationEntry=%s", it) }
    }

    private fun CwaCovidCertificate.calculateRevocationEntryTypeUCI(): String = uniqueCertificateIdentifier
        .hash256(endIndex = 16)

    private fun CwaCovidCertificate.calculateRevocationEntryTypeCOUNTRYCODEUCI(): String = headerIssuer
        .plus(uniqueCertificateIdentifier)
        .hash256(endIndex = 16)

    private fun CwaCovidCertificate.calculateRevocationEntryTypeSIGNATURE(): String {
        return "Not yet implemented"
    }

    private fun String.hash256(beginIndex: Int = 0, endIndex: Int) = toByteArray()
        .toByteString()
        .sha256()
        .substring(beginIndex, endIndex)
        .hex()
}

private val TAG = tag<RevocationCalculation>()
