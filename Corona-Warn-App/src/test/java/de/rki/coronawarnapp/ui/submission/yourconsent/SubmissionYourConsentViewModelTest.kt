package de.rki.coronawarnapp.ui.submission.yourconsent

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.ui.Country
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class SubmissionYourConsentViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository

    private val countryList = Country.values().toList()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { interoperabilityRepository.countryListFlow } returns MutableStateFlow(countryList)
        every { submissionRepository.hasGivenConsentToSubmission } returns flowOf(true)
        every { submissionRepository.giveConsentToSubmission() } just Runs
        every { submissionRepository.revokeConsentToSubmission() } just Runs
    }

    private fun createViewModel(): SubmissionYourConsentViewModel = SubmissionYourConsentViewModel(
        interoperabilityRepository = interoperabilityRepository,
        submissionRepository = submissionRepository,
        dispatcherProvider = TestDispatcherProvider
    )

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `country list`() {
        val viewModel = createViewModel()

        viewModel.countryList.observeForever { }
        viewModel.countryList.value shouldBe countryList
    }

    @Test
    fun `go back`() {
        val viewModel = createViewModel()

        viewModel.goBack()
        viewModel.clickEvent.value shouldBe SubmissionYourConsentEvents.GoBack
    }

    @Test
    fun `consent removed`() {
        val viewModel = createViewModel()

        coEvery { submissionRepository.hasGivenConsentToSubmission } returns flowOf(true)
        viewModel.switchConsent()
        verify(exactly = 1) { submissionRepository.revokeConsentToSubmission() }
    }

    @Test
    fun `consent given`() {
        val viewModel = createViewModel()

        coEvery { submissionRepository.hasGivenConsentToSubmission } returns flowOf(false)
        viewModel.switchConsent()
        verify(exactly = 1) { submissionRepository.giveConsentToSubmission() }
    }

    @Test
    fun `consent repository changed`() {
//        val viewModel = createViewModel()
//
//        val consentMutable = MutableStateFlow(false)
//        every { submissionRepository.hasGivenConsentToSubmission } returns flowOf(false)
//
//        viewModel.consent.observeForever { }
//        viewModel.consent.value shouldBe false
//
//        consentMutable.value = false
//        viewModel.consent.value shouldBe false
    }

    @Test
    fun `go to legal page`() {
        val viewModel = createViewModel()

        viewModel.goLegal()
        viewModel.clickEvent.value shouldBe SubmissionYourConsentEvents.GoLegal
    }
}
