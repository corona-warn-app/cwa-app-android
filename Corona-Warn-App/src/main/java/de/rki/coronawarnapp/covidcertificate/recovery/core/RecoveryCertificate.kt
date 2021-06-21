package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import org.joda.time.LocalDate

interface RecoveryCertificate : CwaCovidCertificate {
    override val containerId: RecoveryCertificateContainerId
    val testedPositiveOn: LocalDate
    val validFrom: LocalDate
    val validUntil: LocalDate

    override val rawCertificate: RecoveryDccV1
}
