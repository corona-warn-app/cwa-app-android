package de.rki.coronawarnapp.submission

import org.junit.Assert
import org.junit.Test

class DaysSinceOnsetOfSymptomsVectorDeterminatorTest {

    @Test
    fun positive_exactDate_Yesterday() {
        val daysAgo = 1
        Assert.assertArrayEquals(
            intArrayOf(-13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1),
            DaysSinceOnsetOfSymptomsVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.Date(System.currentTimeMillis() - 1000 * 3600 * (24 * daysAgo + 2)),
                    Symptoms.Indication.POSITIVE
                ),
                15
            )
        )
    }

    @Test
    fun positive_exactDate_Today() {
        val daysAgo = 0
        Assert.assertArrayEquals(
            intArrayOf(-14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0),
            DaysSinceOnsetOfSymptomsVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.Date(System.currentTimeMillis() - 1000 * 3600 * (24 * daysAgo + 2)),
                    Symptoms.Indication.POSITIVE
                ),
                15
            )
        )
    }

    @Test
    fun positive_exactDate_5DaysAgo() {
        val daysAgo = 5
        Assert.assertArrayEquals(
            intArrayOf(-9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5),
            DaysSinceOnsetOfSymptomsVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.Date(System.currentTimeMillis() - 1000 * 3600 * (24 * daysAgo + 2)),
                    Symptoms.Indication.POSITIVE
                ),
                15
            )
        )
    }

    @Test
    fun positive_exactDate_21DaysAgo() {
        val daysAgo = 21
        Assert.assertArrayEquals(
            intArrayOf(7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21),
            DaysSinceOnsetOfSymptomsVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.Date(System.currentTimeMillis() - 1000 * 3600 * (24 * daysAgo + 2)),
                    Symptoms.Indication.POSITIVE
                ),
                15
            )
        )
    }

    @Test
    fun positive_lastSevenDays() {
        Assert.assertArrayEquals(
            intArrayOf(687, 688, 689, 690, 691, 692, 693, 694, 695, 696, 697, 698, 699, 700, 701),
            DaysSinceOnsetOfSymptomsVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.LastSevenDays,
                    Symptoms.Indication.POSITIVE
                ),
                15
            )
        )
    }

    @Test
    fun positive_oneToTwoWeeks() {
        Assert.assertArrayEquals(
            intArrayOf(694, 695, 696, 697, 698, 699, 700, 701, 702, 703, 704, 705, 706, 707, 708),
            DaysSinceOnsetOfSymptomsVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.OneToTwoWeeksAgo,
                    Symptoms.Indication.POSITIVE
                ),
                15
            )
        )
    }

    @Test
    fun positive_moreThanTwoWeeks() {
        Assert.assertArrayEquals(
            intArrayOf(701, 702, 703, 704, 705, 706, 707, 708, 709, 710, 711, 712, 713, 714, 715),
            DaysSinceOnsetOfSymptomsVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.MoreThanTwoWeeks,
                    Symptoms.Indication.POSITIVE
                ),
                15
            )
        )
    }

    @Test
    fun positive_other_or_noInformation() {
        Assert.assertArrayEquals(
            intArrayOf(1986, 1987, 1988, 1989, 1990, 1991, 1992, 1993, 1994, 1995, 1996, 1997, 1998, 1999, 2000),
            DaysSinceOnsetOfSymptomsVectorDeterminator().determine(
                Symptoms(
                    Symptoms.StartOf.NoInformation,
                    Symptoms.Indication.POSITIVE
                ),
                15
            )
        )
    }

    @Test
    fun noInformation() {
        Assert.assertArrayEquals(
            intArrayOf(3986, 3987, 3988, 3989, 3990, 3991, 3992, 3993, 3994, 3995, 3996, 3997, 3998, 3999, 4000),
            DaysSinceOnsetOfSymptomsVectorDeterminator().determine(
                Symptoms(
                    null,
                    Symptoms.Indication.NO_INFORMATION
                ),
                15
            )
        )
    }

    @Test
    fun noSymptoms() {
        Assert.assertArrayEquals(
            intArrayOf(2986, 2987, 2988, 2989, 2990, 2991, 2992, 2993, 2994, 2995, 2996, 2997, 2998, 2999, 3000),
            DaysSinceOnsetOfSymptomsVectorDeterminator().determine(
                Symptoms(
                    null,
                    Symptoms.Indication.NEGATIVE
                ),
                15
            )
        )
    }
}
