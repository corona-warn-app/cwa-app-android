package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TransmissionRiskVectorDeterminatorTest {

    private lateinit var thisMorning: DateTime

    @MockK
    lateinit var timeStamper: TimeStamper

    @Before
    fun setUp() {
        thisMorning = DateTime(2012, 10, 15, 10, 0, DateTimeZone.UTC)

        every { timeStamper.nowUTC } returns thisMorning.toInstant()

    }

    @Test
    fun test_determine() {
        // positive - exact days
        Assert.assertArrayEquals(
            intArrayOf(8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator(timeStamper).determine(
                Symptoms(
                    createSpecificStart(thisMorning.toLocalDate()),
                    Symptoms.Indication.POSITIVE
                ),
                thisMorning.toLocalDate()
            ).raw
        )
        Assert.assertArrayEquals(
            intArrayOf(8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator(timeStamper).determine(
                Symptoms(
                    createSpecificStart(thisMorning.minusDays(1).toLocalDate()),
                    Symptoms.Indication.POSITIVE
                ),
                thisMorning.toLocalDate()
            ).raw
        )
        Assert.assertArrayEquals(
            intArrayOf(2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator(timeStamper).determine(
                Symptoms(
                    createSpecificStart(thisMorning.minusDays(5).toLocalDate()),
                    Symptoms.Indication.POSITIVE
                ),
                thisMorning.toLocalDate()
            ).raw
        )
        Assert.assertArrayEquals(
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator(timeStamper).determine(
                Symptoms(
                    createSpecificStart(thisMorning.minusDays(21).toLocalDate()),
                    Symptoms.Indication.POSITIVE
                ),
                thisMorning.toLocalDate()
            ).raw
        )

        // positive - LastSevenDays
        Assert.assertArrayEquals(
            intArrayOf(4, 5, 6, 7, 7, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator(timeStamper).determine(
                Symptoms(
                    Symptoms.StartOf.LastSevenDays,
                    Symptoms.Indication.POSITIVE
                )
            ).raw
        )

        // positive - OneToTwoWeeksAgo
        Assert.assertArrayEquals(
            intArrayOf(1, 1, 1, 1, 2, 3, 4, 5, 6, 6, 7, 7, 6, 6, 4),
            TransmissionRiskVectorDeterminator(timeStamper).determine(
                Symptoms(
                    Symptoms.StartOf.OneToTwoWeeksAgo,
                    Symptoms.Indication.POSITIVE
                )
            ).raw
        )

        // positive - MoreThanTwoWeeks
        Assert.assertArrayEquals(
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5),
            TransmissionRiskVectorDeterminator(timeStamper).determine(
                Symptoms(
                    Symptoms.StartOf.MoreThanTwoWeeks,
                    Symptoms.Indication.POSITIVE
                )
            ).raw
        )

        // positive - no info
        Assert.assertArrayEquals(
            intArrayOf(5, 6, 8, 8, 8, 7, 5, 3, 2, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator(timeStamper).determine(
                Symptoms(
                    Symptoms.StartOf.NoInformation,
                    Symptoms.Indication.POSITIVE
                )
            ).raw
        )

        // negative
        Assert.assertArrayEquals(
            intArrayOf(4, 4, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator(timeStamper).determine(
                Symptoms(
                    null,
                    Symptoms.Indication.NEGATIVE
                )
            ).raw
        )

        // no info
        Assert.assertArrayEquals(
            intArrayOf(5, 6, 7, 7, 7, 6, 4, 3, 2, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator(timeStamper).determine(
                Symptoms(
                    null, Symptoms.Indication.NO_INFORMATION
                )
            ).raw
        )
    }

    private fun createSpecificStart(localDate: LocalDate): Symptoms.StartOf.Date =
        Symptoms.StartOf.Date(localDate)
}
