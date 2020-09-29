package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.util.TimeAndDateExtensions.numberOfDayChanges

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
                            numberOfDayChanges(symptoms.startOfSymptoms.millis)
                        )
                    is Symptoms.StartOf.LastSevenDays ->
                        createDaysSinceOnsetOfSymptomsVectorWith(701)
                    is Symptoms.StartOf.OneToTwoWeeksAgo ->
                        createDaysSinceOnsetOfSymptomsVectorWith(708)
                    is Symptoms.StartOf.MoreThanTwoWeeks ->
                        createDaysSinceOnsetOfSymptomsVectorWith(715)
                    else ->
                        createDaysSinceOnsetOfSymptomsVectorWith(2000)
                }
            }
            Symptoms.Indication.NO_INFORMATION ->
                createDaysSinceOnsetOfSymptomsVectorWith(4000)
            Symptoms.Indication.NEGATIVE ->
                createDaysSinceOnsetOfSymptomsVectorWith(3000)
        }
    }

    private fun createDaysSinceOnsetOfSymptomsVectorWith(
        submissionDayValue: Int,
        size: Int = VECTOR_LENGTH
    ): DaysSinceOnsetOfSymptomsVector {
        return (submissionDayValue downTo (submissionDayValue - size + 1)).toList().toIntArray()
    }
}
