package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenFragmentArgs
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultNoConsentFragment
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultNoConsentViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import org.joda.time.Instant
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
class SubmissionTestResultNoConsentGivenFragmentTest : BaseUITest() {

    @MockK lateinit var testResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var coronaTestProvider: CoronaTestProvider
    private val noConsentGivenFragmentArgs =
        SubmissionTestResultConsentGivenFragmentArgs(testType = BaseCoronaTest.Type.PCR).toBundle()

    private lateinit var viewModel: SubmissionTestResultNoConsentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel =
            spyk(
                SubmissionTestResultNoConsentViewModel(
                    TestDispatcherProvider(),
                    testResultAvailableNotificationService,
                    analyticsKeySubmissionCollector,
                    coronaTestProvider = coronaTestProvider,
                    testIdentifier = ""
                )
            )
        setupMockViewModel(
            object : SubmissionTestResultNoConsentViewModel.Factory {
                override fun create(testIdentifier: TestIdentifier): SubmissionTestResultNoConsentViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        every { viewModel.uiState } returns MutableLiveData(
            TestResultUIState(
                coronaTest = mockk<PersonalCoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.PCR_POSITIVE
                    every { registeredAt } returns Instant.now()
                    every { type } returns BaseCoronaTest.Type.PCR
                }
            )
        )

        launchFragmentInContainer2<SubmissionTestResultNoConsentFragment>(fragmentArgs = noConsentGivenFragmentArgs)
        takeScreenshot<SubmissionTestResultNoConsentFragment>()
    }
}

@Module
abstract class SubmissionTestResultNoConsentModel {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultNoConsentFragment
}
