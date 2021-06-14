package de.rki.coronawarnapp.covidcertificate.person.core

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

// Aggregate the certificates and sort them
@Reusable
class PersonCertificatesProvider @Inject constructor(
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val vaccinationRepository: VaccinationRepository,
) {

    // TODO
    val personCertificates: Flow<Set<PersonCertificates>> = emptyFlow()
}
