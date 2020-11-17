@file:Suppress("DEPRECATION")

package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

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

}
