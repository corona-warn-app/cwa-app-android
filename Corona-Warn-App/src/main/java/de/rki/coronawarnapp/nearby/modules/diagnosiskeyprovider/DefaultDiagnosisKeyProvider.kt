package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import com.google.android.gms.nearby.exposurenotification.DiagnosisKeyFileProvider
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.exception.reporting.ReportingConstants
import de.rki.coronawarnapp.nearby.modules.diagnosiskeysdatamapper.DiagnosisKeysDataMapper
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultDiagnosisKeyProvider @Inject constructor(
    private val enfVersion: ENFVersion,
    private val submissionQuota: SubmissionQuota,
    private val enfClient: ExposureNotificationClient,
    private val diagnosisKeysDataMapper: DiagnosisKeysDataMapper
) : DiagnosisKeyProvider {

    override suspend fun provideDiagnosisKeys(
        keyFiles: Collection<File>,
        newDiagnosisKeysDataMapping: DiagnosisKeysDataMapping
    ): Boolean {
        diagnosisKeysDataMapper.updateDiagnosisKeysDataMapping(newDiagnosisKeysDataMapping)

        if (keyFiles.isEmpty()) {
            Timber.d("No key files submitted, returning early.")
            return true
        }

        // Check version of ENF, WindowMode since v1.5, but version check since v1.6
        // Will throw if requirement is not satisfied
        enfVersion.requireMinimumVersion(ENFVersion.V1_6)

        if (!submissionQuota.consumeQuota(1)) {
            Timber.e("No key files submitted because not enough quota available.")
            // Needs discussion until armed, concerns: Hiding other underlying issues.
//            return false
        }

        val keyFilesList = keyFiles.toList()
        val provideDiagnosisKeysTask = if (enfVersion.isAtLeast(ENFVersion.V1_7)) {
            Timber.i("Provide diagnosis keys with DiagnosisKeyFileProvider")
            val diagnosisKeyFileProvider = DiagnosisKeyFileProvider(keyFilesList)
            enfClient.provideDiagnosisKeys(diagnosisKeyFileProvider)
        } else {
            Timber.i("Provide diagnosis keys as list")
            enfClient.provideDiagnosisKeys(keyFilesList)
        }

        return suspendCoroutine { cont ->
            Timber.d("Performing key submission.")
            provideDiagnosisKeysTask
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener {
                    Timber.w("Key submission failed because ${it.message}")
                    val wrappedException =
                        when (it is ApiException &&
                            it.statusCode == ReportingConstants.STATUS_CODE_REACHED_REQUEST_LIMIT) {
                            true -> QuotaExceededException(cause = it)
                            false -> it
                        }
                    cont.resumeWithException(wrappedException)
                }
        }
    }
}
