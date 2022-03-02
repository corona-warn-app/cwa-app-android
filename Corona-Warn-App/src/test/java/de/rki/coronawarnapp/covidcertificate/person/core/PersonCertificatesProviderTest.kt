package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
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
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class PersonCertificatesProviderTest : BaseTest() {
    @MockK lateinit var certificateProvider: CertificateProvider
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings
    @MockK lateinit var dccWalletInfoRepository: DccWalletInfoRepository

    private val identifierA: CertificatePersonIdentifier = mockk {
        every { groupingKey } returns "identifierA"
        every { codeSHA256 } returns groupingKey.toSHA256()
    }
    private val identifierB: CertificatePersonIdentifier = mockk {
        every { groupingKey } returns "identifierB"
        every { codeSHA256 } returns groupingKey.toSHA256()
    }
    private val identifierC: CertificatePersonIdentifier = mockk {
        every { groupingKey } returns "identifierC"
        every { codeSHA256 } returns groupingKey.toSHA256()
    }

    private val vaccinatedPersonACertificate1 = mockk<VaccinationCertificate>().apply {
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
    }

    // Person B
    private val testCertB = mockk<TestCertificate>().apply {
        every { personIdentifier } returns identifierB
        every { sampleCollectedAt } returns Instant.EPOCH
        every { hasNotificationBadge } returns true
    }

    private val recoveryCertB = mockk<RecoveryCertificate>().apply {
        every { personIdentifier } returns identifierB
        every { validFrom } returns Instant.EPOCH.toLocalDateUtc()
        every { hasNotificationBadge } returns true
    }

    private val rcSet = setOf(recoveryCertA, recoveryCertB)
    private val tcSet = setOf(testCertA, testCertB)
//    private val vcInfoSet = setOf(vaccinatedPersonA)
    private val vcSet = setOf(vaccinatedPersonACertificate1)

    private val certificateContainer: CertificateProvider.CertificateContainer = mockk {
//        every { vaccinationInfos } returns vcInfoSet
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

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { certificateProvider.certificateContainer } returns certificateContainerFlow

        personCertificatesSettings.apply {
            every { currentCwaUser } returns flowOf(identifierA)
            every { personsSettings } returns flowOf(mapOf())
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
    fun `empty data`() = runBlockingTest2(ignoreActive = true) {
        val emptyCertificateContainer = CertificateProvider.CertificateContainer(
            recoveryCertificates = emptySet(),
            testCertificates = emptySet(),
            vaccinationCertificates = emptySet()
        )

        certificateContainerFlow.value = emptyCertificateContainer

        val instance = createInstance(this)

        instance.personCertificates.first() shouldBe emptyList()

        verify {
            certificateProvider.certificateContainer
        }
    }

    @Test
    fun `data combination`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)

        instance.personCertificates.first() shouldBe listOf(
            PersonCertificates(
                certificates = listOf(
                    vaccinatedPersonACertificate1,
                    testCertA,
                    recoveryCertA
                ),
                isCwaUser = true,
                badgeCount = 2
            ),
            PersonCertificates(
                certificates = listOf(
                    testCertB,
                    recoveryCertB
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
    fun `data combination and cwa user is not in the list`() = runBlockingTest2(ignoreActive = true) {
        every { personCertificatesSettings.currentCwaUser } returns flowOf(identifierC)
        val instance = createInstance(this)

        instance.personCertificates.first() shouldBe listOf(
            PersonCertificates(
                certificates = listOf(
                    vaccinatedPersonACertificate1,
                    testCertA,
                    recoveryCertA
                ),
                isCwaUser = false,
                badgeCount = 2
            ),
            PersonCertificates(
                certificates = listOf(
                    testCertB,
                    recoveryCertB
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
}
