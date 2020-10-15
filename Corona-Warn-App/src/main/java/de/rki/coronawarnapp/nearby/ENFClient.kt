@file:Suppress("DEPRECATION")

package de.rki.coronawarnapp.nearby

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DiagnosisKeyProvider
import de.rki.coronawarnapp.nearby.modules.tracing.TracingStatus
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ENFClient @Inject constructor(
    private val googleENFClient: ExposureNotificationClient,
    private val diagnosisKeyProvider: DiagnosisKeyProvider,
    private val tracingStatus: TracingStatus
) : DiagnosisKeyProvider, TracingStatus {

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

    override val isTracingEnabled: Flow<Boolean>
        get() = tracingStatus.isTracingEnabled
}
