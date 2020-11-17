package de.rki.coronawarnapp.nearby.modules.tracing

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultTracingStatus @Inject constructor(
    private val client: ExposureNotificationClient,
    @AppScope val scope: CoroutineScope
) : TracingStatus {

    override val isTracingEnabled: Flow<Boolean> = callbackFlow<Boolean> {
        while (true) {
            try {
                send(pollIsEnabled())
            } catch (e: Exception) {
                Timber.w(e, "ENF isEnabled failed.")
                send(false)
                e.report(ExceptionCategory.EXPOSURENOTIFICATION, TAG, null)
                cancel("ENF isEnabled failed", e)
            }
            if (!isActive) break
            delay(POLLING_DELAY_MS)
        }
    }
        .distinctUntilChanged()
        .onStart { Timber.v("isTracingEnabled FLOW start") }
        .onEach { Timber.v("isTracingEnabled FLOW emission: %b", it) }
        .onCompletion { Timber.v("isTracingEnabled FLOW completed.") }
        .shareLatest(
            tag = TAG,
            scope = scope
        )

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
