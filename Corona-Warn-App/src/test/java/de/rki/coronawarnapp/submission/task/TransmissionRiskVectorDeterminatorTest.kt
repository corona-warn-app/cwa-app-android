package de.rki.coronawarnapp.submission.task

import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.Symptoms.Indication.NEGATIVE
import de.rki.coronawarnapp.submission.Symptoms.Indication.NO_INFORMATION
import de.rki.coronawarnapp.submission.Symptoms.Indication.POSITIVE
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class TransmissionRiskVectorDeterminatorTest {

    private lateinit var now: LocalDate

    @MockK
    private lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        LocalDateTime.of(2012, 10, 15, 10, 0,0,0).apply {
            every { timeStamper.nowJavaUTC } returns this.toInstant(ZoneOffset.UTC)
            now = this.toLocalDate()
        }
    }

    @Test
    fun `match a positive symptom indication with the exact date of today`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowJavaUTC.startMinusDays(0), POSITIVE),
            now
        ).raw shouldBe intArrayOf(8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with the exact date of yesterday`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowJavaUTC.startMinusDays(1), POSITIVE),
            now
        ).raw shouldBe intArrayOf(8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with the exact date of 5 days ago`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowJavaUTC.startMinusDays(5), POSITIVE),
            now
        ).raw shouldBe intArrayOf(2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with the exact date of 21 days ago`() {
        TransmissionRiskVectorDeterminator(timeStamper).determine(
            Symptoms(timeStamper.nowJavaUTC.startMinusDays(21), POSITIVE),
            now
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

    private fun Instant.startMinusDays(days: Long): Symptoms.StartOf =
        Symptoms.StartOf.Date(this.toLocalDateUtc().minusDays(days))
}
