package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater_Factory_Impl
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableFragment
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableFragmentArgs
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableViewModel
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultAvailableFragmentTest : BaseUITest() {

    lateinit var viewModel: SubmissionTestResultAvailableViewModel
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var tekHistoryUpdaterFactory: TEKHistoryUpdater_Factory_Impl
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var appShortcutsHelper: AppShortcutsHelper
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var testType: BaseCoronaTest.Type
    @MockK lateinit var coronaTestProvider: CoronaTestProvider
    private val resultAvailableFragmentArgs =
        SubmissionTestResultAvailableFragmentArgs(testIdentifier = "").toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { submissionRepository.testForType(any()) } returns flowOf()
        every { appShortcutsHelper.disableAllShortcuts() } just Runs

        viewModel = spyk(
            SubmissionTestResultAvailableViewModel(
                dispatcherProvider = TestDispatcherProvider(),
                tekHistoryUpdaterFactory = tekHistoryUpdaterFactory,
                autoSubmission = autoSubmission,
                analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
                checkInRepository = checkInRepository,
                coronaTestProvider = coronaTestProvider,
                testIdentifier = ""
            )
        )

        setupMockViewModel(
            object : SubmissionTestResultAvailableViewModel.Factory {
                override fun create(testIdentifier: TestIdentifier): SubmissionTestResultAvailableViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    @Screenshot
    fun capture_fragment_with_consent() {
        every { viewModel.consent } returns MutableLiveData(true)
        launchFragmentInContainer2<SubmissionTestResultAvailableFragment>(
            fragmentArgs = resultAvailableFragmentArgs
        )
        takeScreenshot<SubmissionTestResultAvailableFragment>(suffix = "_consent")
    }

    @Test
    @Screenshot
    fun capture_fragment_without_consent() {
        every { viewModel.consent } returns MutableLiveData(false)
        launchFragmentInContainer2<SubmissionTestResultAvailableFragment>(
            fragmentArgs = resultAvailableFragmentArgs
        )
        takeScreenshot<SubmissionTestResultAvailableFragment>(suffix = "_no_consent")
    }
}

@Module
abstract class SubmissionTestResultTestAvailableModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultAvailableFragment
}
