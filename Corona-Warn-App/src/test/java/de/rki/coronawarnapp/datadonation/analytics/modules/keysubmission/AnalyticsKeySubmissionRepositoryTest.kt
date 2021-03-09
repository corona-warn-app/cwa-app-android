package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.risk.RiskLevelSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Instant
import org.joda.time.LocalTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AnalyticsKeySubmissionRepositoryTest : BaseTest() {

    @MockK lateinit var storage: AnalyticsKeySubmissionStorage
    @MockK lateinit var riskLevelSettings: RiskLevelSettings

    private val now = Instant.now()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = AnalyticsKeySubmissionRepository(
        storage, riskLevelSettings
    )

    @Test
    fun `hours since test result are calculated correctly`() {
        coEvery { storage.submittedAt.value } returns now.millis
        coEvery { storage.testResultReceivedAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        val repository = createInstance()
        repository.hoursSinceTestResult shouldBe 5
    }

    @Test
    fun `hours since test result when not submitted should be 0`() {
        coEvery { storage.submittedAt.value } returns -1
        coEvery { storage.testResultReceivedAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        val repository = createInstance()
        repository.hoursSinceTestResult shouldBe 0
    }

    @Test
    fun `hours since test result when not received or submitted should be 0`() {
        coEvery { storage.submittedAt.value } returns -1
        coEvery { storage.testResultReceivedAt.value } returns -1
        val repository = createInstance()
        repository.hoursSinceTestResult shouldBe 0
    }

    @Test
    fun `hours since test result should be 0 when dates have been manipulated`() {
        coEvery { storage.submittedAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        coEvery { storage.testResultReceivedAt.value } returns now.millis
        val repository = createInstance()
        repository.hoursSinceTestResult shouldBe 0
    }

    @Test
    fun `hours since test registration are calculated correctly`() {
        coEvery { storage.submittedAt.value } returns now.millis
        coEvery { storage.testRegisteredAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        val repository = createInstance()
        repository.hoursSinceTestRegistration shouldBe 5
    }

    @Test
    fun `hours since test registration should be 0 if not submitted`() {
        coEvery { storage.submittedAt.value } returns -1
        coEvery { storage.testRegisteredAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        val repository = createInstance()
        repository.hoursSinceTestRegistration shouldBe 0
    }

    @Test
    fun `days since most recent date at risk level at test registration are calculated correctly`() {
        coEvery {
            riskLevelSettings.lastChangeCheckedRiskLevelTimestamp
        } returns now
            .minus(Days.days(2).toStandardDuration()).toDateTime().toLocalDate()
            .toDateTime(LocalTime(22, 0)).toInstant()
        coEvery { storage.testRegisteredAt.value } returns
            now.toDateTime().toLocalDate().toDateTime(LocalTime(5, 0)).millis
        val repository = createInstance()
        repository.daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 2
    }

    @Test
    fun `days between most recent risk level change and test registration should be 0 if on same day`() {
        coEvery {
            riskLevelSettings.lastChangeCheckedRiskLevelTimestamp
        } returns now
            .toDateTime().toLocalDate()
            .toDateTime(LocalTime(13, 0)).toInstant()
        coEvery { storage.testRegisteredAt.value } returns
            now.toDateTime().toLocalDate().toDateTime(LocalTime(14, 0)).millis
        val repository = createInstance()
        repository.daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 0
    }

    @Test
    fun `days should be 0 if lastChangeCheckedRiskLevelTimestamp is null`() {
        coEvery {
            riskLevelSettings.lastChangeCheckedRiskLevelTimestamp
        } returns null
        coEvery { storage.testRegisteredAt.value } returns
            now.toDateTime().toLocalDate().toDateTime(LocalTime(14, 0)).millis
        val repository = createInstance()
        repository.daysSinceMostRecentDateAtRiskLevelAtTestRegistration shouldBe 0
    }
}
