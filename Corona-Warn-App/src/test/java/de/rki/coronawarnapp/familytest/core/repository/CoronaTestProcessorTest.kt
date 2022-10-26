package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.RegistrationData
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.time.Instant

class CoronaTestProcessorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var coronaTestService: CoronaTestService

    private val nowUTC = Instant.parse("2021-03-15T05:45:00.000Z")
    private val test = CoronaTest(
        type = BaseCoronaTest.Type.PCR,
        identifier = "identifier",
        registeredAt = nowUTC,
        registrationToken = "regtoken",
        testResult = CoronaTestResult.PCR_OR_RAT_PENDING
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns nowUTC

        coronaTestService.apply {
            coEvery { checkTestResult(any()) } returns CoronaTestResultResponse(
                coronaTestResult = CoronaTestResult.PCR_OR_RAT_PENDING,
                sampleCollectedAt = null,
                labId = null,
            )
            coEvery { registerTest(any()) } answers {
                val request = firstArg<RegistrationRequest>()

                RegistrationData(
                    registrationToken = "regtoken-${request.type}",
                    testResultResponse = CoronaTestResultResponse(
                        coronaTestResult = CoronaTestResult.PCR_OR_RAT_PENDING,
                        sampleCollectedAt = null,
                        labId = null,
                    ),
                )
            }
        }
    }

    @Test
    fun `registering a new test works`() = runTest {
        val qrCode = CoronaTestQRCode.PCR(
            qrCodeGUID = "guid",
            rawQrCode = "rawQrCode"
        )

        createInstance().register(qrCode)

        val request = RegistrationRequest(
            key = qrCode.registrationIdentifier,
            dateOfBirthKey = null,
            type = VerificationKeyType.GUID,
        )

        coVerify {
            coronaTestService.registerTest(request)
        }
    }

    @Test
    fun `polling works`() = runTest {
        val test = CoronaTest(
            identifier = "familyTest1",
            type = BaseCoronaTest.Type.PCR,
            registeredAt = nowUTC,
            registrationToken = "registrationToken1"
        )

        createInstance().pollServer(test)

        coVerify {
            coronaTestService.checkTestResult("registrationToken1")
        }
    }

    @Test
    fun `registering a new test maps invalid results to INVALID state`() = runTest {

        var registrationData = RegistrationData(
            registrationToken = "regtoken",
            testResultResponse = CoronaTestResultResponse(
                coronaTestResult = CoronaTestResult.PCR_OR_RAT_PENDING,
                sampleCollectedAt = null,
                labId = null,
            )
        )
        coEvery { coronaTestService.registerTest(any()) } answers { registrationData }
        val instance = createInstance()

        val request = CoronaTestQRCode.PCR(
            qrCodeGUID = "guid",
            rawQrCode = "rawQrCode"
        )

        CoronaTestResult.values().forEach {
            registrationData = registrationData.copy(
                testResultResponse = CoronaTestResultResponse(
                    coronaTestResult = it,
                    sampleCollectedAt = null,
                    labId = null,
                )
            )
            when (it) {
                CoronaTestResult.PCR_OR_RAT_PENDING,
                CoronaTestResult.PCR_NEGATIVE,
                CoronaTestResult.PCR_POSITIVE,
                CoronaTestResult.PCR_INVALID,
                CoronaTestResult.PCR_OR_RAT_REDEEMED -> instance.register(request).testResult shouldBe it

                CoronaTestResult.RAT_PENDING,
                CoronaTestResult.RAT_NEGATIVE,
                CoronaTestResult.RAT_POSITIVE,
                CoronaTestResult.RAT_INVALID,
                CoronaTestResult.RAT_REDEEMED ->
                    instance.register(request).testResult shouldBe CoronaTestResult.PCR_INVALID
            }
        }
    }

    @Test
    fun `polling maps invalid results to INVALID state`() = runTest {
        var pollResult: CoronaTestResult = CoronaTestResult.PCR_OR_RAT_PENDING
        coEvery { coronaTestService.checkTestResult(any()) } answers {
            CoronaTestResultResponse(
                coronaTestResult = pollResult,
                sampleCollectedAt = null,
                labId = null,
            )
        }

        val instance = createInstance()

        val pcrTest = test.copy(
            testResult = CoronaTestResult.PCR_POSITIVE,
        )

        CoronaTestResult.values().forEach {
            pollResult = it
            when (it) {
                CoronaTestResult.PCR_OR_RAT_PENDING,
                CoronaTestResult.PCR_NEGATIVE,
                CoronaTestResult.PCR_POSITIVE,
                CoronaTestResult.PCR_INVALID,
                CoronaTestResult.PCR_OR_RAT_REDEEMED -> {
                    Timber.v("Should NOT throw for $it")
                    val result = instance.pollServer(pcrTest) as
                        CoronaTestProcessor.ServerResponse.CoronaTestResultUpdate
                    result.coronaTestResult shouldBe it
                }
                CoronaTestResult.RAT_PENDING,
                CoronaTestResult.RAT_NEGATIVE,
                CoronaTestResult.RAT_POSITIVE,
                CoronaTestResult.RAT_INVALID,
                CoronaTestResult.RAT_REDEEMED -> {
                    Timber.v("Should throw for $it")
                    val result = instance.pollServer(pcrTest) as
                        CoronaTestProcessor.ServerResponse.CoronaTestResultUpdate
                    result.coronaTestResult shouldBe CoronaTestResult.PCR_INVALID
                }
            }
        }
    }

    fun createInstance() = CoronaTestProcessor(
        timeStamper = timeStamper,
        coronaTestService = coronaTestService,
    )
}
