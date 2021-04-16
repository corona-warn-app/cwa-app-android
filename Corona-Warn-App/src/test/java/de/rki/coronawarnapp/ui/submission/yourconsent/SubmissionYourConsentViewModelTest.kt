package de.rki.coronawarnapp.ui.submission.yourconsent

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.Country
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val coronaTestFlow = MutableStateFlow(
        mockk<CoronaTest>().apply {
            every { isAdvancedConsentGiven } returns true
        }
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { submissionRepository.testForType(any()) } returns coronaTestFlow
        every { interoperabilityRepository.countryList } returns MutableStateFlow(countryList)
        every { submissionRepository.giveConsentToSubmission(any()) } just Runs
        every { submissionRepository.revokeConsentToSubmission(any()) } just Runs
    }

    private fun createViewModel(): SubmissionYourConsentViewModel = SubmissionYourConsentViewModel(
        interoperabilityRepository = interoperabilityRepository,
        submissionRepository = submissionRepository,
        dispatcherProvider = TestDispatcherProvider()
    )

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
        coronaTestFlow.value = mockk<CoronaTest>().apply {
            every { isAdvancedConsentGiven } returns true
        }

        createViewModel().switchConsent()
        verify(exactly = 1) { submissionRepository.revokeConsentToSubmission(any()) }
    }

    @Test
    fun `consent given`() {
        coronaTestFlow.value = mockk<CoronaTest>().apply {
            every { isAdvancedConsentGiven } returns false
        }

        createViewModel().switchConsent()
        verify(exactly = 1) { submissionRepository.giveConsentToSubmission(any()) }
    }

    @Test
    fun `consent repository changed`() {
        coronaTestFlow.value = mockk<CoronaTest>().apply {
            every { isAdvancedConsentGiven } returns false
        }

        val viewModel = createViewModel()

        viewModel.consent.observeForever { }
        viewModel.consent.value shouldBe false

        coronaTestFlow.value = mockk<CoronaTest>().apply {
            every { isAdvancedConsentGiven } returns true
        }
        viewModel.consent.value shouldBe true
    }

    @Test
    fun `go to legal page`() {
        val viewModel = createViewModel()

        viewModel.goLegal()
        viewModel.clickEvent.value shouldBe SubmissionYourConsentEvents.GoLegal
    }
}
