package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.exception.http.BadRequestException
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TestRegistrationStateProcessorTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var request: TestRegistrationRequest
    @MockK lateinit var registeredTest: CoronaTest

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        submissionRepository.apply {
            coEvery { registerTest(any()) } returns registeredTest
            coEvery { tryReplaceTest(any()) } returns registeredTest
            coEvery { giveConsentToSubmission(any()) } just Runs
        }

        registeredTest.apply {
            every { type } returns CoronaTest.Type.RAPID_ANTIGEN
        }

        request.apply {
            every { type } returns CoronaTest.Type.RAPID_ANTIGEN
        }
    }

    private fun createInstance() = TestRegistrationStateProcessor(
        submissionRepository = submissionRepository
    )

    @Test
    fun `register new test - with consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = request,
            isSubmissionConsentGiven = true,
            allowReplacement = false
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTest
        )

        coVerify {
            submissionRepository.registerTest(request)
            submissionRepository.giveConsentToSubmission(CoronaTest.Type.RAPID_ANTIGEN)
        }
    }

    @Test
    fun `register new test - without consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = request,
            isSubmissionConsentGiven = false,
            allowReplacement = false
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTest
        )

        coVerify {
            submissionRepository.registerTest(request)
        }
        coVerify(exactly = 0) {
            submissionRepository.giveConsentToSubmission(any())
        }
    }

    @Test
    fun `replace test - with consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = request,
            isSubmissionConsentGiven = true,
            allowReplacement = true
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTest
        )

        coVerify {
            submissionRepository.tryReplaceTest(request)
            submissionRepository.giveConsentToSubmission(CoronaTest.Type.RAPID_ANTIGEN)
        }
    }

    @Test
    fun `replace new test - without consent`() = runBlockingTest {
        val instance = createInstance()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = request,
            isSubmissionConsentGiven = false,
            allowReplacement = true
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.TestRegistered(
            test = registeredTest
        )

        coVerify {
            submissionRepository.tryReplaceTest(request)
        }
        coVerify(exactly = 0) {
            submissionRepository.giveConsentToSubmission(any())
        }
    }

    @Test
    fun `errors are mapped to state`() = runBlockingTest {
        val instance = createInstance()

        val expectedException = BadRequestException("")
        coEvery { submissionRepository.registerTest(any()) } throws expectedException

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Idle

        instance.startRegistration(
            request = request,
            isSubmissionConsentGiven = true,
            allowReplacement = false
        )

        advanceUntilIdle()

        instance.state.first() shouldBe TestRegistrationStateProcessor.State.Error(
            exception = expectedException
        )

        coVerify {
            submissionRepository.registerTest(request)
        }
        coVerify(exactly = 0) {
            submissionRepository.giveConsentToSubmission(any())
        }
    }
}
