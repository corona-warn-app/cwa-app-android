package de.rki.coronawarnapp.nearby.modules.tracing

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.tracing.TracingIntervalRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.millisecondsToSeconds
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
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
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
    }.also {
        LocalData.lastNonActiveTracingTimestamp()?.let { ts ->
            TracingIntervalRepository.getDateRepository(CoronaWarnApplication.getAppContext())
                .createInterval(ts, System.currentTimeMillis())
            val difference = Date().time.minus(ts).millisecondsToSeconds()
            if (difference >= TimeVariables.getDeactivationTracingMeasureThresholdTimeRange()) {
                LocalData.totalNonActiveTracing(
                    LocalData.totalNonActiveTracing().plus(difference)
                )
            }
        }
        LocalData.lastNonActiveTracingTimestamp(null)
        LocalData.initialTracingActivationTimestamp()
            ?: LocalData.initialTracingActivationTimestamp(System.currentTimeMillis())
    }

    private suspend fun asyncStop() = suspendCoroutine<Void> { cont ->
        client.stop()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }.also {
        LocalData.lastNonActiveTracingTimestamp(System.currentTimeMillis())
    }

    override val isTracingEnabled: Flow<Boolean> = callbackFlow<Boolean> {
        while (true) {
            try {
                send(isEnabled())
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

    private suspend fun isEnabled(): Boolean = suspendCoroutine { cont ->
        client.isEnabled
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    companion object {
        private const val TAG = "DefaultTracingStatus"
        private const val POLLING_DELAY_MS = 1000L
    }
}
