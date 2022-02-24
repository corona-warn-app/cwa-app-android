package de.rki.coronawarnapp.dccreissuance.notification

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.notification.PersonNotificationSender
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class DccReissuanceNotificationServiceTest : BaseTest() {

    @MockK lateinit var personNotificationSender: PersonNotificationSender
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings
    @MockK lateinit var dccWalletInfo: DccWalletInfo

    private val personIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "01-01-2010",
        firstNameStandardized = "fN",
        lastNameStandardized = "lN",
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { personCertificatesSettings.setDccReissuanceNotifiedAt(any(), any()) } returns Job()
        every { personNotificationSender.showNotification(any()) } just Runs
        every { dccWalletInfo.certificateReissuance } returns mockk()
    }

    @Test
    fun `notify person if no previous dcc reissuance`() = runBlockingTest {
        DccReissuanceNotificationService(
            personCertificatesSettings = personCertificatesSettings,
            personNotificationSender = personNotificationSender
        ).notifyIfNecessary(
            personIdentifier = personIdentifier,
            oldWalletInfo = null,
            newWalletInfo = dccWalletInfo
        )

        coVerify {
            personNotificationSender.showNotification(personIdentifier)
            personCertificatesSettings.setDccReissuanceNotifiedAt(personIdentifier, any())
        }
    }

    @Test
    fun `don't notify person if there is previous dcc reissuance`() = runBlockingTest {
        DccReissuanceNotificationService(
            personCertificatesSettings = personCertificatesSettings,
            personNotificationSender = personNotificationSender
        ).notifyIfNecessary(
            personIdentifier = personIdentifier,
            oldWalletInfo = mockk<DccWalletInfo>().apply { every { certificateReissuance } returns mockk() },
            newWalletInfo = dccWalletInfo
        )

        coVerify(exactly = 0) {
            personNotificationSender.showNotification(personIdentifier)
            personCertificatesSettings.setDccReissuanceNotifiedAt(personIdentifier, any())
        }
    }
}
