package de.rki.coronawarnapp.gstatus.notification

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.AdmissionState
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.notification.PersonNotificationSender
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class GStatusNotificationServiceTest : BaseTest() {

    @MockK lateinit var personNotificationSender: PersonNotificationSender
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings

    @MockK lateinit var oldWalletInfo: DccWalletInfo
    @MockK lateinit var newWalletInfo: DccWalletInfo

    @MockK lateinit var oldAdmissionState: AdmissionState
    @MockK lateinit var newAdmissionState: AdmissionState

    private val personIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "01-01-2010",
        firstNameStandardized = "fN",
        lastNameStandardized = "lN",
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { personCertificatesSettings.setGStatusNotifiedAt(any(), any()) } just Runs
        coEvery { personCertificatesSettings.dismissGStatusBadge(any()) } just Runs
        every { personNotificationSender.showNotification(any(), any()) } just Runs
        every { oldWalletInfo.admissionState } returns oldAdmissionState
        every { newWalletInfo.admissionState } returns newAdmissionState
    }

    @Test
    fun `don't notify person if admission state is calculated for the first time`() = runTest {
        every { oldAdmissionState.identifier } returns null
        every { newAdmissionState.identifier } returns "1"

        GStatusNotificationService(
            personCertificatesSettings = personCertificatesSettings,
            personNotificationSender = personNotificationSender
        ).notifyIfNecessary(
            personIdentifier = personIdentifier,
            oldWalletInfo = oldWalletInfo,
            newWalletInfo = newWalletInfo
        )

        coVerify(exactly = 0) {
            personNotificationSender.showNotification(personIdentifier, R.string.notification_body)
            personCertificatesSettings.setGStatusNotifiedAt(personIdentifier, any())
        }
    }

    @Test
    fun `notify person if admission state for person changes`() = runTest {
        every { oldAdmissionState.identifier } returns "1"
        every { newAdmissionState.identifier } returns "2"

        GStatusNotificationService(
            personCertificatesSettings = personCertificatesSettings,
            personNotificationSender = personNotificationSender
        ).notifyIfNecessary(
            personIdentifier = personIdentifier,
            oldWalletInfo = oldWalletInfo,
            newWalletInfo = newWalletInfo
        )

        coVerify {
            personNotificationSender.showNotification(personIdentifier, R.string.notification_body)
            personCertificatesSettings.setGStatusNotifiedAt(personIdentifier, any())
        }
    }

    @Test
    fun `don't notify person if admission state has not changed`() = runTest {
        every { oldAdmissionState.identifier } returns "1"
        every { newAdmissionState.identifier } returns "1"

        GStatusNotificationService(
            personCertificatesSettings = personCertificatesSettings,
            personNotificationSender = personNotificationSender
        ).notifyIfNecessary(
            personIdentifier = personIdentifier,
            oldWalletInfo = oldWalletInfo,
            newWalletInfo = newWalletInfo
        )

        coVerify(exactly = 0) {
            personNotificationSender.showNotification(personIdentifier, R.string.notification_body)
            personCertificatesSettings.setGStatusNotifiedAt(personIdentifier, any())
        }
    }

    @Test
    fun `dismiss the badge if the new admission state calculation doesn't exist`() = runTest {
        every { oldAdmissionState.identifier } returns "1"
        every { newAdmissionState.identifier } returns null

        GStatusNotificationService(
            personCertificatesSettings = personCertificatesSettings,
            personNotificationSender = personNotificationSender
        ).notifyIfNecessary(
            personIdentifier = personIdentifier,
            oldWalletInfo = oldWalletInfo,
            newWalletInfo = newWalletInfo
        )

        coEvery {
            personCertificatesSettings.dismissGStatusBadge(personIdentifier)
        }

        coVerify(exactly = 0) {
            personNotificationSender.showNotification(personIdentifier, R.string.notification_body)
            personCertificatesSettings.setGStatusNotifiedAt(personIdentifier, any())
        }
    }
}
