package de.rki.coronawarnapp.dccticketing.ui.consent.two

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DccTicketingConsentTwoFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: DccTicketingConsentTwoViewModel
    private val vcContainerId = VaccinationCertificateContainerId("1")

    private val dccTicketingTransactionContext: DccTicketingTransactionContext = DccTicketingTransactionContext(
        initializationData = generateDccTicketingQrCodeData(),
        allowlist = setOf(generateDccTicketingAllowListEntry())
    )

    private val fragmentArgs = DccTicketingConsentTwoFragmentArgs(
        containerId = TestCertificateContainerId("testCertificateContainerId")
    ).toBundle()

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.nav_graph)
            setCurrentDestination(R.id.dccTicketingConsentTwoFragment)
        }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : DccTicketingConsentTwoViewModel.Factory {
                override fun create(
                    dccTicketingSharedViewModel: DccTicketingSharedViewModel,
                    containerId: CertificateContainerId
                ): DccTicketingConsentTwoViewModel = viewModel
            }
        )
    }

    @Screenshot
    @Test
    fun dccTicketingCertificateSelectionFragment_certificates() {
        every { viewModel.uiState } returns MutableLiveData(
            DccTicketingConsentTwoViewModel.UiState(
                dccTicketingTransactionContext = dccTicketingTransactionContext,
                certificate = mockVaccinationCertificate(2, true)
            )
        )
        launchFragmentInContainer2<DccTicketingConsentTwoFragment>(
            testNavHostController = navController,
            fragmentArgs = fragmentArgs
        )
        takeScreenshot<DccTicketingConsentTwoFragment>()

        // Take legal part screenshot
        onView(withId(R.id.legal_second_bulletpoint_text)).perform(scrollTo())
        takeScreenshot<DccTicketingConsentTwoFragment>("2")

        // Take description bullet point screenshot
        onView(withId(R.id.third_bulletpoint_text)).perform(scrollTo())
        takeScreenshot<DccTicketingConsentTwoFragment>("3")

        // Take privacy information button screenshot
        onView(withId(R.id.privacy_information)).perform(scrollTo())
        takeScreenshot<DccTicketingConsentTwoFragment>("4")
    }

    private fun mockVaccinationCertificate(
        number: Int = 1,
        final: Boolean = false,
        booster: Boolean = false
    ): VaccinationCertificate =
        mockk<VaccinationCertificate>().apply {
            val localDate = Instant.parse("2021-06-01T11:35:00.000Z").toLocalDateUserTz()
            every { fullName } returns "Andrea Schneider"
            every { uniqueCertificateIdentifier } returns "URN:UVCI:01:AT:858CC18CFCF5965EF82F60E4${number}3349AA5#K"
            every { rawCertificate } returns mockk<VaccinationDccV1>().apply {
                every { vaccination } returns mockk<DccV1.VaccinationData>().apply {
                    every { doseNumber } returns number
                    every { totalSeriesOfDoses } returns 2
                    every { vaccinatedOn } returns localDate
                    every { medicalProductId } returns "medicalProductId"
                }
            }
            every { containerId } returns vcContainerId
            every { vaccinatedOn } returns localDate
            every { personIdentifier } returns certificatePersonIdentifier
            every { vaccinatedOn } returns Instant.parse("2021-06-21T11:35:00.000Z").toLocalDateUserTz()
            every { personIdentifier } returns CertificatePersonIdentifier(
                firstNameStandardized = "firstNameStandardized",
                lastNameStandardized = "lastNameStandardized",
                dateOfBirthFormatted = "1943-04-18"
            )
            every { doseNumber } returns number
            every { totalSeriesOfDoses } returns if (booster) number else 2
            every { dateOfBirthFormatted } returns "1981-03-20"
            every { isSeriesCompletingShot } returns final
            every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.vaccinationCertificate)
            every { isDisplayValid } returns true
            every { state } returns CwaCovidCertificate.State.Valid(Instant.now().plus(20))
            every { hasNotificationBadge } returns false
            every { isNew } returns false
            every { isNotScreened } returns true
        }

    private fun generateDccTicketingQrCodeData(): DccTicketingQrCodeData {
        return DccTicketingQrCodeData(
            protocol = "protocol",
            protocolVersion = "protocol_version",
            serviceIdentity = "service_identity",
            privacyUrl = "http://very_privacy_url.de",
            token = UUID.randomUUID().toString(),
            consent = "Yes, please",
            subject = UUID.randomUUID().toString(),
            serviceProvider = "Anbietername"
        )
    }

    private fun generateDccTicketingAllowListEntry(): DccTicketingValidationServiceAllowListEntry {
        return DccTicketingValidationServiceAllowListEntry(
            serviceProvider = "Betreiber_ValidationService",
            hostname = "http://very-host-allow-provider",
            fingerprint256 = mockk()
        )
    }

    private val certificatePersonIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "1981-03-20",
        firstNameStandardized = "firstNameStandardized",
        lastNameStandardized = "lastNameStandardized",
    )
}

@Module
abstract class DccTicketingConsentTwoFragmentModule {
    @ContributesAndroidInjector
    abstract fun dccTicketingConsentTwoFragment(): DccTicketingConsentTwoFragment
}
