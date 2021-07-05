package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Reusable
class CertificateProvider @Inject constructor(
    private val testCertificateRepository: TestCertificateRepository,
    private val vaccinationRepository: VaccinationRepository,
    private val recoveryCertificateRepository: RecoveryCertificateRepository
) {

    /**
     * Throws an [Exception] if certificate not found
     */
    suspend fun findCertificate(containerId: CertificateContainerId): CwaCovidCertificate =
        when (containerId) {
            is RecoveryCertificateContainerId ->
                recoveryCertificateRepository
                    .certificates.first()
                    .mapNotNull { it.recoveryCertificate }
                    .find { it.containerId == containerId }!!

            is TestCertificateContainerId ->
                testCertificateRepository
                    .certificates.first()
                    .mapNotNull { it.testCertificate }
                    .find { it.containerId == containerId }!!

            is VaccinationCertificateContainerId ->
                vaccinationRepository
                    .vaccinationInfos.first()
                    .flatMap { person -> person.vaccinationCertificates }
                    .find { it.containerId == containerId }!!
        }
}
