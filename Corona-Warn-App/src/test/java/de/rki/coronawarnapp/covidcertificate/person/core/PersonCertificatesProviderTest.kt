package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PersonWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2

class PersonCertificatesProviderTest : BaseTest() {
    @MockK lateinit var certificateProvider: CertificateProvider
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings
    @MockK lateinit var dccWalletInfoRepository: DccWalletInfoRepository

    private val identifierA: CertificatePersonIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "10.11.1990",
        firstNameStandardized = "Harry",
        lastNameStandardized = "Potter",
    )
    private val identifierB: CertificatePersonIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "10.10.1980",
        firstNameStandardized = "Moris",
        lastNameStandardized = "Parker",
    )
    private val identifierB2: CertificatePersonIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "10.10.1980",
        firstNameStandardized = "Moris<Lewis",
        lastNameStandardized = "Parker",
    )
    private val identifierC: CertificatePersonIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "10.12.1970",
        firstNameStandardized = "Joe",
        lastNameStandardized = "Doe",
    )

    private val vaccinationCertA = mockk<VaccinationCertificate>().apply {
        every { personIdentifier } returns identifierA
        every { vaccinatedOn } returns Instant.EPOCH.toLocalDateUtc()
        every { hasNotificationBadge } returns false
        every { headerIssuedAt } returns Instant.EPOCH
    }

    private val testCertA = mockk<TestCertificate>().apply {
        every { personIdentifier } returns identifierA
        every { sampleCollectedAt } returns Instant.EPOCH
        every { hasNotificationBadge } returns true
    }

    private val recoveryCertA = mockk<RecoveryCertificate>().apply {
        every { personIdentifier } returns identifierA
        every { validFrom } returns Instant.EPOCH.toLocalDateUtc()
        every { hasNotificationBadge } returns true
        every { testedPositiveOn } returns LocalDate.now()
    }

    // Person B
    private val testCertB = mockk<TestCertificate>().apply {
        every { personIdentifier } returns identifierB2
        every { sampleCollectedAt } returns Instant.EPOCH
        every { hasNotificationBadge } returns true
    }

    private val recoveryCertB = mockk<RecoveryCertificate>().apply {
        every { personIdentifier } returns identifierB
        every { validFrom } returns Instant.EPOCH.toLocalDateUtc()
        every { hasNotificationBadge } returns true
        every { testedPositiveOn } returns LocalDate.now()
    }

    private val rcSet = setOf(recoveryCertA, recoveryCertB)
    private val tcSet = setOf(testCertA, testCertB)

    private val vcSet = setOf(vaccinationCertA)

    private val certificateContainer: CertificateProvider.CertificateContainer = mockk {
        every { vaccinationCwaCertificates } returns vcSet
        every { testCwaCertificates } returns tcSet
        every { recoveryCwaCertificates } returns rcSet
        every { allCwaCertificates } returns (
            recoveryCwaCertificates +
                testCwaCertificates +
                vaccinationCwaCertificates
            )
    }

    private val certificateContainerFlow = MutableStateFlow(certificateContainer)

    val walletInfoA =  PersonWalletInfo(
        personGroupKey = identifierA.groupingKey,
        dccWalletInfo = null
    )

    val walletInfoB =  PersonWalletInfo(
        personGroupKey = identifierB.groupingKey,
        dccWalletInfo = null
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { certificateProvider.certificateContainer } returns certificateContainerFlow

        personCertificatesSettings.apply {
            every { currentCwaUser } returns flowOf(identifierA)
            every { personsSettings } returns flowOf(mapOf())
            coEvery { removeCurrentCwaUser() } just Runs
        }

        every { dccWalletInfoRepository.personWallets } returns flowOf(emptySet())
    }

    private fun createInstance(scope: CoroutineScope) = PersonCertificatesProvider(
        certificatesProvider = certificateProvider,
        personCertificatesSettings = personCertificatesSettings,
        appScope = scope,
        dccWalletInfoRepository = dccWalletInfoRepository
    )

    @Test
    fun `empty data`() = runTest2 {
        val emptyCertificateContainer = CertificateProvider.CertificateContainer(
            recoveryCertificates = emptySet(),
            testCertificates = emptySet(),
            vaccinationCertificates = emptySet()
        )

        certificateContainerFlow.value = emptyCertificateContainer

        val instance = createInstance(this)

        instance.personCertificates.first() shouldBe emptySet()

        verify {
            certificateProvider.certificateContainer
        }
    }

    @Test
    fun `data combination`() = runTest2 {
        val instance = createInstance(this)

        val personCertificates = instance.personCertificates.first()
        val certificatesPersonA = listOf(
            recoveryCertA,
            vaccinationCertA,
            testCertA
        )
        val personA = PersonCertificates(
            certificates = certificatesPersonA,
            isCwaUser = true,
            badgeCount = 2
        )

        val certificatesPersonB = listOf(
            recoveryCertB,
            testCertB
        )
        val personB = PersonCertificates(
            certificates = certificatesPersonB,
            isCwaUser = false,
            badgeCount = 2
        )

        personCertificates shouldBe listOf(personA, personB)

        // Verify person A identifier
        personA.personIdentifier shouldBe vaccinationCertA.personIdentifier
        personA.personIdentifier shouldBe certificatesPersonA.identifier
        certificatesPersonA.identifier shouldBe vaccinationCertA.personIdentifier

        // Verify person B identifier
        personB.personIdentifier shouldBe recoveryCertB.personIdentifier
        personB.personIdentifier shouldBe certificatesPersonB.identifier
        certificatesPersonB.identifier shouldBe recoveryCertB.personIdentifier

        instance.personsBadgeCount.first() shouldBe 4

        verify {
            certificateProvider.certificateContainer
        }
    }

    @Test
    fun `data combination and cwa user is not in the list`() = runTest2 {
        every { personCertificatesSettings.currentCwaUser } returns flowOf(identifierC)
        val instance = createInstance(this)

        instance.personCertificates.first() shouldBe listOf(
            PersonCertificates(
                certificates = listOf(
                    recoveryCertA,
                    vaccinationCertA,
                    testCertA
                ),
                isCwaUser = false,
                badgeCount = 2
            ),
            PersonCertificates(
                certificates = listOf(
                    recoveryCertB,
                    testCertB
                ),
                isCwaUser = false,
                badgeCount = 2
            )
        )

        instance.personsBadgeCount.first() shouldBe 4

        verify {
            certificateProvider.certificateContainer
        }
    }

    @Test
    fun `wallet assignment it correct`() = runTest2 {
        every { dccWalletInfoRepository.personWallets } returns flowOf(setOf(walletInfoA, walletInfoB))
        val instance = createInstance(this)
        instance.personCertificates.first() shouldBe listOf(
            PersonCertificates(
                certificates = listOf(
                    recoveryCertA,
                    vaccinationCertA,
                    testCertA
                ),
                isCwaUser = true,
                badgeCount = 2,
                dccWalletInfo = walletInfoA.dccWalletInfo
            ),
            PersonCertificates(
                certificates = listOf(
                    recoveryCertB,
                    testCertB
                ),
                isCwaUser = false,
                badgeCount = 2,
                dccWalletInfo = walletInfoB.dccWalletInfo
            )
        )
    }
}
