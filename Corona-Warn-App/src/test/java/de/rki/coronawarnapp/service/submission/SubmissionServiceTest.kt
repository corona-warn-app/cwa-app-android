package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.verification.server.VerificationKeyType
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubmissionServiceTest {

    private val tan = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"

    @MockK lateinit var mockPlaybook: Playbook
    @MockK lateinit var appComponent: ApplicationComponent

    lateinit var submissionService: SubmissionService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent
        every { appComponent.playbook } returns mockPlaybook

        submissionService = SubmissionService(mockPlaybook)
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun registrationWithGUIDSucceeds() {
        coEvery {
            mockPlaybook.initialRegistration(guid, VerificationKeyType.GUID)
        } returns (registrationToken to TestResult.PENDING)

        runBlocking {
            submissionService.asyncRegisterDeviceViaGUID(guid)
        }

        coVerify(exactly = 1) {
            mockPlaybook.initialRegistration(guid, VerificationKeyType.GUID)
        }
    }

    @Test
    fun registrationWithTeleTANSucceeds() {
        coEvery {
            mockPlaybook.initialRegistration(any(), VerificationKeyType.TELETAN)
        } returns (registrationToken to TestResult.PENDING)

        runBlocking {
            submissionService.asyncRegisterDeviceViaTAN(tan)
        }

        coVerify(exactly = 1) {
            mockPlaybook.initialRegistration(tan, VerificationKeyType.TELETAN)
        }
    }

    @Test
    fun requestTestResultSucceeds() {
        coEvery { mockPlaybook.testResult(registrationToken) } returns TestResult.NEGATIVE

        runBlocking {
            submissionService.asyncRequestTestResult(registrationToken) shouldBe TestResult.NEGATIVE
        }
        coVerify(exactly = 1) {
            mockPlaybook.testResult(registrationToken)
        }
    }
}
