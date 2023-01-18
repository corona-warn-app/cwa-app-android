package de.rki.coronawarnapp.ui.submission.testavailable

import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableFragmentDirections
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableViewModel
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionTestResultAvailableViewModelTest : BaseTest() {

    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var tekHistoryUpdater: TEKHistoryUpdater
    @MockK lateinit var tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var coronaTestProvider: CoronaTestProvider

    private val coronaTestFlow = MutableStateFlow(
        mockk<PersonalCoronaTest>().apply {
            every { isAdvancedConsentGiven } returns true
        }
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { tekHistoryUpdaterFactory.create(any()) } returns tekHistoryUpdater
        every { tekHistoryUpdater.getTeksOrRequestPermission() } just Runs

        // TODO Check specific behavior
        coronaTestProvider.apply {
            coEvery { refreshTest(any()) } just Runs
            coEvery { setTestAsViewed(any()) } just Runs
            every { getTestForIdentifier(testIdentifier = any()) } returns coronaTestFlow
        }
    }

    private fun createViewModel(): SubmissionTestResultAvailableViewModel = SubmissionTestResultAvailableViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        tekHistoryUpdaterFactory = tekHistoryUpdaterFactory,
        autoSubmission = autoSubmission,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        checkInRepository = checkInRepository,
        coronaTestProvider = coronaTestProvider,
        testIdentifier = "",
        comesFromDispatcherFragment = false
    )

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
    fun `go back`() {
        val viewModel = createViewModel()

        viewModel.showCloseDialog.value shouldBe null
        viewModel.goBack()
        viewModel.showCloseDialog.value shouldBe Unit
    }

    @Test
    fun `go to your consent page`() = runTest {
        val viewModel = createViewModel()
        coronaTestFlow.value = mockk<PersonalCoronaTest>().apply {
            every { identifier } returns ""
            every { type } returns PCR
        }
        viewModel.goConsent()
        viewModel.routeToScreen.value shouldBe SubmissionTestResultAvailableFragmentDirections
            .actionSubmissionTestResultAvailableFragmentToSubmissionYourConsentFragment(
                testType = coronaTestFlow.first().type,
                isTestResultAvailable = true,
            )
    }

    @Test
    fun `update TEK history if consent is given`() {
        val viewModel = createViewModel()

        viewModel.proceed()
        verify {
            tekHistoryUpdater.getTeksOrRequestPermission()
        }
    }

    @Test
    fun `go to test result without updating TEK history if NO consent is given`() = runTest {
        coronaTestFlow.value = mockk<PersonalCoronaTest>().apply {
            every { isAdvancedConsentGiven } returns false
            every { identifier } returns ""
            every { type } returns PCR
        }
        coEvery { analyticsKeySubmissionCollector.reportConsentWithdrawn(any()) } just Runs

        val viewModel = createViewModel()

        viewModel.proceed()
        viewModel.routeToScreen.value shouldBe SubmissionTestResultAvailableFragmentDirections
            .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultNoConsentFragment(
                coronaTestFlow.first().identifier
            )
    }

    @Test
    fun `proceed() should call analyticsKeySubmissionCollector for PCR tests`() {
        coronaTestFlow.value = mockk<PersonalCoronaTest>().apply {
            every { isAdvancedConsentGiven } returns false
            every { type } returns PCR
        }

        createViewModel().proceed()

        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportConsentWithdrawn(PCR) }
        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportConsentWithdrawn(RAPID_ANTIGEN) }
    }

    @Test
    fun `proceed() should call analyticsKeySubmissionCollector for RAT tests`() {
        coronaTestFlow.value = mockk<PersonalCoronaTest>().apply {
            every { isAdvancedConsentGiven } returns false
            every { type } returns RAPID_ANTIGEN
        }

        createViewModel().proceed()

        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportConsentWithdrawn(PCR) }
        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportConsentWithdrawn(RAPID_ANTIGEN) }
    }
}
