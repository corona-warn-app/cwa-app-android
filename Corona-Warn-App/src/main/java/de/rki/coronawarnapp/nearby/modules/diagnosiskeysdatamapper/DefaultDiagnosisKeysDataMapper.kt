package de.rki.coronawarnapp.nearby.modules.diagnosiskeysdatamapper

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes.FAILED_RATE_LIMITED
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultDiagnosisKeysDataMapper @Inject constructor(
    private val client: ExposureNotificationClient,
    private val appConfigProvider: AppConfigProvider
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

    override suspend fun updateDiagnosisKeysDataMapping() {
        val currentDiagnosisKeysDataMapping = getDiagnosisKeysDataMapping()
        val newDiagnosisKeysDataMapping = appConfigProvider
            .getAppConfig().diagnosisKeyDataMapping

        if (newDiagnosisKeysDataMapping.hasChanged(currentDiagnosisKeysDataMapping)) {
            try {
                Timber.i("New DiagnosisKeysDataMapping differs from last one, applying.")
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

    private fun DiagnosisKeysDataMapping.hasChanged(old: DiagnosisKeysDataMapping?): Boolean {
        // TODO: Make sure this check is enough
        return old == null || old.hashCode() != hashCode()
    }
}
