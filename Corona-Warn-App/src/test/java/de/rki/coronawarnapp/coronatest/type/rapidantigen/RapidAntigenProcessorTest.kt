package de.rki.coronawarnapp.coronatest.type.rapidantigen

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
import io.kotest.matchers.types.instanceOf
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
import timber.log.Timber

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
                PCR_REDEEMED -> {
                    Timber.v("Should throw for $it")
                    instance.pollServer(raTest).testResult shouldBe RAT_POSITIVE
                    instance.pollServer(raTest).lastError shouldBe instanceOf(IllegalArgumentException::class)
                }
                PCR_OR_RAT_PENDING,
                RAT_PENDING,
                RAT_NEGATIVE,
                RAT_POSITIVE,
                RAT_INVALID,
                RAT_REDEEMED -> {
                    Timber.v("Should NOT throw for $it")
                    instance.pollServer(raTest).testResult shouldBe it
                }
            }
        }
    }
}
