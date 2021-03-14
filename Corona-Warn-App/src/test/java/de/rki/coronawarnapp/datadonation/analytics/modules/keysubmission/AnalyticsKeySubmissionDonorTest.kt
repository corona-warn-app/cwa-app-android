package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
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
    private val request = object : DonorModule.Request {
        override val currentConfig: ConfigData
            get() = configData
    }
    private val now = Instant.now()

    @MockK lateinit var ppaData: PpaData.PPADataAndroid.Builder

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns now
        every { configData.analytics.hoursSinceTestResultToSubmitKeySubmissionMetadata } returns 6
    }

    @Test
    fun `no contribution without test result`() {
        every { repository.testResultReceivedAt } returns -1
        every { repository.submitted } returns false
        runBlockingTest {
            val donor = createInstance()
            donor.beginDonation(request) shouldBe AnalyticsKeySubmissionNoContribution
        }
    }

    @Test
    fun `no contribution when neither submitted nor enough time passed`() {
        every { repository.testResultReceivedAt } returns now.minus(Duration.standardHours(4)).millis
        every { repository.submitted } returns false
        runBlockingTest {
            val donor = createInstance()
            donor.beginDonation(request) shouldBe AnalyticsKeySubmissionNoContribution
        }
    }

    @Test
    fun `regular contribution when keys submitted`() {
        every { repository.testResultReceivedAt } returns now.minus(Duration.standardHours(4)).millis
        every { repository.advancedConsentGiven } returns true
        every { repository.daysSinceMostRecentDateAtRiskLevelAtTestRegistration } returns 1
        every { repository.hoursSinceHighRiskWarningAtTestRegistration } returns 1
        every { repository.hoursSinceTestResult } returns 1
        every { repository.hoursSinceTestRegistration } returns 1
        every { repository.lastSubmissionFlowScreen } returns 1
        every { repository.submittedAfterCancel } returns true
        every { repository.submittedAfterSymptomFlow } returns true
        every { repository.submittedInBackground } returns true
        every { repository.submittedWithTeleTAN } returns false
        every { repository.submitted } returns true
        every { ppaData.addKeySubmissionMetadataSet(any<PpaData.PPAKeySubmissionMetadata.Builder>()) } returns ppaData
        every { repository.reset() } just Runs
        runBlockingTest {
            val donor = createInstance()
            val contribution = donor.beginDonation(request)
            contribution.injectData(ppaData)
            coVerify { ppaData.addKeySubmissionMetadataSet(any<PpaData.PPAKeySubmissionMetadata.Builder>()) }
            contribution.finishDonation(false)
            verify(exactly = 0) { repository.reset() }
            contribution.finishDonation(true)
            verify(exactly = 1) { repository.reset() }
        }
    }

    @Test
    fun `submit contribution after enough time has passed`() {
        every { repository.testResultReceivedAt } returns now.minus(Duration.standardHours(4)).millis
        every { repository.submitted } returns true
        val minTimePassedToSubmit = Duration.standardHours(3)
        runBlockingTest {
            val donor = createInstance()
            donor.enoughTimeHasPassedSinceResult(Duration.standardHours(3)) shouldBe true
            donor.shouldSubmitData(minTimePassedToSubmit) shouldBe true
        }
    }

    fun createInstance() = AnalyticsKeySubmissionDonor(repository, timeStamper)
}
