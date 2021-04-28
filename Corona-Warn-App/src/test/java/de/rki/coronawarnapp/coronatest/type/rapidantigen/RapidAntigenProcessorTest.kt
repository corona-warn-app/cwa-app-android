package de.rki.coronawarnapp.coronatest.type.rapidantigen

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.values
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RapidAntigenProcessorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var submissionService: CoronaTestService

    private val nowUTC = Instant.parse("2021-03-15T05:45:00.000Z")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns nowUTC

        submissionService.apply {
            coEvery { asyncRequestTestResult(any()) } returns PCR_OR_RAT_PENDING
            coEvery { asyncRegisterDeviceViaGUID(any()) } returns CoronaTestService.RegistrationData(
                registrationToken = "regtoken-qr",
                testResult = PCR_OR_RAT_PENDING,
            )
            coEvery { asyncRegisterDeviceViaTAN(any()) } returns CoronaTestService.RegistrationData(
                registrationToken = "regtoken-tan",
                testResult = PCR_OR_RAT_PENDING,
            )
        }
    }

    fun createInstance() = RapidAntigenProcessor(
        timeStamper = timeStamper,
        submissionService = submissionService,
    )

    @Test
    fun `if we receive a pending result 60 days after registration, we map to REDEEMED`() = runBlockingTest {
        val instance = createInstance()

        val raTest = RACoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = nowUTC,
            registrationToken = "regtoken",
            testResult = RAT_POSITIVE,
            testedAt = Instant.EPOCH,
        )

        instance.pollServer(raTest).testResult shouldBe PCR_OR_RAT_PENDING

        val past60DaysTest = raTest.copy(
            registeredAt = nowUTC.minus(Duration.standardDays(21))
        )

        instance.pollServer(past60DaysTest).testResult shouldBe RAT_REDEEMED
    }

    @Test
    fun `registering a new test maps invalid results to INVALID state`() = runBlockingTest {
        var registrationData = CoronaTestService.RegistrationData(
            registrationToken = "regtoken",
            testResult = PCR_OR_RAT_PENDING,
        )
        coEvery { submissionService.asyncRegisterDeviceViaGUID(any()) } answers { registrationData }

        val instance = createInstance()

        val request = CoronaTestQRCode.RapidAntigen(
            hash = "hash",
            createdAt = Instant.EPOCH,
        )

        values().forEach {
            registrationData = registrationData.copy(testResult = it)
            when (it) {
                PCR_NEGATIVE,
                PCR_POSITIVE,
                PCR_INVALID,
                PCR_REDEEMED -> instance.create(request).testResult shouldBe RAT_INVALID

                PCR_OR_RAT_PENDING,
                RAT_PENDING,
                RAT_NEGATIVE,
                RAT_POSITIVE,
                RAT_INVALID,
                RAT_REDEEMED -> instance.create(request).testResult shouldBe it
            }
        }
    }

    @Test
    fun `polling filters out invalid test result values`() = runBlockingTest {
        var pollResult: CoronaTestResult = PCR_OR_RAT_PENDING
        coEvery { submissionService.asyncRequestTestResult(any()) } answers { pollResult }

        val instance = createInstance()

        val raTest = RACoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = nowUTC,
            registrationToken = "regtoken",
            testResult = RAT_POSITIVE,
            testedAt = Instant.EPOCH,
        )

        values().forEach {
            pollResult = it
            when (it) {
                PCR_NEGATIVE,
                PCR_POSITIVE,
                PCR_INVALID,
                PCR_REDEEMED -> instance.pollServer(raTest).testResult shouldBe RAT_INVALID

                PCR_OR_RAT_PENDING,
                RAT_PENDING,
                RAT_NEGATIVE,
                RAT_POSITIVE,
                RAT_INVALID,
                RAT_REDEEMED -> instance.pollServer(raTest).testResult shouldBe it
            }
        }
    }
}
