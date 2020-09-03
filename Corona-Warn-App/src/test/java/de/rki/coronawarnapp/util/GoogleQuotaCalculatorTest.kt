package de.rki.coronawarnapp.util

import org.joda.time.DateTimeUtils
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.chrono.GJChronology
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber

internal class GoogleQuotaCalculatorTest : BaseTest() {

    private val timeInTest = DateTimeUtils.currentTimeMillis()

    private lateinit var classUnderTest: GoogleQuotaCalculator

    @BeforeEach
    fun setUpClassUnderTest() {
        classUnderTest = GoogleQuotaCalculator(
            incrementByAmount = 14,
            quotaLimit = 20,
            quotaResetPeriod = Duration.standardHours(24),
            quotaTimeZone = DateTimeZone.UTC,
            quotaChronology = GJChronology.getInstanceUTC()
        )
        DateTimeUtils.setCurrentMillisFixed(timeInTest)
    }

    @Test
    fun `isAboveQuota false if called initially`() {
        assertEquals(classUnderTest.isAboveQuota(), false)
    }

    @Test
    fun `isAboveQuota true if called above quota limit when calling with amount bigger than one`() {
        for (callNumber in 1..5) {
            val aboveQuota = classUnderTest.isAboveQuota()
            Timber.v("call number $callNumber above quota: $aboveQuota")
            if (callNumber > 1) {
                assertEquals(true, aboveQuota)
            } else {
                assertEquals(false, aboveQuota)
            }
        }
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
            val aboveQuota = classUnderTest.isAboveQuota()
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
            val aboveQuota = classUnderTest.isAboveQuota()
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

        val aboveQuotaAfterDayAdvance = classUnderTest.isAboveQuota()
        Timber.v("above quota after day advance: $aboveQuotaAfterDayAdvance")

        assertEquals(false, aboveQuotaAfterDayAdvance)
    }

    @AfterEach
    fun cleanup() {
        DateTimeUtils.setCurrentMillisSystem()
    }
}