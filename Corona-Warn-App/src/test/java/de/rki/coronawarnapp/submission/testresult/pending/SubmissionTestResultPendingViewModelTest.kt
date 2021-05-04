package de.rki.coronawarnapp.submission.testresult.pending

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingViewModel
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.asDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionTestResultPendingViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var testType: CoronaTest.Type

    private val testFlow = MutableStateFlow<CoronaTest?>(null)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        submissionRepository.apply {
            every { testForType(any()) } returns testFlow
            coEvery { setViewedTestResult(any()) } just Runs
        }
    }

    fun createInstance(scope: CoroutineScope = TestCoroutineScope()) = SubmissionTestResultPendingViewModel(
        dispatcherProvider = scope.asDispatcherProvider(),
        submissionRepository = submissionRepository,
        testType = testType
    )

    @Test
    fun `web exception handling`() {
        val expectedError = CwaWebException(statusCode = 1, message = "message")
        val unexpectedError = UnsupportedOperationException()

        testFlow.value = mockk<CoronaTest>().apply { every { lastError } returns expectedError }

        createInstance().apply {
            cwaWebExceptionLiveData.observeForever {}
            cwaWebExceptionLiveData.value shouldBe expectedError

            testFlow.value = mockk<CoronaTest>().apply { every { lastError } returns unexpectedError }
            cwaWebExceptionLiveData.value shouldBe unexpectedError
        }
    }
}
