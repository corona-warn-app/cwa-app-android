package de.rki.coronawarnapp.covidcertificate.person.core

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

// Aggregate the certificates and sort them
@Reusable
class PersonCertificatesProvider @Inject constructor(
    private val personCertificatesSettings: PersonCertificatesSettings,
    private val vaccinationRepository: VaccinationRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    @AppScope private val appScope: CoroutineScope,
) {

    val personCertificates: Flow<Set<PersonCertificates>> = combine(
        vaccinationRepository.vaccinationInfos.map { vaccPersons ->
            vaccPersons.flatMap { it.vaccinationCertificates }.toSet()
        },
        testCertificateRepository.certificates.map { testWrappers ->
            testWrappers.mapNotNull { it.testCertificate }
        },
        recoveryCertificateRepository.certificates.map { recoveryWrappers ->
            recoveryWrappers.map { it.recoveryCertificate }
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
    }.shareLatest(scope = appScope)

    /**
     * Set the current cwa user with regards to listed persons in the certificates tab.
     * After calling this [personCertificates] will emit new values.
     * Setting it to null deletes it.
     */
    fun setCurrentCwaUser(personIdentifier: CertificatePersonIdentifier?) {
        Timber.d("setCurrentCwaUser(personIdentifier=%s)", personIdentifier)
        personCertificatesSettings.currentCwaUser.update { personIdentifier }
    }

    val badgeCount: Flow<Int> = combine(
        testCertificateRepository.certificates.map { certs ->
            certs.filter { !it.seenByUser && !it.isCertificateRetrievalPending }.size
        },
        vaccinationRepository.vaccinationInfos.map { persons ->
            persons
                .map { it.vaccinationCertificates }
                .flatten()
                .filter { it.getState() !is CwaCovidCertificate.State.Valid }
                .count { it.getState() != it.lastSeenStateChange }
        },
        recoveryCertificateRepository.certificates.map { certs ->
            certs
                .map { it.recoveryCertificate }
                .filter { it.getState() !is CwaCovidCertificate.State.Valid }
                .count { it.getState() != it.lastSeenStateChange }
        },
    ) { newTestCertificates, vacStateChanges, recoveryStateChanges ->
        newTestCertificates + vacStateChanges + recoveryStateChanges
    }.shareLatest(scope = appScope)

    suspend fun acknowledgeStateChange(certificate: CwaCovidCertificate) {
        Timber.tag(TAG).d("acknowledgeStateChange(containerId=$certificate.containerId)")

        if (certificate.getState() is CwaCovidCertificate.State.Valid && certificate.lastSeenStateChange == null) {
            Timber.tag(TAG).d("Current state is valid, and previous state was null, don't acknowledge.")
            return
        }

        when (certificate) {
            is VaccinationCertificate -> vaccinationRepository.acknowledgeState(certificate.containerId)
            is RecoveryCertificate -> recoveryCertificateRepository.acknowledgeState(certificate.containerId)
            is TestCertificate -> testCertificateRepository.acknowledgeState(certificate.containerId)
            else -> throw IllegalArgumentException("Unknown certificate type: $certificate")
        }
    }

    companion object {
        private val TAG = PersonCertificatesProvider::class.simpleName!!
    }
}
