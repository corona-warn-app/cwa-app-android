package de.rki.coronawarnapp.submission

import KeyExportFormat
import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey

class ExposureKeyHistoryCalculations(
    private val submissionStatusRepository: SubmissionStatusRepository,
    private val transmissionRiskVectorDeterminator: TransmissionRiskVectorDeterminator,
    private val daysSinceOnsetOfSymptomsVectorDeterminator: DaysSinceOnsetOfSymptomsVectorDeterminator,
    private val keyConverter: KeyConverter
) {

    companion object {
        const val VECTOR_LENGTH = 15
    }

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        symptoms: Symptoms
    ) =
        toExternalFormat(
            limitKeyCount(toSortedHistory(keys)),
            transmissionRiskVectorDeterminator.determine(symptoms),
            daysSinceOnsetOfSymptomsVectorDeterminator.determine(symptoms, VECTOR_LENGTH)
        )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun limitKeyCount(keys: List<TemporaryExposureKey>) =
        keys.filter { it.daysAgo <= 14 }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector,
        daysSinceOnsetOfSymptomsVector: DaysSinceOnsetOfSymptomsVector
    ): List<KeyExportFormat.TemporaryExposureKey> {
        val result = mutableListOf<KeyExportFormat.TemporaryExposureKey>()
        keys.groupBy { it.daysAgo }.forEach { entry ->
            val index = daysSinceOnsetOfSymptomsVector.indexOf(entry.key)
            val today = entry.key == 0
            entry.value.forEach {
                result.add(
                    keyConverter.toExternalFormat(
                        it,
                        transmissionRiskVector.getRiskValue(index),
                        daysSinceOnsetOfSymptomsVector[index]
                    )
                )
            }
            val submissionStatus = submissionStatusRepository.lastSubmission
            if (today && submissionStatus != null && submissionStatus.is15thKeyNeeded) {
                // FIXME create new key
            }
        }
        return result.toList()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toSortedHistory(keys: List<TemporaryExposureKey>) =
        keys.sortedWith(compareByDescending { it.rollingStartIntervalNumber })

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val SubmissionStatus.is15thKeyNeeded: Boolean
        get() = !succeeded

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val TemporaryExposureKey.daysAgo: Int
        get() = rollingStartIntervalNumber // FIXME
}
