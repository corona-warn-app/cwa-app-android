package de.rki.coronawarnapp.submission.task

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.Symptoms.Indication
import de.rki.coronawarnapp.submission.Symptoms.StartOf
import de.rki.coronawarnapp.util.TimeAndDateExtensions.ageInDays
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import java.time.LocalDate
import javax.inject.Inject

@Reusable
class TransmissionRiskVectorDeterminator @Inject constructor(
    private val timeStamper: TimeStamper
) {

    fun determine(symptoms: Symptoms, now: LocalDate = timeStamper.nowJavaUTC.toLocalDateUtc()) = TransmissionRiskVector(
        when (symptoms.symptomIndication) {
            Indication.POSITIVE -> when (symptoms.startOfSymptoms) {
                is StartOf.Date -> {
                    when (symptoms.startOfSymptoms.date.ageInDays(now)) {
                        0L -> intArrayOf(8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1)
                        1L -> intArrayOf(8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1)
                        2L -> intArrayOf(6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1, 1)
                        3L -> intArrayOf(5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1, 1)
                        4L -> intArrayOf(3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1, 1)
                        5L -> intArrayOf(2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1, 1)
                        6L -> intArrayOf(2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1, 1)
                        7L -> intArrayOf(1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1, 1)
                        8L -> intArrayOf(1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2, 1)
                        9L -> intArrayOf(1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4, 2)
                        10L -> intArrayOf(1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6, 4)
                        11L -> intArrayOf(1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7, 6)
                        12L -> intArrayOf(1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8, 7)
                        13L -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8, 8)
                        14L -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8, 8)
                        15L -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6, 8)
                        16L -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5, 6)
                        17L -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 5)
                        18L -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3)
                        19L -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2)
                        20L -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2)
                        else -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
                    }
                }
                is StartOf.LastSevenDays -> intArrayOf(4, 5, 6, 7, 7, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1)
                is StartOf.MoreThanTwoWeeks -> intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5)
                is StartOf.NoInformation -> intArrayOf(5, 6, 8, 8, 8, 7, 5, 3, 2, 1, 1, 1, 1, 1, 1)
                is StartOf.OneToTwoWeeksAgo -> intArrayOf(1, 1, 1, 1, 2, 3, 4, 5, 6, 6, 7, 7, 6, 6, 4)
                else -> {
                    IllegalStateException("Positive indication without startDate is not allowed: $symptoms")
                        .reportProblem(
                            tag = "TransmissionRiskVectorDeterminator",
                            info = "Symptoms has an invalid state."
                        )
                    intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
                }
            }
            Indication.NEGATIVE -> intArrayOf(4, 4, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
            Indication.NO_INFORMATION -> intArrayOf(5, 6, 7, 7, 7, 6, 4, 3, 2, 1, 1, 1, 1, 1, 1)
        }
    )
}
