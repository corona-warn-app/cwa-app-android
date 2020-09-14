package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.util.Dates

class TransmissionRiskVectorDeterminator {

    fun determine(symptoms: Symptoms): TransmissionRiskVector = TransmissionRiskVector(
        when (symptoms.symptomIndication) {
            SymptomIndication.POSITIVE -> when (symptoms.startOfSymptoms) {
                is StartOfSymptoms.Date -> when (Dates.numberOfDays(symptoms.startOfSymptoms.millis, System.currentTimeMillis())) {
                    0 -> arrayOf(8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1)
                    1 -> arrayOf(8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1)
                    2 -> arrayOf(6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1)
                    3 -> arrayOf(5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1)
                    4 -> arrayOf(3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1)
                    5 -> arrayOf(2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1)
                    6 -> arrayOf(2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1)
                    7 -> arrayOf(1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1)
                    8 -> arrayOf(1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1)
                    9 -> arrayOf(1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2)
                    10 -> arrayOf(1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4)
                    11 -> arrayOf(1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6)
                    12 -> arrayOf(1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7)
                    13 -> arrayOf(1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8)
                    14 -> arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8)
                    15 -> arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8)
                    16 -> arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6)
                    17 -> arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5)
                    18 -> arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3)
                    19 -> arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2)
                    20 -> arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2)
                    else -> arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
                    //TODO > 21 invalid
                }
                is StartOfSymptoms.LastSevenDays -> arrayOf(4, 5, 6, 7, 7, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1)
                is StartOfSymptoms.MoreThanTwoWeeks -> arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5)
                is StartOfSymptoms.NoInformation -> arrayOf(5, 6, 8, 8, 8, 7, 5, 3, 2, 1, 1, 1, 1, 1, 1)
                is StartOfSymptoms.OneToTwoWeeksAgo -> arrayOf(1, 1, 1, 1, 2, 3, 4, 5, 6, 6, 7, 7, 6, 6, 4)
            }
            SymptomIndication.NEGATIVE -> arrayOf(4, 4, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
            SymptomIndication.NO_INFORMATION -> arrayOf(5, 6, 7, 7, 7, 6, 4, 3, 2, 1, 1, 1, 1, 1, 1)
        }
    )
}
