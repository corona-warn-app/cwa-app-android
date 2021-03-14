package de.rki.coronawarnapp.nearby

import de.rki.coronawarnapp.util.di.AppInjector
import kotlinx.coroutines.flow.first
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Wrapper class for the Exposure Notification Client in the com.google.android.gms.nearby.Nearby
 * implementing all Exposure Notification related APIs
 */
object InternalExposureNotificationClient {

    // reference to the client from the Google framework with the given application context
    private val enfClient by lazy {
        AppInjector.component.enfClient
    }

    /**
     * Disables broadcasting and scanning. You can call this directly, and it is also called when
     * users uninstall the app. When it’s called as part of the uninstallation process, the
     * database and keys are deleted from the device.
     *
     * @return
     */
    suspend fun asyncStop() = suspendCoroutine<Unit> { cont ->
        enfClient.setTracing(
            false,
            onSuccess = { cont.resume(Unit) },
            onError = { cont.resumeWithException(it) },
            onPermissionRequired = { Timber.e("Permission was required to disable tracing?") }
        )
    }

    /**
     * Indicates if exposure notifications are running
     *
     * @return
     */
    suspend fun asyncIsEnabled(): Boolean = enfClient.isTracingEnabled.first()
}
