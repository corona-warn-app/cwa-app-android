package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.submission.TransmissionRiskVectorDeterminator.Companion.numberOfDays

class DaysSinceOnsetOfSymptomsVectorDeterminator {

    @Suppress("MagicNumber")
    internal fun determine(symptoms: Symptoms, size: Int): DaysSinceOnsetOfSymptomsVector {
        return when (symptoms.symptomIndication) {
            Symptoms.Indication.POSITIVE -> {
                when (symptoms.startOfSymptoms) {
                    is Symptoms.StartOf.Date ->
                        createDaysSinceOnsetOfSymptomsVectorWith(
                            numberOfDays(symptoms.startOfSymptoms.millis),
                            size
                        )
                    is Symptoms.StartOf.LastSevenDays ->
                        createDaysSinceOnsetOfSymptomsVectorWith(701, size)
                    is Symptoms.StartOf.OneToTwoWeeksAgo ->
                        createDaysSinceOnsetOfSymptomsVectorWith(708, size)
                    is Symptoms.StartOf.MoreThanTwoWeeks ->
                        createDaysSinceOnsetOfSymptomsVectorWith(715, size)
                    else ->
                        createDaysSinceOnsetOfSymptomsVectorWith(2000, size)
                }
            }
            Symptoms.Indication.NO_INFORMATION ->
                createDaysSinceOnsetOfSymptomsVectorWith(4000, size)
            Symptoms.Indication.NEGATIVE ->
                createDaysSinceOnsetOfSymptomsVectorWith(3000, size)
        }
    }

    private fun createDaysSinceOnsetOfSymptomsVectorWith(
        submissionDayValue: Int,
        size: Int
    ): DaysSinceOnsetOfSymptomsVector {
        return ((submissionDayValue - size + 1) until submissionDayValue + 1).toList().toIntArray()
    }
}
