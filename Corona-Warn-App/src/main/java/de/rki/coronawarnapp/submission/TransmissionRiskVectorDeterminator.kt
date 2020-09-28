package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.util.TimeAndDateExtensions.ageInDays

class TransmissionRiskVectorDeterminator {

    @Suppress("MagicNumber")
    fun determine(symptoms: Symptoms) = TransmissionRiskVector(
        when (symptoms.symptomIndication) {
            Symptoms.Indication.POSITIVE -> when (symptoms.startOfSymptoms) {
                is Symptoms.StartOf.Date -> {
                    when (ageInDays(symptoms.startOfSymptoms.millis)) {
                        0 -> intArrayOf(8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1)
                        1 -> intArrayOf(8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1)
                        2 -> intArrayOf(6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1)
                        3 -> intArrayOf(5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1)
                        4 -> intArrayOf(3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1)
                        5 -> intArrayOf(2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1)
                        6 -> intArrayOf(2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1)
                        7 -> intArrayOf(1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1)
                        8 -> intArrayOf(1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1)
                        9 -> intArrayOf(1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2)
                        10 -> intArrayOf(1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4)
                        11 -> intArrayOf(1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6)
                        12 -> intArrayOf(1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7)
                        13 -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8)
                        14 -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8)
                        15 -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8)
                        16 -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6)
                        17 -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5)
                        18 -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3)
                        19 -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2)
                        20 -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2)
                        else -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
                    }
                }
                is Symptoms.StartOf.LastSevenDays -> intArrayOf(4, 5, 6, 7, 7, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1)
                is Symptoms.StartOf.MoreThanTwoWeeks -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5)
                is Symptoms.StartOf.NoInformation -> intArrayOf(5, 6, 8, 8, 8, 7, 5, 3, 2, 1, 1, 1, 1, 1, 1)
                is Symptoms.StartOf.OneToTwoWeeksAgo -> intArrayOf(1, 1, 1, 1, 2, 3, 4, 5, 6, 6, 7, 7, 6, 6, 4)
                else -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
            }
            Symptoms.Indication.NEGATIVE -> intArrayOf(4, 4, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
            Symptoms.Indication.NO_INFORMATION -> intArrayOf(5, 6, 7, 7, 7, 6, 4, 3, 2, 1, 1, 1, 1, 1, 1)
        })
}
