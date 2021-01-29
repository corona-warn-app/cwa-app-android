package de.rki.coronawarnapp.datadonation.safetynet

import android.content.Context
import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException.Type
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.gplay.GoogleApiVersion
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
    private val googleApiVersion: GoogleApiVersion
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

//        val appConfig = appConfigProvider.getAppConfig()

        val salt = generateSalt()
        val nonce = calculateNonce(salt = salt, payload = request.scenarioPayload)
        Timber.tag(TAG).d("With salt=%s and payload=%s, we created none=%s", salt, request.scenarioPayload, nonce)

        val report = client.attest(nonce.toByteArray())

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
