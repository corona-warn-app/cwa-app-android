package de.rki.coronawarnapp.dccticketing.ui.consent.two

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
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
    private val vc = DccTicketingValidationCondition(
        lang = "en-en",
        fnt = "SCHNEIDER",
        gnt = "ANDREA",
        dob = "1989-12-12",
        coa = "AF",
        cod = "SJ",
        roa = "AF",
        rod = "SJ",
        type = listOf(
            "r",
            "v",
            "t"
        ),
        category = listOf(
            "Standard"
        ),
        validationClock = "2021-11-03T15:39:43+00:00",
        validFrom = "2021-11-03T07:15:43+00:00",
        validTo = "2021-11-03T15:39:43+00:00",
        hash = null
    )
    private val dccTicketingTransactionContext: DccTicketingTransactionContext = DccTicketingTransactionContext(
        initializationData = generateDccTicketingQrCodeData()
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
                certificate = mockTestCertificate("Andrea Schneider")
            )
        )
        launchFragmentInContainer2<DccTicketingConsentTwoFragment>(
            testNavHostController = navController,
            fragmentArgs = fragmentArgs
        )
        takeScreenshot<DccTicketingConsentTwoFragment>("dcc_consent_2")
    }

    private fun mockTestCertificate(
        name: String,
        isPending: Boolean = false,
        isUpdating: Boolean = false
    ): TestCertificate = mockk<TestCertificate>().apply {
        every { headerExpiresAt } returns Instant.now().plus(20)
        every { isCertificateRetrievalPending } returns isPending
        every { isUpdatingData } returns isUpdating
        every { fullName } returns name
        every { registeredAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { personIdentifier } returns CertificatePersonIdentifier(
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized",
            dateOfBirthFormatted = "1943-04-18"
        )
        every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.testCertificate)
        every { isValid } returns true
        every { sampleCollectedAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { getState() } returns CwaCovidCertificate.State.Valid(headerExpiresAt)
        every { isNew } returns false
        every { containerId } returns TestCertificateContainerId("testCertificateContainerId")
        every { rawCertificate } returns mockk<TestDccV1>().apply {
            every { test } returns mockk<DccV1.TestCertificateData>().apply {
                every { testType } returns "LP6464-4"
            }
        }
        every { isPCRTestCertificate } returns false
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
            serviceProvider = "Space-X"
        )
    }
}

@Module
abstract class DccTicketingConsentTwoFragmentModule {
    @ContributesAndroidInjector
    abstract fun dccTicketingConsentTwoFragment(): DccTicketingConsentTwoFragment
}
