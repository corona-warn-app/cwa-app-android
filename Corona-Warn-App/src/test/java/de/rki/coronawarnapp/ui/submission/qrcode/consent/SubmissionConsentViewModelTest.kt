package de.rki.coronawarnapp.ui.submission.qrcode.consent

import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionConsentViewModelTest {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository

    lateinit var viewModel: SubmissionConsentViewModel

    private val countryList = Country.values().toList()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { interoperabilityRepository.countryList } returns MutableStateFlow(countryList)
        every { submissionRepository.giveConsentToSubmission() } just Runs
        viewModel = SubmissionConsentViewModel(
            submissionRepository,
            interoperabilityRepository,
            dispatcherProvider = TestDispatcherProvider
        )
    }

    @Test
    fun testOnConsentButtonClick() {
        viewModel.onConsentButtonClick()
        verify(exactly = 1) { submissionRepository.giveConsentToSubmission() }
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
}
