package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.preferences.mockFlowPreference

@Disabled
class PersonCertificatesProviderTest : BaseTest() {
    @MockK lateinit var vaccinationRepo: VaccinationRepository
    @MockK lateinit var testRepo: TestCertificateRepository
    @MockK lateinit var recoveryRepo: RecoveryCertificateRepository
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings
    @MockK lateinit var dccWalletInfoRepository: DccWalletInfoRepository

    private val identifierA = mockk<CertificatePersonIdentifier>()
    private val identifierB = mockk<CertificatePersonIdentifier>()
    private val identifierC = mockk<CertificatePersonIdentifier>()

    private val vaccinatedPersonACertificate1 = mockk<VaccinationCertificate>().apply {
        every { personIdentifier } returns identifierA
        every { vaccinatedOn } returns Instant.EPOCH.toLocalDateUtc()
        every { hasNotificationBadge } returns false
    }
    private val vaccinatedPersonA = mockk<VaccinatedPerson>().apply {
        every { vaccinationCertificates } returns setOf(vaccinatedPersonACertificate1)
    }
    private val testWrapperACertificate = mockk<TestCertificate>().apply {
        every { personIdentifier } returns identifierA
        every { sampleCollectedAt } returns Instant.EPOCH
        every { hasNotificationBadge } returns true
    }
    private val testWrapperA = mockk<TestCertificateWrapper>().apply {
        every { testCertificate } returns testWrapperACertificate
    }
    private val recoveryWrapperACertificate = mockk<RecoveryCertificate>().apply {
        every { personIdentifier } returns identifierA
        every { validFrom } returns Instant.EPOCH.toLocalDateUtc()
        every { hasNotificationBadge } returns true
    }
    private val recoveryWrapperA = mockk<RecoveryCertificateWrapper>().apply {
        every { recoveryCertificate } returns recoveryWrapperACertificate
    }

    // Person B
    private val testCertificateB = mockk<TestCertificate>().apply {
        every { personIdentifier } returns identifierB
        every { sampleCollectedAt } returns Instant.EPOCH
        every { hasNotificationBadge } returns true
    }

    private val recoveryCertificateB = mockk<RecoveryCertificate>().apply {
        every { personIdentifier } returns identifierB
        every { validFrom } returns Instant.EPOCH.toLocalDateUtc()
        every { hasNotificationBadge } returns true
    }

    private val recoveryWrapperB = mockk<RecoveryCertificateWrapper>().apply {
        every { recoveryCertificate } returns recoveryCertificateB
    }

    private val testWrapperB = mockk<TestCertificateWrapper>().apply {
        every { testCertificate } returns testCertificateB
    }

    private val vaccinationPersons = MutableStateFlow(setOf(vaccinatedPersonA))
    private val testWrappers = MutableStateFlow(setOf(testWrapperA, testWrapperB))
    private val recoveryWrappers = MutableStateFlow(setOf(recoveryWrapperA, recoveryWrapperB))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { vaccinationRepo.vaccinationInfos } returns vaccinationPersons
        every { testRepo.certificates } returns testWrappers
        every { recoveryRepo.certificates } returns recoveryWrappers

        personCertificatesSettings.apply {
            every { currentCwaUser } returns mockFlowPreference(identifierA)
        }

        every { dccWalletInfoRepository.personWallets } returns flowOf(emptySet())
    }

    private fun createInstance(scope: CoroutineScope) = PersonCertificatesProvider(
        recoveryCertificateRepository = recoveryRepo,
        testCertificateRepository = testRepo,
        vaccinationRepository = vaccinationRepo,
        personCertificatesSettings = personCertificatesSettings,
        appScope = scope,
        dccWalletInfoRepository = dccWalletInfoRepository
    )

    @Test
    fun `empty data`() = runBlockingTest2(ignoreActive = true) {
        vaccinationPersons.value = emptySet()
        testWrappers.value = emptySet()
        recoveryWrappers.value = emptySet()

        val instance = createInstance(this)

        instance.personCertificates.first() shouldBe emptyList()

        verify {
            recoveryRepo.certificates
            testRepo.certificates
            vaccinationRepo.vaccinationInfos
        }
    }

    @Test
    fun `data combination`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        instance.personCertificates.first() shouldBe listOf(
            PersonCertificates(
                certificates = listOf(
                    vaccinatedPersonACertificate1,
                    testWrapperACertificate,
                    recoveryWrapperACertificate
                ),
                isCwaUser = true,
                badgeCount = 2
            ),
            PersonCertificates(
                certificates = listOf(
                    testCertificateB,
                    recoveryCertificateB
                ),
                isCwaUser = false,
                badgeCount = 2
            )
        )

        instance.personsBadgeCount.first() shouldBe 4

        verify {
            recoveryRepo.certificates
            testRepo.certificates
            vaccinationRepo.vaccinationInfos
        }

        verify(exactly = 0) {
            personCertificatesSettings.currentCwaUser
        }
    }

    @Test
    fun `data combination and cwa user is not in the list`() = runBlockingTest2(ignoreActive = true) {
        personCertificatesSettings.apply {
            every { currentCwaUser } returns mockFlowPreference(identifierC)
        }
        val instance = createInstance(this)

        instance.personCertificates.first() shouldBe listOf(
            PersonCertificates(
                certificates = listOf(
                    vaccinatedPersonACertificate1,
                    testWrapperACertificate,
                    recoveryWrapperACertificate
                ),
                isCwaUser = true,
                badgeCount = 2
            ),
            PersonCertificates(
                certificates = listOf(
                    testCertificateB,
                    recoveryCertificateB
                ),
                isCwaUser = false,
                badgeCount = 2
            )
        )

        instance.personsBadgeCount.first() shouldBe 4

        verify {
            recoveryRepo.certificates
            testRepo.certificates
            vaccinationRepo.vaccinationInfos
            personCertificatesSettings.currentCwaUser
        }
    }
}
