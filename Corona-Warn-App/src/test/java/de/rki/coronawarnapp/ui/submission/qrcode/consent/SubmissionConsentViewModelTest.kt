package de.rki.coronawarnapp.ui.submission.qrcode.consent

import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
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
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension
import java.io.PrintWriter

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class SubmissionConsentViewModelTest {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository
    @MockK lateinit var tekHistoryProvider: TEKHistoryProvider
    @MockK lateinit var testRegistrationStateProcessor: TestRegistrationStateProcessor

    lateinit var viewModel: SubmissionConsentViewModel

    private val countryList = Country.values().toList()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { interoperabilityRepository.countryList } returns MutableStateFlow(countryList)
        coEvery { submissionRepository.giveConsentToSubmission(any()) } just Runs
        testRegistrationStateProcessor.apply {
            every { state } returns flowOf(TestRegistrationStateProcessor.State.Idle)
            coEvery { startTestRegistration(any(), any(), any()) } returns mockk()
        }

        viewModel = SubmissionConsentViewModel(
            interoperabilityRepository = interoperabilityRepository,
            dispatcherProvider = TestDispatcherProvider(),
            coronaTestQRCode = CoronaTestQRCode.PCR(qrCodeGUID = "qrCodeGUID", rawQrCode = "rawQrCode"),
            allowReplacement = false,
            tekHistoryProvider = tekHistoryProvider,
            registrationStateProcessor = testRegistrationStateProcessor
        )
    }

    @Test
    fun testOnDataPrivacyClick() {
        viewModel.onDataPrivacyClick()
        viewModel.routeToScreen.value shouldBe SubmissionNavigationEvents.NavigateToDataPrivacy
    }

    @Test
    fun testCountryList() {
        viewModel.countries.observeForever { }
        viewModel.countries.value shouldBe countryList
    }

    @Test
    fun `onConsentButtonClick sets normal consent and request new Google consent Api`() {
        viewModel.onConsentButtonClick()
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
}
