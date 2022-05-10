package de.rki.coronawarnapp.ui.submission.yourconsent

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.Country
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionYourConsentViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository
    @MockK lateinit var testType: BaseCoronaTest.Type

    private val countryList = Country.values().toList()

    private val coronaTestFlow = MutableStateFlow(
        mockk<PersonalCoronaTest>().apply {
            every { isAdvancedConsentGiven } returns true
        }
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { submissionRepository.testForType(any()) } returns coronaTestFlow
        every { interoperabilityRepository.countryList } returns MutableStateFlow(countryList)
        coEvery { submissionRepository.giveConsentToSubmission(any()) } just Runs
        coEvery { submissionRepository.revokeConsentToSubmission(any()) } just Runs
    }

    private fun createViewModel(): SubmissionYourConsentViewModel = SubmissionYourConsentViewModel(
        interoperabilityRepository = interoperabilityRepository,
        submissionRepository = submissionRepository,
        dispatcherProvider = TestDispatcherProvider(),
        testType = testType
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
        coronaTestFlow.value = mockk<PersonalCoronaTest>().apply {
            every { isAdvancedConsentGiven } returns true
        }

        createViewModel().switchConsent()
        coVerify(exactly = 1) { submissionRepository.revokeConsentToSubmission(any()) }
    }

    @Test
    fun `consent given`() {
        coronaTestFlow.value = mockk<PersonalCoronaTest>().apply {
            every { isAdvancedConsentGiven } returns false
        }

        createViewModel().switchConsent()
        coVerify(exactly = 1) { submissionRepository.giveConsentToSubmission(any()) }
    }

    @Test
    fun `consent repository changed`() {
        coronaTestFlow.value = mockk<PersonalCoronaTest>().apply {
            every { isAdvancedConsentGiven } returns false
        }

        val viewModel = createViewModel()

        viewModel.consent.observeForever { }
        viewModel.consent.value shouldBe false

        coronaTestFlow.value = mockk<PersonalCoronaTest>().apply {
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
