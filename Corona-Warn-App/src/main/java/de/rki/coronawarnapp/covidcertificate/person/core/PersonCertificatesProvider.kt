package de.rki.coronawarnapp.covidcertificate.person.core

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

// Aggregate the certificates and sort them
@Reusable
class PersonCertificatesProvider @Inject constructor(
    private val personCertificatesSettings: PersonCertificatesSettings,
    vaccinationRepository: VaccinationRepository,
    testCertificateRepository: TestCertificateRepository,
    recoveryCertificateRepository: RecoveryCertificateRepository,
) {

    val personCertificates: Flow<Set<PersonCertificates>> = combine(
        vaccinationRepository.vaccinationInfos.map { vaccPersons ->
            vaccPersons.flatMap { it.vaccinationCertificates }.toSet()
        },
        testCertificateRepository.certificates.map { testWrappers ->
            testWrappers.mapNotNull { it.testCertificate }
        },
        recoveryCertificateRepository.certificates.map { recoveryWrappers ->
            recoveryWrappers.mapNotNull { it.testCertificate }
        },
        personCertificatesSettings.currentCwaUser.flow,
    ) { vaccs, tests, recos, cwaUser ->
        val mapping = mutableMapOf<CertificatePersonIdentifier, MutableSet<CwaCovidCertificate>>()

        val allCerts: Set<CwaCovidCertificate> = (vaccs + tests + recos)
        allCerts.forEach {
            mapping[it.personIdentifier] = (mapping[it.personIdentifier] ?: mutableSetOf()).apply {
                add(it)
            }
        }

        mapping.entries.map { (personIdentifier, certs) ->
            Timber.tag(TAG).v("PersonCertificates for %s with %d certs.", personIdentifier, certs.size)
            PersonCertificates(
                certificates = certs.toCertificateSortOrder(),
                isCwaUser = personIdentifier == cwaUser,
            )
        }.toSet()
    }

    /**
     * Set the current cwa user with regards to listed persons in the certificates tab.
     * After calling this [personCertificates] will emit new values.
     * Setting it to null deletes it.
     */
    fun setCurrentCwaUser(personIdentifier: CertificatePersonIdentifier?) {
        Timber.d("setCurrentCwaUser(personIdentifier=%s)", personIdentifier)
        personCertificatesSettings.currentCwaUser.update { personIdentifier }
    }

    companion object {
        private val TAG = PersonCertificatesProvider::class.simpleName!!
    }
}
