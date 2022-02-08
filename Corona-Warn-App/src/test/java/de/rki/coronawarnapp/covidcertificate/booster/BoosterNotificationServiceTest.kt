package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class BoosterNotificationServiceTest : BaseTest() {

    @MockK lateinit var boosterNotificationSender: BoosterNotificationSender
    @MockK lateinit var vaccinationRepository: VaccinationRepository
    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var vaccinatedPerson: VaccinatedPerson
    @MockK lateinit var vaccinatedPersonData: VaccinatedPersonData

    @MockK lateinit var oldWalletInfo: DccWalletInfo
    @MockK lateinit var newWalletInfo: DccWalletInfo

    @MockK lateinit var oldBoosterNotification: BoosterNotification
    @MockK lateinit var newBoosterNotification: BoosterNotification

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.parse("2021-01-01T00:00:00.000Z")
        every { boosterNotificationSender.showBoosterNotification(any()) } just Runs

        coEvery { vaccinationRepository.updateBoosterNotifiedAt(any(), any()) } just Runs
        coEvery { vaccinationRepository.clearBoosterRuleInfo(any()) } just Runs

        every { oldWalletInfo.boosterNotification } returns oldBoosterNotification
        every { newWalletInfo.boosterNotification } returns newBoosterNotification

        every { vaccinatedPersonData.boosterRuleIdentifier } returns null
        every { vaccinatedPerson.identifier } returns personIdentifier
        every { vaccinatedPerson.data } returns vaccinatedPersonData
        every { vaccinationRepository.vaccinationInfos } returns flowOf(setOf(vaccinatedPerson))
    }

    private fun service() = BoosterNotificationService(
        boosterNotificationSender = boosterNotificationSender,
        vaccinationRepository = vaccinationRepository,
        timeStamper = timeStamper
    )

    @Test
    fun `notifyIfNecessary() should send notification if there is a new booster rule id`() = runBlockingTest {
        every { newBoosterNotification.identifier } returns "1"
        every { oldBoosterNotification.identifier } returns null

        service().notifyIfNecessary(personIdentifier, oldWalletInfo, newWalletInfo)

        verifyThatLegacyBoosterRuleIsNotCleared()
        verifyThatBoosterNotificationIsShown()
        verifyThatBoosterNotificationTimeIsUpdated()
    }

    @Test
    fun `notifyIfNecessary() should send notification if there is a new booster rule id and no old wallet info`() =
        runBlockingTest {
            every { newBoosterNotification.identifier } returns "1"
            every { oldBoosterNotification.identifier } returns null

            service().notifyIfNecessary(personIdentifier, oldWalletInfo = null, newWalletInfo)

            verifyThatLegacyBoosterRuleIsNotCleared()
            verifyThatBoosterNotificationIsShown()
            verifyThatBoosterNotificationTimeIsUpdated()
        }

    @Test
    fun `notifyIfNecessary() should send notification if the new booster rule id is different to the old one`() =
        runBlockingTest {
            every { newBoosterNotification.identifier } returns "2"
            every { oldBoosterNotification.identifier } returns "1"

            service().notifyIfNecessary(personIdentifier, oldWalletInfo, newWalletInfo)

            verifyThatLegacyBoosterRuleIsNotCleared()
            verifyThatBoosterNotificationIsShown()
            verifyThatBoosterNotificationTimeIsUpdated()
        }

    @Test
    fun `notifyIfNecessary() should NOT send notification if the new booster rule id null`() = runBlockingTest {
        every { newBoosterNotification.identifier } returns null
        every { oldBoosterNotification.identifier } returns "1"

        service().notifyIfNecessary(personIdentifier, oldWalletInfo, newWalletInfo)

        verifyThatNotificationWasNotSent()
        verifyThatLegacyBoosterRuleIsCleared()
        verifyThatBoosterNotificationTimeIsNotUpdated()
    }

    @Test
    fun `notifyIfNecessary() should NOT send notification if the new booster rule id is the same than the old one`() =
        runBlockingTest {
            every { newBoosterNotification.identifier } returns "1"
            every { oldBoosterNotification.identifier } returns "1"

            service().notifyIfNecessary(personIdentifier, oldWalletInfo, newWalletInfo)

            verifyThatNotificationWasNotSent()
            verifyThatBoosterNotificationTimeIsNotUpdated()
        }

    @Test
    fun `notifyIfNecessary() should NOT send notification if the new booster rule id is the same than the legacy booster rule id`() =
        runBlockingTest {
            every { newBoosterNotification.identifier } returns "2"
            every { oldBoosterNotification.identifier } returns "1"

            // legacy booster rule id
            every { vaccinatedPersonData.boosterRuleIdentifier } returns "2"

            service().notifyIfNecessary(personIdentifier, oldWalletInfo, newWalletInfo)

            verifyThatNotificationWasNotSent()
            verifyThatBoosterNotificationTimeIsNotUpdated()
        }

    private val personIdentifier = CertificatePersonIdentifier(
        firstNameStandardized = "Erika",
        lastNameStandardized = "MusterFrau",
        dateOfBirthFormatted = "1980-01-01"
    )

    private fun verifyThatBoosterNotificationTimeIsNotUpdated() {
        coVerify(exactly = 0) { vaccinationRepository.updateBoosterNotifiedAt(personIdentifier, any()) }
    }

    private fun verifyThatNotificationWasNotSent() {
        verify { boosterNotificationSender wasNot Called }
    }

    private fun verifyThatBoosterNotificationTimeIsUpdated() {
        coVerify(exactly = 1) { vaccinationRepository.updateBoosterNotifiedAt(personIdentifier, any()) }
    }

    private fun verifyThatBoosterNotificationIsShown() {
        verify(exactly = 1) { boosterNotificationSender.showBoosterNotification(personIdentifier) }
    }

    private fun verifyThatLegacyBoosterRuleIsNotCleared() {
        coVerify(exactly = 0) { vaccinationRepository.clearBoosterRuleInfo(personIdentifier) }
    }

    private fun verifyThatLegacyBoosterRuleIsCleared() {
        coVerify(exactly = 1) { vaccinationRepository.clearBoosterRuleInfo(personIdentifier) }
    }
}
