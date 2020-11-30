package de.rki.coronawarnapp.ui.submission.yourconsent

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
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionYourConsentViewModelTest {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository

    lateinit var viewModel: SubmissionYourConsentViewModel

    private val countryList = Country.values().toList()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { interoperabilityRepository.countryListFlow } returns MutableStateFlow(countryList)
        every { submissionRepository.hasGivenConsentToSubmission } returns flowOf(true)
        every { submissionRepository.giveConsentToSubmission() } just Runs
        every { submissionRepository.revokeConsentToSubmission() } just Runs
        viewModel = SubmissionYourConsentViewModel(interoperabilityRepository, submissionRepository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun testCountryList() {
        viewModel.countryList.observeForever { }
        viewModel.countryList.value shouldBe countryList
    }

    @Test
    fun testGoBack() {
        viewModel.goBack()
        viewModel.clickEvent.value shouldBe SubmissionYourConsentEvents.GoBack
    }

    @Test
    fun testConsentRevoke() {
        coEvery { submissionRepository.hasGivenConsentToSubmission } returns flowOf(true)
        viewModel.switchConsent()
        verify(exactly = 1) { submissionRepository.revokeConsentToSubmission() }
        viewModel.clickEvent.value shouldBe SubmissionYourConsentEvents.SwitchConsent
    }

    @Test
    fun testConsentGiven() {
        coEvery { submissionRepository.hasGivenConsentToSubmission } returns flowOf(false)
        viewModel = SubmissionYourConsentViewModel(interoperabilityRepository, submissionRepository)
        viewModel.switchConsent()
        verify(exactly = 1) { submissionRepository.giveConsentToSubmission() }
        viewModel.clickEvent.value shouldBe SubmissionYourConsentEvents.SwitchConsent
    }

    @Test
    fun testGoLegal() {
        viewModel.goLegal()
        viewModel.clickEvent.value shouldBe SubmissionYourConsentEvents.GoLegal
    }
}
