package de.rki.coronawarnapp.nearby.modules.diagnosiskeysdatamapper

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes.FAILED_RATE_LIMITED
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultDiagnosisKeysDataMapper @Inject constructor(
    private val client: ExposureNotificationClient
) : DiagnosisKeysDataMapper {
    private suspend fun getDiagnosisKeysDataMapping(): DiagnosisKeysDataMapping? =
        suspendCoroutine { cont ->
            client.diagnosisKeysDataMapping
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private suspend fun setDiagnosisKeysDataMapping(diagnosisKeysDataMapping: DiagnosisKeysDataMapping) =
        suspendCoroutine<Unit> { cont ->
            client.setDiagnosisKeysDataMapping(diagnosisKeysDataMapping)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    override suspend fun updateDiagnosisKeysDataMapping(newDiagnosisKeysDataMapping: DiagnosisKeysDataMapping) {
        val currentDiagnosisKeysDataMapping =
            try {
                getDiagnosisKeysDataMapping()
            } catch (e: Exception) {
                Timber.e("Failed to get the current DiagnosisKeysDataMapping assuming none present")
                null
            }

        if (newDiagnosisKeysDataMapping.hasChanged(currentDiagnosisKeysDataMapping)) {
            try {
                Timber.i(
                    "Current DiagnosisKeysDataMapping: %s vs new: %s, applying new version.",
                    currentDiagnosisKeysDataMapping,
                    newDiagnosisKeysDataMapping
                )
                setDiagnosisKeysDataMapping(newDiagnosisKeysDataMapping)
            } catch (e: ApiException) {
                if (e.statusCode == FAILED_RATE_LIMITED) {
                    Timber.e(e, "Failed to setDiagnosisKeysDataMapping due to rate limit ")
                } else {
                    throw e
                }
            }
        }
    }

    companion object {
        fun DiagnosisKeysDataMapping?.hasChanged(old: DiagnosisKeysDataMapping?): Boolean {
            return old == null || old.hashCode() != hashCode()
        }
    }
}
