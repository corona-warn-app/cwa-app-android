package de.rki.coronawarnapp.submission

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
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransmissionRiskVectorDeterminatorTest {

    private lateinit var now: LocalDate

    @MockK
    private lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        DateTime(2012, 10, 15, 10, 0, DateTimeZone.UTC).apply {
            every { timeStamper.nowUTC } returns this.toInstant()
            now =  this.toLocalDate()
        }
    }

    @Test
    fun `match a positive symptom indication with the exact date of today`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowUTC.startMinusDays(0), POSITIVE), now
        ).raw shouldBe intArrayOf(8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with the exact date of yesterday`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowUTC.startMinusDays(1), POSITIVE), now
        ).raw shouldBe intArrayOf(8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with the exact date of 5 days ago`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowUTC.startMinusDays(5), POSITIVE), now
        ).raw shouldBe intArrayOf(2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with the exact date of 21 days ago`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowUTC.startMinusDays(21), POSITIVE), now
        ).raw shouldBe intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with a symptom range within the last 7 days`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(Symptoms.StartOf.LastSevenDays, POSITIVE)
        ).raw shouldBe intArrayOf(4, 5, 6, 7, 7, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with a symptom range within the last 2 weeks`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(Symptoms.StartOf.OneToTwoWeeksAgo, POSITIVE)
        ).raw shouldBe intArrayOf(1, 1, 1, 1, 2, 3, 4, 5, 6, 6, 7, 7, 6, 6, 4)
    }

    @Test
    fun `match a positive symptom indication with a symptom range more than 2 weeks`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(Symptoms.StartOf.MoreThanTwoWeeks, POSITIVE)
        ).raw shouldBe intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5)
    }

    @Test
    fun `match a positive symptom indication with other or no detailed information`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(Symptoms.StartOf.NoInformation, POSITIVE)
        ).raw shouldBe intArrayOf(5, 6, 8, 8, 8, 7, 5, 3, 2, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match no symptoms`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(null, NEGATIVE)
        ).raw shouldBe intArrayOf(4, 4, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match no information about symptoms`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(null, NO_INFORMATION)
        ).raw shouldBe intArrayOf(5, 6, 7, 7, 7, 6, 4, 3, 2, 1, 1, 1, 1, 1, 1)
    }

    private fun Instant.startMinusDays(days: Int): Symptoms.StartOf =
        Symptoms.StartOf.Date(this.toLocalDate().minusDays(days))
}
