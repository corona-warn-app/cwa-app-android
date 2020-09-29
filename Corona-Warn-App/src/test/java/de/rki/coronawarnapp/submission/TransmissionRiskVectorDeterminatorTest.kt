package de.rki.coronawarnapp.submission

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TransmissionRiskVectorDeterminatorTest {

    private lateinit var thisMorning: DateTime

    @Before
    fun setUp() {
        thisMorning = DateTime(2012, 10, 15, 10, 0, DateTimeZone.UTC)
    }

    @Test
    fun test_determine() {
        // positive - exact days
        Assert.assertArrayEquals(
            intArrayOf(8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    createSpecificStart(thisMorning),
                    Symptoms.Indication.POSITIVE
                ),
                thisMorning
            ).raw
        )
        Assert.assertArrayEquals(
            intArrayOf(8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    createSpecificStart(thisMorning.minusDays(1)),
                    Symptoms.Indication.POSITIVE
                ),
                thisMorning
            ).raw
        )
        Assert.assertArrayEquals(
            intArrayOf(2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    createSpecificStart(thisMorning.minusDays(5)),
                    Symptoms.Indication.POSITIVE
                ),
                thisMorning
            ).raw
        )
        Assert.assertArrayEquals(
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    createSpecificStart(thisMorning.minusDays(21)),
                    Symptoms.Indication.POSITIVE
                ),
                thisMorning
            ).raw
        )

        // positive - LastSevenDays
        Assert.assertArrayEquals(
            intArrayOf(4, 5, 6, 7, 7, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.LastSevenDays,
                    Symptoms.Indication.POSITIVE
                )
            ).raw
        )

        // positive - OneToTwoWeeksAgo
        Assert.assertArrayEquals(
            intArrayOf(1, 1, 1, 1, 2, 3, 4, 5, 6, 6, 7, 7, 6, 6, 4),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.OneToTwoWeeksAgo,
                    Symptoms.Indication.POSITIVE
                )
            ).raw
        )

        // positive - MoreThanTwoWeeks
        Assert.assertArrayEquals(
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.MoreThanTwoWeeks,
                    Symptoms.Indication.POSITIVE
                )
            ).raw
        )

        // positive - no info
        Assert.assertArrayEquals(
            intArrayOf(5, 6, 8, 8, 8, 7, 5, 3, 2, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.NoInformation,
                    Symptoms.Indication.POSITIVE
                )
            ).raw
        )

        // negative
        Assert.assertArrayEquals(
            intArrayOf(4, 4, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    null,
                    Symptoms.Indication.NEGATIVE
                )
            ).raw
        )

        // no info
        Assert.assertArrayEquals(
            intArrayOf(5, 6, 7, 7, 7, 6, 4, 3, 2, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    null, Symptoms.Indication.NO_INFORMATION
                )
            ).raw
        )
    }

    private fun createSpecificStart(dateTime: DateTime): Symptoms.StartOf.Date =
        Symptoms.StartOf.Date(dateTime.millis)
}
