package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import org.joda.time.Hours
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AnalyticsPCRKeySubmissionRepositoryTest : BaseTest() {

    @MockK lateinit var storage: AnalyticsPCRKeySubmissionStorage

    private val now = Instant.now()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = AnalyticsPCRKeySubmissionRepository(
        storage
    )

    @Test
    fun `hours since test result are calculated correctly`() {
        coEvery { storage.submittedAt.value } returns now.millis
        coEvery { storage.testResultReceivedAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        val repository = createInstance()
        repository.hoursSinceTestResult shouldBe 5
    }

    @Test
    fun `hours since test result when not submitted should be -1`() {
        coEvery { storage.submittedAt.value } returns -1
        coEvery { storage.testResultReceivedAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        val repository = createInstance()
        repository.hoursSinceTestResult shouldBe -1
    }

    @Test
    fun `hours since test result should be -1 when testResultReceivedAt is missing`() {
        coEvery { storage.submittedAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        coEvery { storage.testResultReceivedAt.value } returns -1
        val repository = createInstance()
        repository.hoursSinceTestResult shouldBe -1
    }

    @Test
    fun `hours since test result when not received or submitted should be -1`() {
        coEvery { storage.submittedAt.value } returns -1
        coEvery { storage.testResultReceivedAt.value } returns -1
        val repository = createInstance()
        repository.hoursSinceTestResult shouldBe -1
    }

    @Test
    fun `hours since test result should be -1 when dates have been manipulated`() {
        coEvery { storage.submittedAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        coEvery { storage.testResultReceivedAt.value } returns now.millis
        val repository = createInstance()
        repository.hoursSinceTestResult shouldBe -1
    }

    @Test
    fun `hours since test registration are calculated correctly`() {
        coEvery { storage.submittedAt.value } returns now.millis
        coEvery { storage.testRegisteredAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        val repository = createInstance()
        repository.hoursSinceTestRegistration shouldBe 5
    }

    @Test
    fun `hours since test registration should be -1 if not submitted`() {
        coEvery { storage.submittedAt.value } returns -1
        coEvery { storage.testRegisteredAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        val repository = createInstance()
        repository.hoursSinceTestRegistration shouldBe -1
    }

    @Test
    fun `hours since test registration should be -1 if testRegisteredAt is missing`() {
        coEvery { storage.submittedAt.value } returns now.minus(Hours.hours(5).toStandardDuration()).millis
        coEvery { storage.testRegisteredAt.value } returns -1
        val repository = createInstance()
        repository.hoursSinceTestRegistration shouldBe -1
    }
}
