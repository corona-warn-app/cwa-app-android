package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2

class AnalyticsPCRKeySubmissionDonorTest : BaseTest() {
    @MockK lateinit var repository: AnalyticsPCRKeySubmissionRepository
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
        coEvery { timeStamper.nowUTC } returns now
        coEvery { configData.analytics.hoursSinceTestResultToSubmitKeySubmissionMetadata } returns 6
    }

    @Test
    fun `no contribution without test result`() = runTest2 {
        coEvery { repository.testResultReceivedAt() } returns -1
        coEvery { repository.submitted() } returns false

        val donor = createInstance()
        donor.beginDonation(request) shouldBe AnalyticsKeySubmissionNoContribution
    }

    @Test
    fun `no contribution when neither submitted nor enough time passed`() = runTest2 {
        coEvery { repository.testResultReceivedAt() } returns now.minus(Duration.ofHours(4)).toEpochMilli()
        coEvery { repository.submitted() } returns false

        val donor = createInstance()
        donor.beginDonation(request) shouldBe AnalyticsKeySubmissionNoContribution
    }

    @Test
    fun `regular contribution when keys submitted`() = runTest2 {
        coEvery { repository.testResultReceivedAt() } returns now.minus(Duration.ofHours(4)).toEpochMilli()
        coEvery { repository.advancedConsentGiven() } returns true
        coEvery { repository.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration() } returns 1
        coEvery { repository.ewHoursSinceHighRiskWarningAtTestRegistration() } returns 1
        coEvery { repository.hoursSinceTestResult() } returns 1
        coEvery { repository.hoursSinceTestRegistration() } returns 1
        coEvery { repository.lastSubmissionFlowScreen() } returns 1
        coEvery { repository.submittedAfterCancel() } returns true
        coEvery { repository.submittedAfterSymptomFlow() } returns true
        coEvery { repository.submittedInBackground() } returns true
        coEvery { repository.submittedWithTeleTAN() } returns false
        coEvery { repository.submitted() } returns true
        coEvery { repository.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration() } returns 1
        coEvery { repository.ptHoursSinceHighRiskWarningAtTestRegistration() } returns 1
        coEvery { repository.submittedAfterRAT } returns false
        coEvery { repository.submittedWithCheckIns() } returns false
        coEvery { ppaData.addKeySubmissionMetadataSet(any<PpaData.PPAKeySubmissionMetadata.Builder>()) } returns ppaData
        coEvery { repository.reset() } just Runs

        val donor = createInstance()
        val contribution = donor.beginDonation(request)
        contribution.injectData(ppaData)
        coVerify { ppaData.addKeySubmissionMetadataSet(any<PpaData.PPAKeySubmissionMetadata.Builder>()) }
        contribution.finishDonation(false)
        coVerify(exactly = 0) { repository.reset() }
        contribution.finishDonation(true)
        coVerify(exactly = 1) { repository.reset() }
    }

    @Test
    fun `submit contribution after enough time has passed`() = runTest2 {
        coEvery { repository.testResultReceivedAt() } returns now.minus(Duration.ofHours(4)).toEpochMilli()
        coEvery { repository.submitted() } returns true
        val minTimePassedToSubmit = Duration.ofHours(3)

        val donor = createInstance()
        donor.enoughTimeHasPassedSinceResult(Duration.ofHours(3)) shouldBe true
        donor.shouldSubmitData(minTimePassedToSubmit) shouldBe true
    }

    fun createInstance() = AnalyticsPCRKeySubmissionDonor(repository, timeStamper)
}
