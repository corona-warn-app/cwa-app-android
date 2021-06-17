package de.rki.coronawarnapp.ui.submission.qrcode.consent

import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.nearby.modules.tekhistory.TEKHistoryProvider
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import java.io.PrintWriter

@ExtendWith(InstantExecutorExtension::class)
class SubmissionConsentViewModelTest {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository
    @MockK lateinit var tekHistoryProvider: TEKHistoryProvider
    @MockK lateinit var testRegistrationStateProcessor: TestRegistrationStateProcessor
    @MockK lateinit var qrCodeValidator: CoronaTestQrCodeValidator

    lateinit var viewModel: SubmissionConsentViewModel

    private val countryList = Country.values().toList()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { interoperabilityRepository.countryList } returns MutableStateFlow(countryList)
        coEvery { submissionRepository.giveConsentToSubmission(any()) } just Runs

        testRegistrationStateProcessor.apply {
            every { state } returns flowOf(TestRegistrationStateProcessor.State.Idle)
            coEvery { startRegistration(any(), any(), any()) } returns mockk()
        }

        viewModel = SubmissionConsentViewModel(
            interoperabilityRepository,
            dispatcherProvider = TestDispatcherProvider(),
            tekHistoryProvider,
            testRegistrationStateProcessor,
            submissionRepository,
            qrCodeValidator
        )
    }

    @Test
    fun testOnConsentButtonClick() {
        viewModel.onConsentButtonClick()
        // TODO doesn't happen here anymore, we don't have a CoronaTest instance to store it with, see QR Code VM
//        verify(exactly = 1) { submissionRepository.giveConsentToSubmission(any()) }
    }

    @Test
    fun testOnDataPrivacyClick() {
        viewModel.onDataPrivacyClick()
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToDataPrivacy
    }

    @Test
    fun testOnBackButtonClick() {
        viewModel.onBackButtonClick()
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToDispatcher
    }

    @Test
    fun testCountryList() {
        viewModel.countries.observeForever { }
        viewModel.countries.value shouldBe countryList
    }

    @Test
    fun `giveGoogleConsentResult when user Allows routes to QR Code scan`() {
        viewModel.giveGoogleConsentResult(true)
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToQRCodeScan
    }

    @Test
    fun `giveGoogleConsentResult when user Doesn't Allow routes to QR Code scan`() {
        viewModel.giveGoogleConsentResult(false)
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToQRCodeScan
    }

    @Test
    fun `onConsentButtonClick sets normal consent and request new Google consent Api`() {
        viewModel.onConsentButtonClick()
        // TODO doesn't happen here anymore, we don't have a CoronaTest instance to store it with, see QR Code VM
//        verify(exactly = 1) { submissionRepository.giveConsentToSubmission(any()) }
        coVerify(exactly = 1) { tekHistoryProvider.preAuthorizeExposureKeyHistory() }
    }

    @Test
    fun `onConsentButtonClick triggers new Google consent window`() {
        val apiException = mockk<ApiException>().apply {
            every { status.hasResolution() } returns true
            every { printStackTrace(any<PrintWriter>()) } just Runs
        }
        coEvery { tekHistoryProvider.preAuthorizeExposureKeyHistory() } throws apiException

        viewModel.onConsentButtonClick()

        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.ResolvePlayServicesException(apiException)
    }

    @Test
    fun `onConsentButtonClick routes to QR scan when unrecoverable Error is thrown`() {
        coEvery { tekHistoryProvider.preAuthorizeExposureKeyHistory() } throws Exception("Unrecoverable Error")

        viewModel.onConsentButtonClick()

        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToQRCodeScan
    }
}
