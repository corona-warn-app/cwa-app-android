@file:Suppress("DEPRECATION")

package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.exception.reporting.ReportingConstants
import de.rki.coronawarnapp.util.GoogleAPIVersion
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultDiagnosisKeyProvider @Inject constructor(
    private val googleAPIVersion: GoogleAPIVersion,
    private val submissionQuota: SubmissionQuota,
    private val enfClient: ExposureNotificationClient
) : DiagnosisKeyProvider {

    override suspend fun provideDiagnosisKeys(keyFiles: Collection<File>): Boolean {
        if (keyFiles.isEmpty()) {
            Timber.d("No key files submitted, returning early.")
            return true
        }

        if (!googleAPIVersion.isAtLeast(GoogleAPIVersion.V15)) {
            // Actually this shouldn't happen
            Timber.d("No key files submitted because client uses an old unsupported version")
            return false
        }

        if (!submissionQuota.consumeQuota(1)) {
            Timber.w("No key files submitted because not enough quota available.")
        }

        return suspendCoroutine { cont ->
            Timber.d("Performing key submission.")
            enfClient
                .provideDiagnosisKeys(keyFiles.toList())
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    private suspend fun provideKeys(
        files: Collection<File>,
        configuration: ExposureConfiguration,
        token: String
    ): Boolean {
        Timber.d("Using non-legacy key provision.")

        if (!submissionQuota.consumeQuota(1)) {
            Timber.w("Not enough quota available.")
            // TODO Currently only logging, we'll be more strict in a future release
            // return false
        }

        performSubmission(files, configuration, token)
        return true
    }

    /**
     * We use Batch Size 1 and thus submit multiple times to the API.
     * This means that instead of directly submitting all files at once, we have to split up
     * our file list as this equals a different batch for Google every time.
     */
    private suspend fun provideKeysLegacy(
        keyFiles: Collection<File>,
        configuration: ExposureConfiguration,
        token: String
    ): Boolean {
        Timber.d("Using LEGACY key provision.")

        if (!submissionQuota.consumeQuota(keyFiles.size)) {
            Timber.w("Not enough quota available.")
            // TODO What about proceeding with partial submission?
            // TODO Currently only logging, we'll be more strict in a future release
            // return false
        }

        keyFiles.forEach { performSubmission(listOf(it), configuration, token) }
        return true
    }

    private suspend fun performSubmission(
        keyFiles: Collection<File>,
        configuration: ExposureConfiguration,
        token: String
    ): Void = suspendCoroutine { cont ->
        Timber.d("Performing key submission.")
        enfClient
            .provideDiagnosisKeys(keyFiles.toList(), configuration, token)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener {
                val wrappedException = when {
                    it is ApiException && it.statusCode == ReportingConstants.STATUS_CODE_REACHED_REQUEST_LIMIT -> {
                        QuotaExceededException(cause = it)
                    }
                    else -> it
                }
                cont.resumeWithException(wrappedException)
            }
    }
}
