package de.rki.coronawarnapp.submission.testresult.pending

import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
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
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.asDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class SubmissionTestResultPendingViewModelTest : BaseTest() {

    @MockK lateinit var recycledTestProvider: RecycledCoronaTestsProvider
    @MockK lateinit var coronaTestProvider: CoronaTestProvider

    private val testFlow = MutableStateFlow<PersonalCoronaTest?>(null)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { coronaTestProvider.refreshTest(any()) } just Runs
        every { coronaTestProvider.getTestForIdentifier(any()) } returns testFlow
    }

    fun createInstance(scope: CoroutineScope = TestScope(), forceInitialUpdate: Boolean = false) =
        SubmissionTestResultPendingViewModel(
            dispatcherProvider = scope.asDispatcherProvider(),
            initialUpdate = forceInitialUpdate,
            recycledTestProvider = recycledTestProvider,
            testIdentifier = "",
            coronaTestProvider = coronaTestProvider
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
    fun `initial update not triggered when not forced`() {
        createInstance().apply {
            coVerify(exactly = 0) { coronaTestProvider.refreshTest(any()) }
        }
    }
}
