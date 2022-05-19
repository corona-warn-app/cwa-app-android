package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.RegistrationData
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestServiceTest : BaseTest() {

    private val tan = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"

    @MockK lateinit var mockPlaybook: Playbook
    @MockK lateinit var appComponent: ApplicationComponent
    @MockK lateinit var noiseScheduler: NoiseScheduler

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent
        every { appComponent.playbook } returns mockPlaybook

        coEvery {
            mockPlaybook.initialRegistration(any())
        } returns RegistrationData(
            registrationToken = registrationToken,
            testResultResponse = CoronaTestResultResponse(
                coronaTestResult = CoronaTestResult.PCR_OR_RAT_PENDING,
                sampleCollectedAt = null,
                labId = null,
            )
        )
    }

    private fun createInstance() = CoronaTestService(
        playbook = mockPlaybook,
        noiseScheduler = noiseScheduler
    )

    @Test
    fun registrationWithGUIDSucceeds() = runTest {
        val request = RegistrationRequest(
            key = guid,
            type = VerificationKeyType.GUID,
        )
        createInstance().registerTest(request)

        coVerify(exactly = 1) {
            mockPlaybook.initialRegistration(request)
        }
    }

    @Test
    fun registrationWithTeleTANSucceeds() = runTest {
        val request = RegistrationRequest(
            key = tan,
            type = VerificationKeyType.TELETAN,
        )
        createInstance().registerTest(request)

        coVerify(exactly = 1) {
            mockPlaybook.initialRegistration(request)
        }
    }

    @Test
    fun requestTestResultSucceeds() {
        coEvery { mockPlaybook.testResult(registrationToken) } returns CoronaTestResultResponse(
            coronaTestResult = CoronaTestResult.PCR_NEGATIVE,
            sampleCollectedAt = null,
            labId = null,
        )

        runTest {
            createInstance().checkTestResult(registrationToken) shouldBe CoronaTestResultResponse(
                coronaTestResult = CoronaTestResult.PCR_NEGATIVE,
                sampleCollectedAt = null,
                labId = null,
            )
        }
        coVerify(exactly = 1) {
            mockPlaybook.testResult(registrationToken)
        }
    }
}
