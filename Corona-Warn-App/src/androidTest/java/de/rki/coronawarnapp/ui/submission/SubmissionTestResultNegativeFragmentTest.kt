package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.negative.SubmissionTestResultNegativeFragment
import de.rki.coronawarnapp.ui.submission.testresult.negative.SubmissionTestResultNegativeViewModel
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultConsentGivenFragmentArgs
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
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
class SubmissionTestResultNegativeFragmentTest : BaseUITest() {

    lateinit var viewModel: SubmissionTestResultNegativeViewModel
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var certificateRepository: TestCertificateRepository
    @MockK lateinit var testResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @MockK lateinit var testType: CoronaTest.Type
    private val resultNegativeFragmentArgs =
        SubmissionTestResultConsentGivenFragmentArgs(testType = CoronaTest.Type.PCR).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { submissionRepository.testForType(any()) } returns flowOf()
        every { certificateRepository.certificates } returns flowOf()

        viewModel = spyk(
            SubmissionTestResultNegativeViewModel(
                TestDispatcherProvider(),
                submissionRepository,
                certificateRepository,
                testResultAvailableNotificationService,
                testType
            )
        )

        setupMockViewModel(
            object : SubmissionTestResultNegativeViewModel.Factory {
                override fun create(testType: CoronaTest.Type): SubmissionTestResultNegativeViewModel = viewModel
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
        every { viewModel.testResult } returns MutableLiveData(
            SubmissionTestResultNegativeViewModel.UIState(
                coronaTest = mockk<CoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.PCR_NEGATIVE
                    every { registeredAt } returns Instant.now()
                    every { type } returns CoronaTest.Type.PCR
                },
                certificateState = SubmissionTestResultNegativeViewModel.CertificateState.AVAILABLE
            )
        )
        launchFragmentInContainer2<SubmissionTestResultNegativeFragment>(fragmentArgs = resultNegativeFragmentArgs)
        takeScreenshot<SubmissionTestResultNegativeFragment>()
    }
}

@Module
abstract class SubmissionTestResultTestNegativeModule {
    @ContributesAndroidInjector
    abstract fun submissionTestResultScreen(): SubmissionTestResultNegativeFragment
}
