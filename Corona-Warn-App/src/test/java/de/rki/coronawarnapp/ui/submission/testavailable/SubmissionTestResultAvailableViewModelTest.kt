package de.rki.coronawarnapp.ui.submission.testavailable

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableFragmentDirections
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableViewModel
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
class SubmissionTestResultAvailableViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var tekHistoryUpdater: TEKHistoryUpdater
    @MockK lateinit var tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var checkInRepository: CheckInRepository

    private val coronaTestFlow = MutableStateFlow(
        mockk<CoronaTest>().apply {
            every { isAdvancedConsentGiven } returns true
        }
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { tekHistoryUpdaterFactory.create(any()) } returns tekHistoryUpdater
        every { tekHistoryUpdater.updateTEKHistoryOrRequestPermission() } just Runs

        // TODO Check specific behavior
        submissionRepository.apply {
            every { refreshTest(any()) } just Runs
            every { testForType(type = any()) } returns coronaTestFlow
        }
    }

    private fun createViewModel(): SubmissionTestResultAvailableViewModel = SubmissionTestResultAvailableViewModel(
        submissionRepository = submissionRepository,
        dispatcherProvider = TestDispatcherProvider(),
        tekHistoryUpdaterFactory = tekHistoryUpdaterFactory,
        autoSubmission = autoSubmission,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        checkInRepository = checkInRepository
    )

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
    fun `go back`() {
        val viewModel = createViewModel()

        viewModel.showCloseDialog.value shouldBe null
        viewModel.goBack()
        viewModel.showCloseDialog.value shouldBe Unit
    }

    @Test
    fun `go to your consent page`() {
        val viewModel = createViewModel()

        viewModel.goConsent()
        viewModel.routeToScreen.value shouldBe SubmissionTestResultAvailableFragmentDirections
            .actionSubmissionTestResultAvailableFragmentToSubmissionYourConsentFragment(true)
    }

    @Test
    fun `update TEK history if consent is given`() {
        val viewModel = createViewModel()

        viewModel.proceed()
        verify {
            tekHistoryUpdater.updateTEKHistoryOrRequestPermission()
        }
    }

    @Test
    fun `go to test result without updating TEK history if NO consent is given`() {
        coronaTestFlow.value = mockk<CoronaTest>().apply {
            every { isAdvancedConsentGiven } returns false
        }
        every { analyticsKeySubmissionCollector.reportConsentWithdrawn() } just Runs

        val viewModel = createViewModel()

        viewModel.proceed()
        viewModel.routeToScreen.value shouldBe SubmissionTestResultAvailableFragmentDirections
            .actionSubmissionTestResultAvailableFragmentToSubmissionTestResultNoConsentFragment()
    }
}
