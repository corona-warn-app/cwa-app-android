package de.rki.coronawarnapp.submission.task

import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.Symptoms.Indication.NEGATIVE
import de.rki.coronawarnapp.submission.Symptoms.Indication.NO_INFORMATION
import de.rki.coronawarnapp.submission.Symptoms.Indication.POSITIVE
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUserTz
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TransmissionRiskVectorDeterminerTest {

    private val now = Instant.parse("2012-10-15T10:00:00Z")
    private val today = now.toLocalDateUserTz()
    private val zoneId = ZoneId.of("Europe/Paris")

    @MockK
    private lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(ZoneId::class)
        every { ZoneId.systemDefault() } returns zoneId
        every { timeStamper.nowUTC } returns now
    }

    @Test
    fun `match a positive symptom indication with the exact date of today`() {
        TransmissionRiskVectorDeterminer(timeStamper).determine(
            Symptoms(today.startMinusDays(0), POSITIVE)
        ).raw shouldBe intArrayOf(8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with the exact date of yesterday`() {
        TransmissionRiskVectorDeterminer(timeStamper).determine(
            Symptoms(today.startMinusDays(1), POSITIVE)
        ).raw shouldBe intArrayOf(8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with the exact date of 5 days ago`() {
        TransmissionRiskVectorDeterminer(timeStamper).determine(
            Symptoms(today.startMinusDays(5), POSITIVE)
        ).raw shouldBe intArrayOf(2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with the exact date of 21 days ago`() {
        TransmissionRiskVectorDeterminer(timeStamper).determine(
            Symptoms(today.startMinusDays(21), POSITIVE)
        ).raw shouldBe intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with a symptom range within the last 7 days`() {
        TransmissionRiskVectorDeterminer(timeStamper).determine(
            Symptoms(Symptoms.StartOf.LastSevenDays, POSITIVE)
        ).raw shouldBe intArrayOf(4, 5, 6, 7, 7, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1)
    }

    @Test
    fun `match a positive symptom indication with a symptom range within the last 2 weeks`() {
        TransmissionRiskVectorDeterminer(timeStamper).determine(
            Symptoms(Symptoms.StartOf.OneToTwoWeeksAgo, POSITIVE)
        ).raw shouldBe intArrayOf(1, 1, 1, 1, 2, 3, 4, 5, 6, 6, 7, 7, 6, 6, 4)
    }

    @Test
    fun `match a positive symptom indication with a symptom range more than 2 weeks`() {
        TransmissionRiskVectorDeterminer(timeStamper).determine(
            Symptoms(Symptoms.StartOf.MoreThanTwoWeeks, POSITIVE)
        ).raw shouldBe intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5)
    }

    @Test
    fun `match a positive symptom indication with other or no detailed information`() {
        TransmissionRiskVectorDeterminer(timeStamper).determine(
            Symptoms(Symptoms.StartOf.NoInformation, POSITIVE)
        ).raw shouldBe intArrayOf(5, 6, 8, 8, 8, 7, 5, 3, 2, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match no symptoms`() {
        TransmissionRiskVectorDeterminer(timeStamper).determine(
            Symptoms(null, NEGATIVE)
        ).raw shouldBe intArrayOf(4, 4, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    }

    @Test
    fun `match no information about symptoms`() {
        TransmissionRiskVectorDeterminer(timeStamper).determine(
            Symptoms(null, NO_INFORMATION)
        ).raw shouldBe intArrayOf(5, 6, 7, 7, 7, 6, 4, 3, 2, 1, 1, 1, 1, 1, 1)
    }

    private fun LocalDate.startMinusDays(days: Int): Symptoms.StartOf =
        Symptoms.StartOf.Date(this.minusDays(days.toLong()))
}
