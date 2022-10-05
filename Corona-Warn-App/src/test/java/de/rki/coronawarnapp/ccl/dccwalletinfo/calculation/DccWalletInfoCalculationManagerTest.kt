package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.booster.BoosterNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.dccreissuance.notification.DccReissuanceNotificationService
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUtc
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import testhelpers.BaseTest
import java.time.Instant

class DccWalletInfoCalculationManagerTest : BaseTest() {

    @MockK lateinit var boosterRulesRepository: BoosterRulesRepository
    @MockK lateinit var boosterNotificationService: BoosterNotificationService
    @MockK lateinit var dccReissuanceNotificationService: DccReissuanceNotificationService
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var dccWalletInfoRepository: DccWalletInfoRepository
    @MockK lateinit var calculation: DccWalletInfoCalculation
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var certificatesPersonA: PersonCertificates
    @MockK lateinit var certificatesPersonB: PersonCertificates
    @MockK lateinit var dccWalletInfo1: DccWalletInfo
    @MockK lateinit var dccWalletInfo2: DccWalletInfo
    @MockK lateinit var dccValidationRepository: DccValidationRepository

    lateinit var instance: DccWalletInfoCalculationManager

    private val identifierA = CertificatePersonIdentifier(
        dateOfBirthFormatted = "01.10.1982",
        firstNameStandardized = "fNA",
        lastNameStandardized = "lNA"
    )

    private val vaccinationCertA = mockk<VaccinationCertificate>().apply {
        every { personIdentifier } returns identifierA
        every { vaccinatedOn } returns Instant.EPOCH.toLocalDateUtc()
        every { hasNotificationBadge } returns false
        every { headerIssuedAt } returns Instant.EPOCH
    }

    private val identifierB = CertificatePersonIdentifier(
        dateOfBirthFormatted = "01.01.1976",
        firstNameStandardized = "fNB",
        lastNameStandardized = "lNB"
    )

    private val vaccinationCertB = mockk<VaccinationCertificate>().apply {
        every { personIdentifier } returns identifierB
        every { vaccinatedOn } returns Instant.EPOCH.toLocalDateUtc()
        every { hasNotificationBadge } returns false
        every { headerIssuedAt } returns Instant.EPOCH
    }

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { personCertificatesProvider.personCertificates } returns
            flowOf(setOf(certificatesPersonA, certificatesPersonB))
        every { timeStamper.nowUTC } returns Instant.EPOCH.plusMillis(1000)
        every { boosterRulesRepository.rules } returns flowOf(listOf())
        every { dccValidationRepository.invalidationRules } returns flowOf(listOf())
        every { certificatesPersonA.certificates } returns listOf(vaccinationCertA)
        every { certificatesPersonB.certificates } returns listOf(vaccinationCertB)
        every { certificatesPersonA.personIdentifier } returns identifierA
        every { certificatesPersonB.personIdentifier } returns identifierB

        every { dccWalletInfo1.validUntilInstant } returns Instant.EPOCH.plusMillis(2000)
        every { dccWalletInfo2.validUntilInstant } returns Instant.EPOCH.plusMillis(100)
        every { calculation.init(any(), any()) } just Runs
        coEvery { calculation.getDccWalletInfo(any(), "", any()) } returns dccWalletInfo1
        coEvery { dccWalletInfoRepository.save(any(), any()) } just Runs
        coEvery { boosterNotificationService.notifyIfNecessary(any(), any(), any()) } just Runs
        coEvery { dccReissuanceNotificationService.notifyIfNecessary(any(), any(), any()) } just Runs
        instance = DccWalletInfoCalculationManager(
            boosterRulesRepository = boosterRulesRepository,
            notificationServices = setOf(boosterNotificationService, dccReissuanceNotificationService),
            personCertificatesProvider = personCertificatesProvider,
            dccWalletInfoRepository = dccWalletInfoRepository,
            calculation = calculation,
            timeStamper = timeStamper,
            dccValidationRepository = dccValidationRepository
        )
    }

    @Test
    fun `catches exception`() = runTest {
        coEvery { calculation.getDccWalletInfo(any()) } throws Exception()
        assertDoesNotThrow {
            instance.triggerAfterConfigChange("")
        }
    }

    @Test
    fun `calculation runs for each person after certificate change`() = runTest {
        every { certificatesPersonA.dccWalletInfo } returns dccWalletInfo1
        every { certificatesPersonB.dccWalletInfo } returns dccWalletInfo2

        instance.triggerNow("")

        coVerify(exactly = 2) {
            calculation.getDccWalletInfo(any(), "", any())
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(identifierA, dccWalletInfo1)
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(identifierB, dccWalletInfo1)
        }
    }

    @Test
    fun `calculation runs for each person after config change`() = runTest {
        every { certificatesPersonA.dccWalletInfo } returns dccWalletInfo1
        every { certificatesPersonB.dccWalletInfo } returns dccWalletInfo2
        instance.triggerAfterConfigChange("")

        coVerify(exactly = 2) {
            calculation.getDccWalletInfo(any(), "", any())
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(identifierA, dccWalletInfo1)
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(identifierB, dccWalletInfo1)
        }
    }

    @Test
    fun `calculation runs for each person without walletInfo`() = runTest {
        every { certificatesPersonA.dccWalletInfo } returns null
        every { certificatesPersonB.dccWalletInfo } returns dccWalletInfo1

        instance.triggerAfterConfigChange("", false)

        coVerify(exactly = 1) {
            calculation.getDccWalletInfo(any(), "", any())
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(identifierA, dccWalletInfo1)
            boosterNotificationService.notifyIfNecessary(
                personIdentifier = identifierA,
                oldWalletInfo = null,
                newWalletInfo = dccWalletInfo1
            )
            dccReissuanceNotificationService.notifyIfNecessary(
                personIdentifier = identifierA,
                oldWalletInfo = null,
                newWalletInfo = dccWalletInfo1
            )
        }
        coVerify(exactly = 0) {
            dccWalletInfoRepository.save(identifierB, dccWalletInfo1)
        }
    }

    @Test
    fun `calculation runs for each person with invalid walletInfo`() = runTest {
        every { certificatesPersonA.dccWalletInfo } returns dccWalletInfo1
        every { certificatesPersonB.dccWalletInfo } returns dccWalletInfo2

        instance.triggerAfterConfigChange("", false)

        coVerify(exactly = 1) {
            calculation.getDccWalletInfo(any(), "", any())
        }
        coVerify(exactly = 0) {
            dccWalletInfoRepository.save(identifierA, dccWalletInfo1)
        }
        coVerify(exactly = 1) {
            dccWalletInfoRepository.save(identifierB, dccWalletInfo1)
        }
    }
}
