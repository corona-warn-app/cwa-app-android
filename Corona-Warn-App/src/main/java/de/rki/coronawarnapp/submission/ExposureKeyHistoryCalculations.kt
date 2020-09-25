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
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal fun toSortedHistory(keys: List<TemporaryExposureKey>) =
            keys.sortedWith(compareByDescending { it.rollingStartIntervalNumber })

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val SubmissionStatus.is15thKeyNeeded: Boolean
            get() = !succeeded

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val TemporaryExposureKey.daysAgo: Int
            get() = rollingStartIntervalNumber // FIXME

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun DaysSinceOnsetOfSymptomsVector.indexOf(
            temporaryExposureKey: TemporaryExposureKey
        ) = indexOf(temporaryExposureKey.daysAgo)
    }

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        symptoms: Symptoms
    ) =
        toExternalFormat(
            limitKeyCount(toSortedHistory(keys)),
            transmissionRiskVectorDeterminator.determine(symptoms),
            daysSinceOnsetOfSymptomsVectorDeterminator.determine(symptoms, keys.size)
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
        submissionStatusRepository.lastSubmission.also { submissionStatus ->
            return if (submissionStatus != null && submissionStatus.is15thKeyNeeded) {
                emptyList() // FIXME
            } else keys.map {
                val index = daysSinceOnsetOfSymptomsVector.indexOf(it)
                keyConverter.toExternalFormat(
                    it,
                    transmissionRiskVector.getRiskValue(index),
                    daysSinceOnsetOfSymptomsVector[index]
                )
            }
        }
    }
}
