package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.testresult.negative.SubmissionTestResultNegativeFragment
import de.rki.coronawarnapp.ui.submission.testresult.negative.SubmissionTestResultNegativeViewModel
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingFragment
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.util.Date

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultNegativeFragmentTest : BaseUITest() {

    lateinit var viewModel: SubmissionTestResultNegativeViewModel
    @MockK lateinit var uiState: TestResultUIState
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var dispatcherProvider: DispatcherProvider

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { submissionRepository.deviceUIStateFlow } returns flow { emit(NetworkRequestWrapper.RequestIdle) }
        every { submissionRepository.testResultReceivedDateFlow } returns flow { }
        every { dispatcherProvider.Default } returns Dispatchers.Default

        viewModel = spyk(SubmissionTestResultNegativeViewModel(
            dispatcherProvider,
            submissionRepository
        ))

        setupMockViewModel(object : SubmissionTestResultNegativeViewModel.Factory {
            override fun create(): SubmissionTestResultNegativeViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    @Screenshot
    fun capture_negative_fragment() {
        every { viewModel.testResult } returns MutableLiveData(
            TestResultUIState(
                NetworkRequestWrapper.RequestSuccessful(
                    DeviceUIState.PAIRED_NEGATIVE
                ), Date()
            )
        )
        captureScreenshot<SubmissionTestResultNegativeFragment>()
    }
}

@Module
abstract class SubmissionTestResultTestNegativeModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultNegativeFragment
}
