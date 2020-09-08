package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.storage.LocalData
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.chrono.GJChronology
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

internal class GoogleQuotaCalculatorTest {

    private val timeInTest = DateTimeUtils.currentTimeMillis()

    private lateinit var classUnderTest: GoogleQuotaCalculator
    private val nextTimeRateLimitingUnlocksInTesting = AtomicLong()
    private val googleAPIProvideDiagnosisKeysCallCount = AtomicInteger()

    private val defaultIncrementByAmountInTest = 14
    private val defaultQuotaLimitInTest = 20

    @Before
    fun setUpClassUnderTest() {
        classUnderTest = GoogleQuotaCalculator(
            incrementByAmount = defaultIncrementByAmountInTest,
            quotaLimit = defaultQuotaLimitInTest,
            quotaResetPeriod = Duration.standardHours(24),
            quotaTimeZone = DateTimeZone.UTC,
            quotaChronology = GJChronology.getInstanceUTC()
        )
        DateTimeUtils.setCurrentMillisFixed(timeInTest)

        // Since LocalData is simple to mock
        mockkObject(LocalData)
        every { LocalData.nextTimeRateLimitingUnlocks = any() } answers {
            nextTimeRateLimitingUnlocksInTesting.set((this.arg(0) as Instant).millis)
        }
        every { LocalData.nextTimeRateLimitingUnlocks } answers {
            Instant.ofEpochMilli(nextTimeRateLimitingUnlocksInTesting.get())
        }
        every { LocalData.googleAPIProvideDiagnosisKeysCallCount = any() } answers {
            googleAPIProvideDiagnosisKeysCallCount.set(this.arg(0))
        }
        every { LocalData.googleAPIProvideDiagnosisKeysCallCount } answers {
            googleAPIProvideDiagnosisKeysCallCount.get()
        }

    }

    @Test
    fun `isAboveQuota false if called initially`() {
        assertEquals(classUnderTest.hasExceededQuota, false)
    }

    @Test
    fun `isAboveQuota true if called above quota limit when calling with amount bigger than one`() {
        for (callNumber in 1..5) {
            classUnderTest.calculateQuota()
            val aboveQuota = classUnderTest.hasExceededQuota
            Timber.v("call number $callNumber above quota: $aboveQuota")
            if (callNumber > 1) {
                assertEquals(true, aboveQuota)
            } else {
                assertEquals(false, aboveQuota)
            }
        }
    }

    @Test
    fun `getProgressTowardsQuota increases with calls to isAboveQuota but is stopped once increased above the quota`() {
        var latestCallNumberWithoutLimiting = 1
        for (callNumber in 1..5) {
            classUnderTest.calculateQuota()
            val aboveQuota = classUnderTest.hasExceededQuota
            Timber.v("call number $callNumber above quota: $aboveQuota")
            val expectedIncrement = callNumber * defaultIncrementByAmountInTest
            if (expectedIncrement >= defaultQuotaLimitInTest) {
                assertEquals(
                    (latestCallNumberWithoutLimiting + 1) * defaultIncrementByAmountInTest,
                    classUnderTest.getProgressTowardsQuota()
                )
            } else {
                assertEquals(
                    callNumber * defaultIncrementByAmountInTest,
                    classUnderTest.getProgressTowardsQuota()
                )
                latestCallNumberWithoutLimiting = callNumber
            }
        }
    }

    @Test
    fun `getProgressTowardsQuota is reset and the quota is not recalculated but isAboveQuota should still be false`() {
        var latestCallNumberWithoutLimiting = 1
        for (callNumber in 1..5) {
            classUnderTest.calculateQuota()
            val aboveQuota = classUnderTest.hasExceededQuota
            Timber.v("call number $callNumber above quota: $aboveQuota")
            val expectedIncrement = callNumber * defaultIncrementByAmountInTest
            if (expectedIncrement >= defaultQuotaLimitInTest) {
                assertEquals(
                    (latestCallNumberWithoutLimiting + 1) * defaultIncrementByAmountInTest,
                    classUnderTest.getProgressTowardsQuota()
                )
            } else {
                assertEquals(
                    callNumber * defaultIncrementByAmountInTest,
                    classUnderTest.getProgressTowardsQuota()
                )
                latestCallNumberWithoutLimiting = callNumber
            }
        }

        classUnderTest.resetProgressTowardsQuota(0)
        assertEquals(false, classUnderTest.hasExceededQuota)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getProgressTowardsQuota is reset but the reset value is no multiple of incrementByAmount`() {
        var latestCallNumberWithoutLimiting = 1
        for (callNumber in 1..5) {
            classUnderTest.calculateQuota()
            val aboveQuota = classUnderTest.hasExceededQuota
            Timber.v("call number $callNumber above quota: $aboveQuota")
            val expectedIncrement = callNumber * defaultIncrementByAmountInTest
            if (expectedIncrement >= defaultQuotaLimitInTest) {
                assertEquals(
                    (latestCallNumberWithoutLimiting + 1) * defaultIncrementByAmountInTest,
                    classUnderTest.getProgressTowardsQuota()
                )
            } else {
                assertEquals(
                    callNumber * defaultIncrementByAmountInTest,
                    classUnderTest.getProgressTowardsQuota()
                )
                latestCallNumberWithoutLimiting = callNumber
            }
        }

        classUnderTest.resetProgressTowardsQuota(defaultIncrementByAmountInTest + 1)
    }

    @Test
    fun `getProgressTowardsQuota is reset and the quota is not recalculated and the progress should update`() {
        var latestCallNumberWithoutLimiting = 1
        for (callNumber in 1..5) {
            classUnderTest.calculateQuota()
            val aboveQuota = classUnderTest.hasExceededQuota
            Timber.v("call number $callNumber above quota: $aboveQuota")
            val expectedIncrement = callNumber * defaultIncrementByAmountInTest
            if (expectedIncrement >= defaultQuotaLimitInTest) {
                assertEquals(
                    (latestCallNumberWithoutLimiting + 1) * defaultIncrementByAmountInTest,
                    classUnderTest.getProgressTowardsQuota()
                )
            } else {
                assertEquals(
                    callNumber * defaultIncrementByAmountInTest,
                    classUnderTest.getProgressTowardsQuota()
                )
                latestCallNumberWithoutLimiting = callNumber
            }
        }

        val newProgressAfterReset = 14
        classUnderTest.resetProgressTowardsQuota(newProgressAfterReset)
        assertEquals(false, classUnderTest.hasExceededQuota)
        assertEquals(newProgressAfterReset, classUnderTest.getProgressTowardsQuota())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getProgressTowardsQuota is reset and the quota is not recalculated and the progress throws an error because of too high newProgress`() {
        var latestCallNumberWithoutLimiting = 1
        var progressBeforeReset: Int? = null
        for (callNumber in 1..5) {
            classUnderTest.calculateQuota()
            val aboveQuota = classUnderTest.hasExceededQuota
            Timber.v("call number $callNumber above quota: $aboveQuota")
            val expectedIncrement = callNumber * defaultIncrementByAmountInTest
            if (expectedIncrement >= defaultQuotaLimitInTest) {
                progressBeforeReset =
                    (latestCallNumberWithoutLimiting + 1) * defaultIncrementByAmountInTest
                assertEquals(
                    (latestCallNumberWithoutLimiting + 1) * defaultIncrementByAmountInTest,
                    classUnderTest.getProgressTowardsQuota()
                )
            } else {
                assertEquals(
                    callNumber * defaultIncrementByAmountInTest,
                    classUnderTest.getProgressTowardsQuota()
                )
                latestCallNumberWithoutLimiting = callNumber
            }
        }

        val newProgressAfterReset = defaultQuotaLimitInTest + 1
        classUnderTest.resetProgressTowardsQuota(newProgressAfterReset)
        assertEquals(true, classUnderTest.hasExceededQuota)
        assertEquals(
            (progressBeforeReset
                ?: throw IllegalStateException("progressBeforeReset was not set during test")),
            classUnderTest.getProgressTowardsQuota()
        )
    }

    @Test
    fun `isAboveQuota true if called above quota limit when calling with amount one`() {
        classUnderTest = GoogleQuotaCalculator(
            incrementByAmount = 1,
            quotaLimit = 3,
            quotaResetPeriod = Duration.standardHours(24),
            quotaTimeZone = DateTimeZone.UTC,
            quotaChronology = GJChronology.getInstanceUTC()
        )
        for (callNumber in 1..15) {
            classUnderTest.calculateQuota()
            val aboveQuota = classUnderTest.hasExceededQuota
            Timber.v("call number $callNumber above quota: $aboveQuota")
            if (callNumber > 3) {
                assertEquals(true, aboveQuota)
            } else {
                assertEquals(false, aboveQuota)
            }
        }
    }

    @Test
    fun `isAboveQuota false if called above quota limit but next day resets quota`() {
        for (callNumber in 1..5) {
            classUnderTest.calculateQuota()
            val aboveQuota = classUnderTest.hasExceededQuota
            Timber.v("call number $callNumber above quota: $aboveQuota")
            if (callNumber > 1) {
                assertEquals(true, aboveQuota)
            } else {
                assertEquals(false, aboveQuota)
            }
        }

        // Day Change
        val timeInTestAdvancedByADay = timeInTest + Duration.standardDays(1).millis
        DateTimeUtils.setCurrentMillisFixed(timeInTestAdvancedByADay)
        classUnderTest.calculateQuota()
        val aboveQuotaAfterDayAdvance = classUnderTest.hasExceededQuota
        Timber.v("above quota after day advance: $aboveQuotaAfterDayAdvance")

        assertEquals(false, aboveQuotaAfterDayAdvance)
    }

    @Test
    fun `test if isAfter is affected by Timezone to make sure we do not run into Shifting Errors`() {
        val testTimeUTC = DateTime(
            timeInTest,
            DateTimeZone.UTC
        ).withChronology(GJChronology.getInstanceUTC())
        val testTimeGMT = DateTime(
            timeInTest,
            DateTimeZone.forID("Etc/GMT+2")
        ).withChronology(GJChronology.getInstanceUTC())

        assertEquals(testTimeGMT, testTimeUTC)
        assertEquals(testTimeGMT.millis, testTimeUTC.millis)

        val testTimeUTCAfterGMT = testTimeUTC.plusMinutes(1)

        assertEquals(true, testTimeUTCAfterGMT.isAfter(testTimeGMT))

        val testTimeGMTAfterUTC = testTimeGMT.plusMinutes(1)

        assertEquals(true, testTimeGMTAfterUTC.isAfter(testTimeUTC))

    }

    @After
    fun cleanup() {
        DateTimeUtils.setCurrentMillisSystem()
        unmockkObject(LocalData)
    }
}