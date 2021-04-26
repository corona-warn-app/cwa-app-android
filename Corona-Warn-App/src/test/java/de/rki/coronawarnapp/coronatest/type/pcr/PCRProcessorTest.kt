package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest.TestResultDataCollector
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PCRProcessorTest : BaseTest() {
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var submissionService: CoronaTestService
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var testResultDataCollector: TestResultDataCollector
    @MockK lateinit var deadmanNotificationScheduler: DeadmanNotificationScheduler

    private val nowUTC = Instant.parse("2021-03-15T05:45:00.000Z")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns nowUTC

        submissionService.apply {
            coEvery { asyncRequestTestResult(any()) } answers { CoronaTestResult.PCR_OR_RAT_PENDING }
        }

        testResultDataCollector.apply {
            coEvery { updatePendingTestResultReceivedTime(any()) } just Runs
        }
    }

    fun createInstance() = PCRProcessor(
        timeStamper = timeStamper,
        submissionService = submissionService,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        testResultDataCollector = testResultDataCollector,
        deadmanNotificationScheduler = deadmanNotificationScheduler,
    )

    @Test
    fun `if we receive a pending result 60 days after registration, we map to REDEEMED`() = runBlockingTest {
        val instance = createInstance()

        val pcrTest = PCRCoronaTest(
            identifier = "identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = nowUTC,
            registrationToken = "regtoken",
            testResult = CoronaTestResult.PCR_POSITIVE
        )

        instance.pollServer(pcrTest).testResult shouldBe CoronaTestResult.PCR_OR_RAT_PENDING

        val past60DaysTest = pcrTest.copy(
            registeredAt = nowUTC.minus(Duration.standardDays(21))
        )

        instance.pollServer(past60DaysTest).testResult shouldBe CoronaTestResult.PCR_REDEEMED
    }
}
