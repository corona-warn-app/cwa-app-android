package de.rki.coronawarnapp.submission

import org.junit.Assert
import org.junit.Test

class DaysSinceOnsetOfSymptomsVectorDeterminatorTest {

    @Test
    fun positive_exactDate_Yesterday() {
        val daysAgo = 1
        Assert.assertArrayEquals(
            intArrayOf(1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13),
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
            intArrayOf(0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14),
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
            intArrayOf(5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9),
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
            intArrayOf(21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7),
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
            intArrayOf(701, 700, 699, 698, 697, 696, 695, 694, 693, 692, 691, 690, 689, 688, 687),
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
            intArrayOf(708, 707, 706, 705, 704, 703, 702, 701, 700, 699, 698, 697, 696, 695, 694),
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
            intArrayOf(715, 714, 713, 712, 711, 710, 709, 708, 707, 706, 705, 704, 703, 702, 701),
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
            intArrayOf(2000, 1999, 1998, 1997, 1996, 1995, 1994, 1993, 1992, 1991, 1990, 1989, 1988, 1987, 1986),
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
            intArrayOf(4000, 3999, 3998, 3997, 3996, 3995, 3994, 3993, 3992, 3991, 3990, 3989, 3988, 3987, 3986),
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
            intArrayOf(3000, 2999, 2998, 2997, 2996, 2995, 2994, 2993, 2992, 2991, 2990, 2989, 2988, 2987, 2986),
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
