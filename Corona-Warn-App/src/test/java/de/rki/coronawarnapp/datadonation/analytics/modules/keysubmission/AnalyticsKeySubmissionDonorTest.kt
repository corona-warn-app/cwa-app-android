package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AnalyticsKeySubmissionDonorTest : BaseTest() {
    @MockK lateinit var repository: AnalyticsKeySubmissionRepository
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var configData: ConfigData
    val request = object : DonorModule.Request {
        override val currentConfig: ConfigData
            get() = configData
    }
    private val now = Instant.now()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns now
        every { configData.analytics.hoursSinceTestResultToSubmitKeySubmissionMetadata } returns 6
    }

    @Test
    fun testNoContributionWithoutTestResult() {
        every { repository.testResultReceivedAt } returns -1
        every { repository.submitted } returns false
        runBlockingTest {
            val donor = createInstance()
            donor.beginDonation(request) shouldBe AnalyticsKeySubmissionNoContribution
        }
    }

    @Test
    fun testNoContributionWhenNotSubmitted() {
        every { repository.testResultReceivedAt } returns now.minus(Duration.standardHours(4)).millis
        every { repository.submitted } returns false
        runBlockingTest {
            val donor = createInstance()
            donor.beginDonation(request) shouldBe AnalyticsKeySubmissionNoContribution
        }
    }

    @Test
    fun testSubmitContributionAfterEnoughTimeHasPassed() {
        every { repository.testResultReceivedAt } returns now.minus(Duration.standardHours(4)).millis
        every { repository.submitted } returns true
        every { repository.testResultReceivedAt } returns 1
        val minTimePassedToSubmit = Duration.standardHours(3)
        runBlockingTest {
            val donor = createInstance()
            donor.enoughTimeHasPassedSinceResult(Duration.standardHours(3)) shouldBe true
            donor.shouldSubmitData(minTimePassedToSubmit) shouldBe true
        }
    }

    fun createInstance() = AnalyticsKeySubmissionDonor(repository, timeStamper)
}
