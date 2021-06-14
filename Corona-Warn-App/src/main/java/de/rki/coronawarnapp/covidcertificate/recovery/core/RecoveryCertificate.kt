package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import org.joda.time.LocalDate

interface RecoveryCertificate : CwaCovidCertificate {
    val testedPositiveOn: LocalDate
    val validFrom: LocalDate
    val validUntil: LocalDate
}
