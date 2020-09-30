package de.rki.coronawarnapp.submission

import dagger.Reusable
import de.rki.coronawarnapp.util.TimeAndDateExtensions.ageInDays
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.TimeStamper
import javax.inject.Inject

@Reusable
class DaysSinceOnsetOfSymptomsVectorDeterminator @Inject constructor(
    private val timeStamper: TimeStamper
) {

    @Suppress("MagicNumber")
    internal fun determine(symptoms: Symptoms): DaysSinceOnsetOfSymptomsVector {
        return when (symptoms.symptomIndication) {
            Symptoms.Indication.POSITIVE ->
                determinePositiveIndication(symptoms)
            Symptoms.Indication.NO_INFORMATION ->
                createDaysSinceOnsetOfSymptomsVectorWith(4000)
            Symptoms.Indication.NEGATIVE ->
                createDaysSinceOnsetOfSymptomsVectorWith(3000)
        }
    }

    @Suppress("MagicNumber")
    private fun determinePositiveIndication(symptoms: Symptoms): DaysSinceOnsetOfSymptomsVector {
        return when (symptoms.startOfSymptoms) {
            is Symptoms.StartOf.Date ->
                createDaysSinceOnsetOfSymptomsVectorWith(
                    symptoms.startOfSymptoms.date.ageInDays(timeStamper.nowUTC.toLocalDate())
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

    private fun createDaysSinceOnsetOfSymptomsVectorWith(
        submissionDayValue: Int,
        size: Int = VECTOR_LENGTH
    ): DaysSinceOnsetOfSymptomsVector {
        return (submissionDayValue downTo (submissionDayValue - size + 1)).toList().toIntArray()
    }

    companion object {
        private const val VECTOR_LENGTH = 15
    }
}
