package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.booster.BoosterNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class DccWalletInfoCalculationManagerTest : BaseTest() {

    @MockK lateinit var boosterRulesRepository: BoosterRulesRepository
    @MockK lateinit var boosterNotificationService: BoosterNotificationService
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var dccWalletInfoRepository: DccWalletInfoRepository
    @MockK lateinit var calculation: DccWalletInfoCalculation
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var certificatesPerson1: PersonCertificates
    @MockK lateinit var certificatesPerson2: PersonCertificates
    @MockK lateinit var certificatePersonIdentifier1: CertificatePersonIdentifier
    @MockK lateinit var certificatePersonIdentifier2: CertificatePersonIdentifier
    @MockK lateinit var dccWalletInfo1: DccWalletInfo
    @MockK lateinit var dccWalletInfo2: DccWalletInfo

    lateinit var instance: DccWalletInfoCalculationManager

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { personCertificatesProvider.personCertificates } returns
            flowOf(setOf(certificatesPerson1, certificatesPerson2))
        every { timeStamper.nowUTC } returns Instant.EPOCH.withMillis(1000)
        every { boosterRulesRepository.rules } returns flowOf(listOf())
        every { certificatesPerson1.certificates } returns listOf()
        every { certificatesPerson2.certificates } returns listOf()
        every { certificatesPerson1.personIdentifier } returns certificatePersonIdentifier1
        every { certificatesPerson2.personIdentifier } returns certificatePersonIdentifier2
        every { dccWalletInfo1.validUntilInstant } returns Instant.EPOCH.withMillis(2000)
        every { dccWalletInfo2.validUntilInstant } returns Instant.EPOCH.withMillis(100)
        every { calculation.init(any()) } just Runs
        coEvery { calculation.getDccWalletInfo(any(), any()) } returns dccWalletInfo1
        coEvery { dccWalletInfoRepository.save(any(), any()) } just Runs
        coEvery { boosterNotificationService.notifyIfNecessary(any(), any(), any()) } just Runs
        instance = DccWalletInfoCalculationManager(
            boosterRulesRepository,
            boosterNotificationService,
            personCertificatesProvider,
            dccWalletInfoRepository,
            calculation,
            timeStamper
        )
    }

    @Test
    fun `catches exception`() {
        coEvery { calculation.getDccWalletInfo(any()) } throws Exception()
        assertDoesNotThrow {
            runBlockingTest2 {
                instance.triggerCalculationAfterConfigChange()
            }
        }
    }

    @Test
    fun `calculation runs for each person after certificate change`() {
        every { certificatesPerson1.dccWalletInfo } returns dccWalletInfo1
        every { certificatesPerson2.dccWalletInfo } returns dccWalletInfo2
        runBlockingTest2 {
            instance.triggerCalculationAfterCertificateChange()
        }

        coVerify(exactly = 2) {
            calculation.getDccWalletInfo(any(), any())
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(certificatePersonIdentifier1, dccWalletInfo1)
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(certificatePersonIdentifier2, dccWalletInfo1)
        }
    }

    @Test
    fun `calculation runs for each person after config change`() {
        every { certificatesPerson1.dccWalletInfo } returns dccWalletInfo1
        every { certificatesPerson2.dccWalletInfo } returns dccWalletInfo2
        runBlockingTest2 {
            instance.triggerCalculationAfterConfigChange()
        }

        coVerify(exactly = 2) {
            calculation.getDccWalletInfo(any(), any())
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(certificatePersonIdentifier1, dccWalletInfo1)
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(certificatePersonIdentifier2, dccWalletInfo1)
        }
    }

    @Test
    fun `calculation runs for each person without walletInfo`() {
        every { certificatesPerson1.dccWalletInfo } returns null
        every { certificatesPerson2.dccWalletInfo } returns dccWalletInfo1

        runBlockingTest2 {
            instance.triggerCalculationAfterConfigChange(false)
        }

        coVerify(exactly = 1) {
            calculation.getDccWalletInfo(any(), any())
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(certificatePersonIdentifier1, dccWalletInfo1)
        }
        coVerify(exactly = 0) {
            dccWalletInfoRepository.save(certificatePersonIdentifier2, dccWalletInfo1)
        }
    }

    @Test
    fun `calculation runs for each person with invalid walletInfo`() {
        every { certificatesPerson1.dccWalletInfo } returns dccWalletInfo1
        every { certificatesPerson2.dccWalletInfo } returns dccWalletInfo2

        runBlockingTest2 {
            instance.triggerCalculationAfterConfigChange(false)
        }

        coVerify(exactly = 1) {
            calculation.getDccWalletInfo(any(), any())
        }
        coVerify(exactly = 0) {
            dccWalletInfoRepository.save(certificatePersonIdentifier1, dccWalletInfo1)
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(certificatePersonIdentifier2, dccWalletInfo1)
        }
    }
}
