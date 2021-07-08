package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class PersonCertificatesProviderTest : BaseTest() {
    @MockK lateinit var vaccinationRepo: VaccinationRepository
    @MockK lateinit var testRepo: TestCertificateRepository
    @MockK lateinit var recoveryRepo: RecoveryCertificateRepository
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings

    private val identifierA = mockk<CertificatePersonIdentifier>()

    private val vaccinatedPersonACertificate1 = mockk<VaccinationCertificate>().apply {
        every { personIdentifier } returns identifierA
        every { vaccinatedOn } returns Instant.EPOCH.toLocalDateUtc()
    }
    private val vaccinatedPersonA = mockk<VaccinatedPerson>().apply {
        every { vaccinationCertificates } returns setOf(vaccinatedPersonACertificate1)
    }
    private val testWrapperACertificate = mockk<TestCertificate>().apply {
        every { personIdentifier } returns identifierA
        every { sampleCollectedAt } returns Instant.EPOCH
    }
    private val testWrapperA = mockk<TestCertificateWrapper>().apply {
        every { testCertificate } returns testWrapperACertificate
    }
    private val recoveryWrapperACertificate = mockk<RecoveryCertificate>().apply {
        every { personIdentifier } returns identifierA
        every { validFrom } returns Instant.EPOCH.toLocalDateUtc()
    }
    private val recoveryWrapperA = mockk<RecoveryCertificateWrapper>().apply {
        every { recoveryCertificate } returns recoveryWrapperACertificate
    }

    private val vaccinationPersons = MutableStateFlow(setOf(vaccinatedPersonA))
    private val testWrappers = MutableStateFlow(setOf(testWrapperA))
    private val recoveryWrappers = MutableStateFlow(setOf(recoveryWrapperA))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { vaccinationRepo.vaccinationInfos } returns vaccinationPersons
        every { testRepo.certificates } returns testWrappers
        every { recoveryRepo.certificates } returns recoveryWrappers

        personCertificatesSettings.apply {
            every { currentCwaUser } returns mockFlowPreference(identifierA)
        }
    }

    private fun createInstance() = PersonCertificatesProvider(
        recoveryCertificateRepository = recoveryRepo,
        testCertificateRepository = testRepo,
        vaccinationRepository = vaccinationRepo,
        personCertificatesSettings = personCertificatesSettings,
    )

    @Test
    fun `empty data`() = runBlockingTest {
        vaccinationPersons.value = emptySet()
        testWrappers.value = emptySet()
        recoveryWrappers.value = emptySet()

        val instance = createInstance()

        instance.personCertificates.first() shouldBe emptyList()

        verify {
            recoveryRepo.certificates
            testRepo.certificates
            vaccinationRepo.vaccinationInfos
        }
    }

    @Test
    fun `data combination`() = runBlockingTest {
        val instance = createInstance()

        instance.personCertificates.first() shouldBe listOf(
            PersonCertificates(
                certificates = listOf(
                    vaccinatedPersonACertificate1,
                    testWrapperACertificate,
                    recoveryWrapperACertificate
                ),
                isCwaUser = true,
            )
        )

        verify {
            recoveryRepo.certificates
            testRepo.certificates
            vaccinationRepo.vaccinationInfos
        }
    }
}
