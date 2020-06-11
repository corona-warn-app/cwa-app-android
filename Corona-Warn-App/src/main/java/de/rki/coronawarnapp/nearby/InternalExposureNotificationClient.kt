package de.rki.coronawarnapp.nearby

import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration.ExposureConfigurationBuilder
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.tracing.TracingIntervalRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.millisecondsToSeconds
import java.io.File
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Wrapper class for the Exposure Notification Client in the com.google.android.gms.nearby.Nearby
 * implementing all Exposure Notification related APIs
 */
object InternalExposureNotificationClient {

    // reference to the client from the Google framework with the given application context
    private val exposureNotificationClient by lazy {
        Nearby.getExposureNotificationClient(CoronaWarnApplication.getAppContext())
    }

    /****************************************************
     * COROUTINE FUNCTIONS
     ****************************************************/
    /**
     * Tells Google Play services to start the broadcasting and scanning process. The first time
     * that this method is called after installation of the app, it prompts Google Play services to
     * display a dialog box, where the user is asked to give permission to broadcast and scan.
     *
     * @return
     */
    suspend fun asyncStart() = suspendCoroutine<Void> { cont ->
        exposureNotificationClient.start()
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
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

    /**
     * Disables broadcasting and scanning. You can call this directly, and it is also called when
     * users uninstall the app. When it’s called as part of the uninstallation process, the
     * database and keys are deleted from the device.
     *
     * @return
     */
    suspend fun asyncStop() = suspendCoroutine<Void> { cont ->
        exposureNotificationClient.stop()
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }.also {
        LocalData.lastNonActiveTracingTimestamp(System.currentTimeMillis())
    }

    /**
     * Indicates if exposure notifications are running
     *
     * @return
     */
    suspend fun asyncIsEnabled(): Boolean = suspendCoroutine { cont ->
        exposureNotificationClient.isEnabled
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    /**
     * Takes an ExposureConfiguration object. Inserts a list of files that contain key
     * information into the on-device database. Provide the keys of confirmed cases retrieved
     * from your internet-accessible server to the Google Play service once requested from the
     * API. Information about the file format is in the Exposure Key Export File Format and
     * Verification document that is linked from google.com/covid19/exposurenotifications.
     *
     * @param keyFiles
     * @param configuration
     * @param token
     * @return
     */
    suspend fun asyncProvideDiagnosisKeys(
        keyFiles: Collection<File>,
        configuration: ExposureConfiguration?,
        token: String
    ): Void = suspendCoroutine { cont ->
        val exposureConfiguration = configuration ?: ExposureConfigurationBuilder().build()
        exposureNotificationClient.provideDiagnosisKeys(
            keyFiles.toList(),
            exposureConfiguration,
            token
        )
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    /**
     * Retrieves key history from the data store on the device for uploading to your
     * internet-accessible server. Calling this method prompts Google Play services to display
     * a dialog, requesting permission from the user to gather and upload their exposure keys.
     * The keys returned include the past 14 days, but not the current day’s key.
     *
     * The permission granted by the user lasts for 24 hours, so the permission dialog appears
     * only once for each 24-hour period, regardless of how many times the method is called
     *
     * @return
     */
    suspend fun asyncGetTemporaryExposureKeyHistory(): List<TemporaryExposureKey> =
        suspendCoroutine { cont ->
            exposureNotificationClient.temporaryExposureKeyHistory
                .addOnSuccessListener {
                    cont.resume(it)
                }.addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }

    /**
     * Retrieves the ExposureSummary object that matches the token from
     * provideDiagnosisKeys() that you provide to the method. The ExposureSummary
     * object provides a high-level overview of the exposure that a user has experienced.
     *
     * @param token
     * @return
     */
    suspend fun asyncGetExposureSummary(token: String): ExposureSummary =
        suspendCoroutine { cont ->
            exposureNotificationClient.getExposureSummary(token)
                .addOnSuccessListener {
                    cont.resume(it)
                }.addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }
}
