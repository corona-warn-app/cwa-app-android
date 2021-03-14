package de.rki.coronawarnapp.submission.task

import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.Symptoms.Indication.NEGATIVE
import de.rki.coronawarnapp.submission.Symptoms.Indication.NO_INFORMATION
import de.rki.coronawarnapp.submission.Symptoms.Indication.POSITIVE
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DaysSinceOnsetOfSymptomsVectorDeterminatorTest {

    private val now = DateTime(2012, 10, 15, 10, 0, DateTimeZone.UTC)

    @MockK
    private lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns now.toInstant()
    }

    @Test
    fun `match a positive symptom indication to the exact date of yesterday`() {
        DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowUTC.startMinusDays(1), POSITIVE)
        ) shouldBe intArrayOf(1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13)
    }

    @Test
    fun `match a positive symptom indication to the exact date of today`() {
        DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowUTC.startMinusDays(0), POSITIVE)
        ) shouldBe intArrayOf(0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14)
    }

    @Test
    fun `match a positive symptom indication to the exact date 5 days ago`() {
        DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowUTC.startMinusDays(5), POSITIVE)
        ) shouldBe intArrayOf(5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9)
    }

    @Test
    fun `match a positive symptom indication to the exact date 21 days ago`() {
        DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowUTC.startMinusDays(21), POSITIVE)
        ) shouldBe intArrayOf(21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7)
    }

    @Test
    fun `match a positive symptom indication to a range within the last 7 days`() {
        DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper).determine(
            Symptoms(Symptoms.StartOf.LastSevenDays, POSITIVE)
        ) shouldBe intArrayOf(701, 700, 699, 698, 697, 696, 695, 694, 693, 692, 691, 690, 689, 688, 687)
    }

    @Test
    fun `match a positive symptom indication to a range within the last two weeks`() {
        DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper).determine(
            Symptoms(Symptoms.StartOf.OneToTwoWeeksAgo, POSITIVE)
        ) shouldBe intArrayOf(708, 707, 706, 705, 704, 703, 702, 701, 700, 699, 698, 697, 696, 695, 694)
    }

    @Test
    fun `match a positive symptom indication to a range more than two weeks ago`() {
        DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper).determine(
            Symptoms(Symptoms.StartOf.MoreThanTwoWeeks, POSITIVE)
        ) shouldBe intArrayOf(715, 714, 713, 712, 711, 710, 709, 708, 707, 706, 705, 704, 703, 702, 701)
    }

    @Test
    fun `match a positive symptom indication with other or no detailed information`() {
        DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper).determine(
            Symptoms(Symptoms.StartOf.NoInformation, POSITIVE)
        ) shouldBe intArrayOf(2000, 1999, 1998, 1997, 1996, 1995, 1994, 1993, 1992, 1991, 1990, 1989, 1988, 1987, 1986)
    }

    @Test
    fun `match no information about symptoms`() {
        DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper).determine(
            Symptoms(null, NO_INFORMATION)
        ) shouldBe intArrayOf(4000, 3999, 3998, 3997, 3996, 3995, 3994, 3993, 3992, 3991, 3990, 3989, 3988, 3987, 3986)
    }

    @Test
    fun `match no symptoms`() {
        DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper).determine(
            Symptoms(null, NEGATIVE)
        ) shouldBe intArrayOf(3000, 2999, 2998, 2997, 2996, 2995, 2994, 2993, 2992, 2991, 2990, 2989, 2988, 2987, 2986)
    }

    private fun Instant.startMinusDays(days: Int): Symptoms.StartOf =
        Symptoms.StartOf.Date(this.toLocalDate().minusDays(days))
}
