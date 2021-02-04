package de.rki.coronawarnapp.datadonation.safetynet

import android.content.Context
import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException.Type
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.gplay.GoogleApiVersion
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CWASafetyNet @Inject constructor(
    @AppContext private val context: Context,
    private val client: SafetyNetClientWrapper,
    private val secureRandom: SecureRandom,
    private val appConfigProvider: AppConfigProvider,
    private val googleApiVersion: GoogleApiVersion,
    private val cwaSettings: CWASettings,
    private val timeStamper: TimeStamper
) : DeviceAttestation {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun generateSalt(): ByteArray = ByteArray(16).apply {
        secureRandom.nextBytes(this)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun calculateNonce(salt: ByteArray, payload: ByteArray): String {
        val concat = salt + payload
        return concat.toSHA256()
    }

    override suspend fun attest(request: DeviceAttestation.Request): DeviceAttestation.Result {
        if (!googleApiVersion.isPlayServicesVersionAvailable(13000000)) {
            throw SafetyNetException(Type.PLAY_SERVICES_VERSION_MISMATCH, "Google Play Services too old.")
        }

        appConfigProvider.getAppConfig().apply {
            if (deviceTimeState == ConfigData.DeviceTimeState.ASSUMED_CORRECT) {
                throw SafetyNetException(Type.DEVICE_TIME_UNVERIFIED, "Device time is unverified")
            }
            if (deviceTimeState == ConfigData.DeviceTimeState.INCORRECT) {
                throw SafetyNetException(Type.DEVICE_TIME_INCORRECT, "Device time is incorrect")
            }
        }

        val firstReliableTimeStamp = cwaSettings.firstReliableDeviceTime
        if (firstReliableTimeStamp == Instant.EPOCH) {
            throw SafetyNetException(Type.TIME_SINCE_ONBOARDING_UNVERIFIED, "No first reliable timestamp available")
        } else if (Duration(firstReliableTimeStamp, timeStamper.nowUTC) < Duration.standardHours(24)) {
            throw SafetyNetException(Type.TIME_SINCE_ONBOARDING_UNVERIFIED, "Time since first reliable timestamp <24h")
        }

        val salt = generateSalt()
        val nonce = calculateNonce(salt = salt, payload = request.scenarioPayload)
        Timber.tag(TAG).d("With salt=%s and payload=%s, we created nonce=%s", salt, request.scenarioPayload, nonce)

        val report = client.attest(nonce.toByteArray())

        report.error?.let {
            Timber.tag(TAG).w("SafetyNet Response has an error message: %s", it)
        }

        if (nonce != report.nonce) {
            throw SafetyNetException(
                Type.NONCE_MISMATCH,
                "Request nonce doesn't match response ($nonce != ${report.nonce})"
            )
        }

        if (context.packageName != report.apkPackageName) {
            throw SafetyNetException(
                Type.APK_PACKAGE_NAME_MISMATCH,
                "Our APK name doesn't match response (${context.packageName} != ${report.apkPackageName})"
            )
        }

        return AttestationContainer(salt, report)
    }

    companion object {
        private const val TAG = "CWASafetyNet"
    }
}
