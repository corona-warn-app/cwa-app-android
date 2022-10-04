package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.notification.PersonNotificationSender
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

@Suppress("MaxLineLength")
class BoosterNotificationServiceTest : BaseTest() {

    @MockK lateinit var personNotificationSender: PersonNotificationSender
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings

    @MockK lateinit var oldWalletInfo: DccWalletInfo
    @MockK lateinit var newWalletInfo: DccWalletInfo

    @MockK lateinit var oldBoosterNotification: BoosterNotification
    @MockK lateinit var newBoosterNotification: BoosterNotification

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.parse("2021-01-01T00:00:00.000Z")
        every { personNotificationSender.showNotification(any(), any(), any()) } just Runs

        coEvery { personCertificatesSettings.setBoosterNotifiedAt(any(), any()) } just Runs
        coEvery { personCertificatesSettings.clearBoosterRuleInfo(any()) } just Runs

        every { oldWalletInfo.boosterNotification } returns oldBoosterNotification
        every { newWalletInfo.boosterNotification } returns newBoosterNotification
    }

    private fun service() = BoosterNotificationService(
        personNotificationSender = personNotificationSender,
        personCertificatesSettings = personCertificatesSettings,
        timeStamper = timeStamper,
    )

    @Test
    fun `notifyIfNecessary() should send notification if there is a new booster rule id`() = runTest {
        every { newBoosterNotification.identifier } returns "1"
        every { oldBoosterNotification.identifier } returns null

        service().notifyIfNecessary(personIdentifier, oldWalletInfo, newWalletInfo)

        verifyThatLegacyBoosterRuleIsCleared()
        verifyThatBoosterNotificationIsShown()
        verifyThatBoosterNotificationTimeIsUpdated()
    }

    @Test
    fun `notifyIfNecessary() should send notification if there is a new booster rule id and no old wallet info`() =
        runTest {
            every { newBoosterNotification.identifier } returns "1"
            every { oldBoosterNotification.identifier } returns null

            service().notifyIfNecessary(personIdentifier, oldWalletInfo = null, newWalletInfo)

            verifyThatLegacyBoosterRuleIsCleared()
            verifyThatBoosterNotificationIsShown()
            verifyThatBoosterNotificationTimeIsUpdated()
        }

    @Test
    fun `notifyIfNecessary() should send notification if the new booster rule id is different to the old one`() =
        runTest {
            every { newBoosterNotification.identifier } returns "2"
            every { oldBoosterNotification.identifier } returns "1"

            service().notifyIfNecessary(personIdentifier, oldWalletInfo, newWalletInfo)

            verifyThatLegacyBoosterRuleIsCleared()
            verifyThatBoosterNotificationIsShown()
            verifyThatBoosterNotificationTimeIsUpdated()
        }

    @Test
    fun `notifyIfNecessary() should NOT send notification if the new booster rule id null`() = runTest {
        every { newBoosterNotification.identifier } returns null
        every { oldBoosterNotification.identifier } returns "1"

        service().notifyIfNecessary(personIdentifier, oldWalletInfo, newWalletInfo)

        verifyThatNotificationWasNotSent()
        verifyThatLegacyBoosterRuleIsCleared()
        verifyThatBoosterNotificationTimeIsNotUpdated()
    }

    @Test
    fun `notifyIfNecessary() should NOT send notification if the new booster rule id is the same than the old one`() =
        runTest {
            every { newBoosterNotification.identifier } returns "1"
            every { oldBoosterNotification.identifier } returns "1"

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
        coVerify(exactly = 0) { personCertificatesSettings.setBoosterNotifiedAt(personIdentifier, any()) }
    }

    private fun verifyThatNotificationWasNotSent() {
        verify { personNotificationSender wasNot Called }
    }

    private fun verifyThatBoosterNotificationTimeIsUpdated() {
        coVerify(exactly = 1) { personCertificatesSettings.setBoosterNotifiedAt(personIdentifier, any()) }
    }

    private fun verifyThatBoosterNotificationIsShown() {
        verify(exactly = 1) {
            personNotificationSender.showNotification(
                personIdentifier,
                any(),
                R.string.notification_body
            )
        }
    }

    private fun verifyThatLegacyBoosterRuleIsCleared() {
        coVerify(exactly = 1) { personCertificatesSettings.clearBoosterRuleInfo(personIdentifier) }
    }
}
