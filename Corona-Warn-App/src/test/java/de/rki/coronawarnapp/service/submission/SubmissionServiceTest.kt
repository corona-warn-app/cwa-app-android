package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.deniability.NoiseScheduler
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SubmissionServiceTest : BaseTest() {

    private val tan = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"

    @MockK lateinit var mockPlaybook: Playbook
    @MockK lateinit var appComponent: ApplicationComponent
    @MockK lateinit var noiseScheduler: NoiseScheduler

    lateinit var submissionService: CoronaTestService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent
        every { appComponent.playbook } returns mockPlaybook

        submissionService = CoronaTestService(
            playbook = mockPlaybook,
            noiseScheduler = noiseScheduler
        )
    }

    @Test
    fun registrationWithGUIDSucceeds() {
        coEvery {
            mockPlaybook.initialRegistration(guid, VerificationKeyType.GUID)
        } returns (registrationToken to CoronaTestResult.PCR_OR_RAT_PENDING)

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
        } returns (registrationToken to CoronaTestResult.PCR_OR_RAT_PENDING)

        runBlocking {
            submissionService.asyncRegisterDeviceViaTAN(tan)
        }

        coVerify(exactly = 1) {
            mockPlaybook.initialRegistration(tan, VerificationKeyType.TELETAN)
        }
    }

    @Test
    fun requestTestResultSucceeds() {
        coEvery { mockPlaybook.testResult(registrationToken) } returns CoronaTestResult.PCR_NEGATIVE

        runBlocking {
            submissionService.asyncRequestTestResult(registrationToken) shouldBe CoronaTestResult.PCR_NEGATIVE
        }
        coVerify(exactly = 1) {
            mockPlaybook.testResult(registrationToken)
        }
    }
}
