package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates.Type.*
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

class RevocationCalculation @Inject constructor() {

    fun calculateRevocationEntryForType(
        dgc: CwaCovidCertificate,
        type: RevocationEntryCoordinates.Type
    ): String {
        Timber.tag(TAG).d("calculateRevocationEntryForType(type=%s)", type)
        return with(dgc) {
            when (type) {
                SIGNATURE -> calculateRevocationEntryTypeSIGNATURE()
                UCI -> calculateRevocationEntryTypeUCI()
                COUNTRYCODEUCI -> calculateRevocationEntryTypeCOUNTRYCODEUCI()
            }
        }.also { Timber.tag(TAG).d("revocationEntry=%s", it) }
    }

    private fun CwaCovidCertificate.calculateRevocationEntryTypeUCI(): String {
        TODO("Not yet implemented")
    }

    private fun CwaCovidCertificate.calculateRevocationEntryTypeCOUNTRYCODEUCI(): String {
        TODO("Not yet implemented")
    }

    private fun CwaCovidCertificate.calculateRevocationEntryTypeSIGNATURE(): String {
        TODO("Not yet implemented")
    }
}

private val TAG = tag<RevocationCalculation>()
