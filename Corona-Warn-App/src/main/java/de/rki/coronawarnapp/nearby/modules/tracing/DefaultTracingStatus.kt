package de.rki.coronawarnapp.nearby.modules.tracing

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultTracingStatus @Inject constructor(
    private val client: ExposureNotificationClient
) : TracingStatus {

    override val isTracingEnabled: Flow<Boolean> = callbackFlow<Boolean> {
        var isRunning = true
        while (isRunning) {
            try {
                sendBlocking(pollIsEnabled())
            } catch (e: Exception) {
                Timber.w(e, "ENF isEnabled failed.")
                sendBlocking(false)
                e.report(ExceptionCategory.EXPOSURENOTIFICATION, TAG, null)
                cancel("ENF isEnabled failed", e)
            }
            delay(POLLING_DELAY_MS)
        }
        awaitClose { isRunning = false }
    }
        .distinctUntilChanged()
        .onStart { Timber.v("isTracingEnabled FLOW start") }
        .onEach { Timber.v("isTracingEnabled FLOW emission: %b", it) }
        .onCompletion { Timber.v("isTracingEnabled FLOW completed.") }

    private suspend fun pollIsEnabled(): Boolean = suspendCoroutine { cont ->
        client.isEnabled
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    companion object {
        private const val TAG = "DefaultTracingStatus"
        private const val POLLING_DELAY_MS = 1000L
    }
}
