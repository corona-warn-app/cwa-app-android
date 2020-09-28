package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.submission.TransmissionRiskVectorDeterminator.Companion.numberOfDays

class DaysSinceOnsetOfSymptomsVectorDeterminator {

    companion object {
        const val VECTOR_LENGTH = 15
    }

    @Suppress("MagicNumber")
    internal fun determine(symptoms: Symptoms): DaysSinceOnsetOfSymptomsVector {
        return when (symptoms.symptomIndication) {
            Symptoms.Indication.POSITIVE -> {
                when (symptoms.startOfSymptoms) {
                    is Symptoms.StartOf.Date ->
                        createDaysSinceOnsetOfSymptomsVectorWith(
                            numberOfDays(symptoms.startOfSymptoms.millis),
                            VECTOR_LENGTH
                        )
                    is Symptoms.StartOf.LastSevenDays ->
                        createDaysSinceOnsetOfSymptomsVectorWith(701,
                            VECTOR_LENGTH)
                    is Symptoms.StartOf.OneToTwoWeeksAgo ->
                        createDaysSinceOnsetOfSymptomsVectorWith(708,
                            VECTOR_LENGTH)
                    is Symptoms.StartOf.MoreThanTwoWeeks ->
                        createDaysSinceOnsetOfSymptomsVectorWith(715,
                            VECTOR_LENGTH)
                    else ->
                        createDaysSinceOnsetOfSymptomsVectorWith(2000,
                            VECTOR_LENGTH)
                }
            }
            Symptoms.Indication.NO_INFORMATION ->
                createDaysSinceOnsetOfSymptomsVectorWith(4000, VECTOR_LENGTH)
            Symptoms.Indication.NEGATIVE ->
                createDaysSinceOnsetOfSymptomsVectorWith(3000, VECTOR_LENGTH)
        }
    }

    private fun createDaysSinceOnsetOfSymptomsVectorWith(
        submissionDayValue: Int,
        size: Int
    ): DaysSinceOnsetOfSymptomsVector {
        return (submissionDayValue downTo (submissionDayValue - size + 1)).toList().toIntArray()
    }
}
