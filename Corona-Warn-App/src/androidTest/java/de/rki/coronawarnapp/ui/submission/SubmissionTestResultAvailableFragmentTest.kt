package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater_AssistedFactory
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableFragment
import de.rki.coronawarnapp.ui.submission.resultavailable.SubmissionTestResultAvailableViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.captureScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultAvailableFragmentTest : BaseUITest() {

    lateinit var viewModel: SubmissionTestResultAvailableViewModel
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var tekHistoryUpdaterFactory: TEKHistoryUpdater_AssistedFactory
    @MockK lateinit var autoSubmission: AutoSubmission

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { submissionRepository.deviceUIStateFlow } returns flowOf()
        every { submissionRepository.testResultReceivedDateFlow } returns flowOf()

        viewModel = spyk(
            SubmissionTestResultAvailableViewModel(
                TestDispatcherProvider,
                tekHistoryUpdaterFactory,
                submissionRepository,
                autoSubmission
            )
        )

        setupMockViewModel(object : SubmissionTestResultAvailableViewModel.Factory {
            override fun create(): SubmissionTestResultAvailableViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    @Screenshot
    fun capture_fragment_with_consent() {
        every { viewModel.consent } returns MutableLiveData(true)
        captureScreenshot<SubmissionTestResultAvailableFragment>("_consent")
    }

    @Test
    @Screenshot
    fun capture_fragment_without_consent() {
        every { viewModel.consent } returns MutableLiveData(false)
        captureScreenshot<SubmissionTestResultAvailableFragment>("_no_consent")
    }
}

@Module
abstract class SubmissionTestResultTestAvailableModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultAvailableFragment
}
