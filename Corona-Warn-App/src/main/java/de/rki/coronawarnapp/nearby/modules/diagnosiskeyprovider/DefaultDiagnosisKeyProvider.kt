@file:Suppress("DEPRECATION")

package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
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

    override suspend fun provideDiagnosisKeys(
        keyFiles: Collection<File>,
        configuration: ExposureConfiguration?,
        token: String
    ): Boolean {
        return try {
            if (keyFiles.isEmpty()) {
                Timber.tag(TAG).d("No key files submitted, returning early.")
                return true
            }

            val usedConfiguration = if (configuration == null) {
                Timber.tag(TAG).w("Passed configuration was NULL, creating fallback.")
                ExposureConfiguration.ExposureConfigurationBuilder().build()
            } else {
                configuration
            }

            if (googleAPIVersion.isAtLeast(GoogleAPIVersion.V16)) {
                provideKeys(keyFiles, usedConfiguration, token)
            } else {
                provideKeysLegacy(keyFiles, usedConfiguration, token)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(
                e, "Error during provideDiagnosisKeys(keyFiles=%s, configuration=%s, token=%s)",
                keyFiles, configuration, token
            )
            throw e
        }
    }

    private suspend fun provideKeys(
        files: Collection<File>,
        configuration: ExposureConfiguration,
        token: String
    ): Boolean {
        Timber.tag(TAG).d("Using non-legacy key provision.")

        if (!submissionQuota.consumeQuota(1)) {
            Timber.tag(TAG).w("Not enough quota available.")
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
        Timber.tag(TAG).d("Using LEGACY key provision.")

        if (!submissionQuota.consumeQuota(keyFiles.size)) {
            Timber.tag(TAG).w("Not enough quota available.")
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
        Timber.tag(TAG).d("Performing key submission.")
        enfClient
            .provideDiagnosisKeys(keyFiles.toList(), configuration, token)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    companion object {
        private val TAG: String = DefaultDiagnosisKeyProvider::class.java.simpleName
    }
}
