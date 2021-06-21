package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

interface RecoveryCertificate : CwaCovidCertificate {
    val testedPositiveOnFormatted: String
    val validFromFormatted: String
    val validUntilFormatted: String
}
