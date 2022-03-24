package de.rki.coronawarnapp.submission.testresult.pending

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingViewModel
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
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
    @MockK lateinit var testType: BaseCoronaTest.Type
    @MockK lateinit var recycledTestProvider: RecycledCoronaTestsProvider

    private val testFlow = MutableStateFlow<PersonalCoronaTest?>(null)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        submissionRepository.apply {
            every { testForType(any()) } returns testFlow
            coEvery { setViewedTestResult(any()) } just Runs
        }
    }

    fun createInstance(scope: CoroutineScope = TestCoroutineScope(), forceInitialUpdate: Boolean = false) =
        SubmissionTestResultPendingViewModel(
            dispatcherProvider = scope.asDispatcherProvider(),
            submissionRepository = submissionRepository,
            testType = testType,
            initialUpdate = forceInitialUpdate,
            recycledTestProvider = recycledTestProvider,
            testIdentifier = ""
        )

    @Test
    fun `web exception handling`() {
        val expectedError = CwaWebException(statusCode = 1, message = "message")
        val unexpectedError = UnsupportedOperationException()

        testFlow.value = mockk<PersonalCoronaTest>().apply { every { lastError } returns expectedError }

        createInstance().apply {
            cwaWebExceptionLiveData.observeForever {}
            cwaWebExceptionLiveData.value shouldBe expectedError

            testFlow.value = mockk<PersonalCoronaTest>().apply { every { lastError } returns unexpectedError }
            cwaWebExceptionLiveData.value shouldBe unexpectedError
        }
    }

    @Test
    fun `initial update triggered when forced`() {
        createInstance(forceInitialUpdate = true).apply {
            coVerify(exactly = 1) { submissionRepository.refreshTest(any()) }
        }
    }

    @Test
    fun `initial update not triggered when not forced`() {
        createInstance().apply {
            coVerify(exactly = 0) { submissionRepository.refreshTest(any()) }
        }
    }
}
