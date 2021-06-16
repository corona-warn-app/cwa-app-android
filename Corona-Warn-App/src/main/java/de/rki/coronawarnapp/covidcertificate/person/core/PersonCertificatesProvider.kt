package de.rki.coronawarnapp.covidcertificate.person.core

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject

// Aggregate the certificates and sort them
@Reusable
class PersonCertificatesProvider @Inject constructor(
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
        }
    ) { vaccs, tests, recos ->
        val mapping = mutableMapOf<CertificatePersonIdentifier, MutableSet<CwaCovidCertificate>>()

        val allCerts: Set<CwaCovidCertificate> = (vaccs + tests + recos)
        allCerts.forEach {
            mapping[it.personIdentifier] = (mapping[it.personIdentifier] ?: mutableSetOf()).apply {
                add(it)
            }
        }

        mapping.entries.map { (personIdentifier, certs) ->
            Timber.tag(TAG).v("PersonCertificates for %s with %d certs.", personIdentifier, certs.size)
            PersonCertificates(certificates = certs.toPrioritySortOrder())
        }.toSet()
//        + testData // TODO remove
    }

    fun Collection<CwaCovidCertificate>.toPrioritySortOrder(): List<CwaCovidCertificate> {
        // TODO
        return this.toList()
    }

    /**
     * Set the current cwa user with regards to listed persons in the certificates tab.
     * After calling this [personCertificates] will emit new values.
     * Setting it to null deletes it.
     */
    suspend fun setCurrentCwaUser(personIdentifier: CertificatePersonIdentifier?) {
        // TODO
    }

    // TODO remove
    private val testData = (0..30).map { PersonCertificates(listOf(testCertificate(it))) }.toSet() +
        PersonCertificates(listOf(testCertificate(31, true, false))) +
        PersonCertificates(listOf(testCertificate(32, true, true))) +
        PersonCertificates(listOf(testCertificate(33)), true)

    // TODO remove
    private fun testCertificate(
        index: Int,
        isCertificateRetrievalPending: Boolean = false,
        isUpdating: Boolean = false
    ) =
        object : TestCertificate {
            override val targetName: String
                get() = "targetName"
            override val testType: String
                get() = "testType"
            override val testResult: String
                get() = "testResult"
            override val testName: String?
                get() = "testName"
            override val testNameAndManufacturer: String?
                get() = "testNameAndManufacturer"
            override val sampleCollectedAt: Instant
                get() = Instant.now()
            override val testResultAt: Instant?
                get() = Instant.now()
            override val testCenter: String
                get() = "testCenter"
            override val registeredAt: Instant
                get() = Instant.now()
            override val isUpdatingData: Boolean
                get() = isUpdating
            override val isCertificateRetrievalPending: Boolean
                get() = isCertificateRetrievalPending
            override val issuer: String
                get() = "issuer"
            override val issuedAt: Instant
                get() = Instant.now()
            override val expiresAt: Instant
                get() = Instant.now()
            override val qrCode: QrCodeString
                get() = (0..20).fold("") { it, _ -> it + "qrCode$index" }

            override val firstName: String
                get() = "Max"
            override val lastName: String
                get() = "Mustermann"
            override val fullName: String
                get() = "Max Mustermann $index"
            override val dateOfBirth: LocalDate
                get() = LocalDate.now()
            override val personIdentifier: CertificatePersonIdentifier
                get() = CertificatePersonIdentifier(
                    LocalDate.now(),
                    "last$index",
                    "firs$index"
                )
            override val certificateIssuer: String
                get() = "certificateIssuer"
            override val certificateCountry: String
                get() = "certificateCountry"
            override val certificateId: String
                get() = "certificateId"
        }

    companion object {
        private val TAG = PersonCertificatesProvider::class.simpleName!!
    }
}
