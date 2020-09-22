@file:Suppress("DEPRECATION")

package de.rki.coronawarnapp.nearby

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DiagnosisKeyProvider
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ENFClient @Inject constructor(
    private val googleENFClient: ExposureNotificationClient,
    private val diagnosisKeyProvider: DiagnosisKeyProvider
) : DiagnosisKeyProvider {

    // TODO Remove this once we no longer need direct access to the ENF Client,
    // i.e. in **[InternalExposureNotificationClient]**
    internal val internalClient: ExposureNotificationClient
        get() = googleENFClient

    override suspend fun provideDiagnosisKeys(
        keyFiles: Collection<File>,
        configuration: ExposureConfiguration?,
        token: String
    ): Boolean {
        Timber.d(
            "asyncProvideDiagnosisKeys(keyFiles=%s, configuration=%s, token=%s)",
            keyFiles, configuration, token
        )
        return diagnosisKeyProvider.provideDiagnosisKeys(keyFiles, configuration, token)
    }
}
