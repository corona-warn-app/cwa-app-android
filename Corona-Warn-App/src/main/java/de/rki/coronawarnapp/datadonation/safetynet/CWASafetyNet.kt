package de.rki.coronawarnapp.datadonation.safetynet

import android.content.Context
import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException.Type
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.gplay.GoogleApiVersion
import de.rki.coronawarnapp.util.security.RandomStrong
import kotlinx.coroutines.flow.first
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import java.time.Duration
import java.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class CWASafetyNet @Inject constructor(
    @AppContext private val context: Context,
    private val client: SafetyNetClientWrapper,
    @RandomStrong private val randomSource: Random,
    private val appConfigProvider: AppConfigProvider,
    private val googleApiVersion: GoogleApiVersion,
    private val cwaSettings: CWASettings,
    private val timeStamper: TimeStamper,
    private val testSettings: TestSettings
) : DeviceAttestation {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun generateSalt(): ByteArray = ByteArray(16).apply {
        randomSource.nextBytes(this)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun calculateNonce(salt: ByteArray, payload: ByteArray): ByteString {
        val concat = salt + payload
        // Default format is hex.
        return concat.toSHA256().decodeHex()
    }

    override suspend fun attest(request: DeviceAttestation.Request): DeviceAttestation.Result {
        if (!googleApiVersion.isPlayServicesVersionAvailable(13000000)) {
            throw SafetyNetException(Type.PLAY_SERVICES_VERSION_MISMATCH, "Google Play Services too old.")
        }

        if (request.checkDeviceTime) {
            Timber.tag(TAG).d("Checking device time.")
            requireCorrectDeviceTime(request.configData)
        } else {
            Timber.tag(TAG).d("Device time check not required.")
        }

        val salt = generateSalt()
        val nonce = calculateNonce(salt = salt, payload = request.scenarioPayload)
        Timber.tag(TAG).d(
            "With salt=%s and payload=%s, we created nonce=%s",
            salt.toByteString().base64(),
            request.scenarioPayload.toByteString().base64(),
            nonce
        )

        val report = client.attest(nonce.toByteArray())

        report.error?.let {
            Timber.tag(TAG).w("SafetyNet Response has an error message: $it")

            if (it == "internal_error") {
                throw SafetyNetException(
                    Type.INTERNAL_ERROR,
                    "Internal error occurred. Retry."
                )
            }
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

    private suspend fun requireCorrectDeviceTime(suppliedConfig: ConfigData?) {
        val configData = suppliedConfig ?: appConfigProvider.getAppConfig()

        configData.apply {
            if (deviceTimeState == ConfigData.DeviceTimeState.ASSUMED_CORRECT) {
                throw SafetyNetException(Type.DEVICE_TIME_UNVERIFIED, "Device time is unverified")
            }
            if (deviceTimeState == ConfigData.DeviceTimeState.INCORRECT) {
                throw SafetyNetException(Type.DEVICE_TIME_INCORRECT, "Device time is incorrect")
            }
        }

        val skip24hCheck = CWADebug.isDeviceForTestersBuild && testSettings.skipSafetyNetTimeCheck.first()
        val nowUTC = timeStamper.nowUTC
        val firstReliableTimeStamp = cwaSettings.firstReliableDeviceTime.first()
        val timeSinceOnboarding = Duration.between(firstReliableTimeStamp, nowUTC)
        Timber.d("firstReliableTimeStamp=%s, now=%s", firstReliableTimeStamp, nowUTC)
        Timber.d("skip24hCheck=%b, timeSinceOnboarding=%dh", skip24hCheck, timeSinceOnboarding.toHours())

        if (firstReliableTimeStamp == Instant.EPOCH) {
            throw SafetyNetException(Type.TIME_SINCE_ONBOARDING_UNVERIFIED, "No first reliable timestamp available")
        } else if (!skip24hCheck && timeSinceOnboarding < Duration.ofHours(24)) {
            throw SafetyNetException(Type.TIME_SINCE_ONBOARDING_UNVERIFIED, "Time since first reliable timestamp <24h")
        }
    }

    companion object {
        private const val TAG = "CWASafetyNet"
    }
}
