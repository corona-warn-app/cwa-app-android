package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultKeysSharedFragment
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultKeysSharedFragmentArgs
import de.rki.coronawarnapp.ui.submission.testresult.positive.SubmissionTestResultKeysSharedViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultKeysSharedFragmentTest : BaseUITest() {

    private val submissionTestResultPendingFragmentArgs = SubmissionTestResultKeysSharedFragmentArgs(testIdentifier = "").toBundle()

    @MockK lateinit var viewModel: SubmissionTestResultKeysSharedViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

    }



    @Test
    @Screenshot
    fun capture_fragment() {
        every { viewModel.uiState } returns MutableLiveData(
            TestResultUIState(
                coronaTest = mockk<PersonalCoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.RAT_POSITIVE
                    every { registeredAt } returns Instant.now()
                    every { identifier } returns ""
                    every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
                    every { isProcessing } returns false
                }
            )
        )

        launchFragmentInContainer2<SubmissionTestResultKeysSharedFragment>(
            fragmentArgs = submissionTestResultPendingFragmentArgs
        )
        takeScreenshot<SubmissionTestResultKeysSharedFragment>()
    }
}
