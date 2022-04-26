package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.ExpiringSoon
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Valid
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz

/*
    The list items shall be sorted descending by the following date attributes depending on the type of the DGC:
    for Vaccination Certificates (i.e. DGC with v[0]): the date of the vaccination v[0].dt and issuedAt date
    for Test Certificates (i.e. DGC with t[0]): the date of the sample collection t[0].sc
    for Recovery Certificates (i.e. DGC with r[0]): the date of the sample collection r[0].fr
 */
fun Collection<CwaCovidCertificate>.toCertificateSortOrder(): List<CwaCovidCertificate> {
    return this.sortedWith(
        compareBy(
            {
                when (it) {
                    is VaccinationCertificate -> it.vaccinatedOn
                    is TestCertificate -> it.sampleCollectedAt?.toLocalDateUserTz()
                    is RecoveryCertificate -> it.testedPositiveOn
                    else -> throw IllegalStateException("Can't sort $it")
                }
            },
            {
                when (it) {
                    is VaccinationCertificate -> it.headerIssuedAt.toLocalDateUserTz()
                    is TestCertificate -> it.sampleCollectedAt?.toLocalDateUserTz()
                    is RecoveryCertificate -> it.testedPositiveOn
                    else -> throw IllegalStateException("Can't sort $it")
                }
            }
        )
    ).reversed()
}

/**
 * Finds Fallback DCC according to:
 *  - First VC or RC in valid sorted certificates, if not ⏎
 *  - First Dcc in valid sorted certificates, if not ⏎
 *  - First or `null` from the original certificates list
 */
fun List<CwaCovidCertificate>.findFallbackDcc(): CwaCovidCertificate? {
    val validCerts = filter {
        when (it.state) {
            is Valid, is ExpiringSoon -> true
            else -> false
        }
    }.toCertificateSortOrder()

    return validCerts.firstOrNull { it is VaccinationCertificate || it is RecoveryCertificate } // First VC or RC
        ?: validCerts.firstOrNull() // First from filtered valid list
        ?: firstOrNull() // First from the original certificates list
}
