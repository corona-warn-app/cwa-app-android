package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import org.joda.time.LocalDate

interface RecoveryCertificate : CwaCovidCertificate {
    override val containerId: RecoveryCertificateContainerId
    val testedPositiveOnFormatted: String
    val validFromFormatted: String
    val validUntilFormatted: String

    val testedPositiveOn: LocalDate?
    val validFrom: LocalDate?
    val validUntil: LocalDate?
    val targetDisease: String

    override val rawCertificate: RecoveryDccV1

    companion object {
        const val icon = R.drawable.ic_recovery_certificate
    }
}
