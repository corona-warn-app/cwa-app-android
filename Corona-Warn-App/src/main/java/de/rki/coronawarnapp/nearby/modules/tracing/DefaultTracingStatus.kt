package de.rki.coronawarnapp.nearby.modules.tracing

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultTracingStatus @Inject constructor(
    private val client: ExposureNotificationClient,
    private val tracingSettings: TracingSettings,
    @AppScope val scope: CoroutineScope
) : TracingStatus {

    override fun setTracing(
        enable: Boolean,
        onSuccess: (Boolean) -> Unit,
        onError: (Throwable) -> Unit,
        onPermissionRequired: (Status) -> Unit
    ) {
        scope.launch {
            try {
                if (enable) {
                    if (isEnabled()) {
                        onSuccess(true)
                    } else {
                        asyncStart()
                        onSuccess(true)
                    }
                } else {
                    if (!isEnabled()) {
                        onSuccess(false)
                    } else {
                        asyncStop()
                        onSuccess(false)
                    }
                }
            } catch (e: Exception) {
                if (e is ApiException && e.statusCode == ExposureNotificationStatusCodes.RESOLUTION_REQUIRED) {
                    Timber.tag(TAG).i(e, "Permission needs to be granted by user.")
                    onPermissionRequired(e.status)
                } else {
                    Timber.tag(TAG).e(e, "Failed to change tracing state.")
                    onError(e)
                }
            }
        }
    }

    private suspend fun asyncStart() = suspendCoroutine<Void> { cont ->
        client.start()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
            .also {
                tracingSettings.isConsentGiven = true
            }
    }

    private suspend fun asyncStop() = suspendCoroutine<Void> { cont ->
        client.stop()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    @Suppress("LoopWithTooManyJumpStatements")
    override val isTracingEnabled: Flow<Boolean> = flow {
        while (true) {
            try {
                emit(isEnabled())
                delay(POLLING_DELAY_MS)
            } catch (e: CancellationException) {
                Timber.tag(TAG).d("isBackgroundRestricted was cancelled")
                break
            } catch (e: ApiException) {
                emit(false)
                if (e.statusCode == 17) {
                    // No ENS installed, no need to keep polling.
                    Timber.tag(TAG).v("No ENS available, aborting polling, assuming permanent.")
                    break
                } else {
                    Timber.tag(TAG).v("Polling failed, will retry with backoff.")
                    delay(POLLING_DELAY_MS * 5)
                }
            }
        }
    }
        .distinctUntilChanged()
        .onStart { Timber.tag(TAG).v("isTracingEnabled FLOW start") }
        .onEach { Timber.tag(TAG).v("isTracingEnabled FLOW emission: %b", it) }
        .onCompletion { if (it == null) Timber.tag(TAG).v("isTracingEnabled FLOW completed.") }
        .catch {
            Timber.tag(TAG).w(it, "ENF isEnabled failed.")
            it.report(ExceptionCategory.EXPOSURENOTIFICATION, TAG, null)
            emit(false)
        }
        .shareLatest(
            tag = TAG,
            scope = scope
        )

    private suspend fun isEnabled(): Boolean = suspendCoroutine { cont ->
        client.isEnabled
            .addOnSuccessListener {
                Timber.tag(TAG).v("Tracing isEnabled=$it")
                cont.resume(it)
            }
            .addOnFailureListener {
                Timber.tag(TAG).w(it, "Failed to determine tracing status.")
                cont.resumeWithException(it)
            }
    }

    companion object {
        private const val TAG = "DefaultTracingStatus"
        private const val POLLING_DELAY_MS = 1000L
    }
}
