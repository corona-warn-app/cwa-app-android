package de.rki.coronawarnapp.datadonation.safetynet

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import timber.log.Timber
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CWASafetyNet @Inject constructor(
    private val client: SafetyNetClientWrapper,
    private val secureRandom: SecureRandom
) : DeviceAttestation {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun generateSalt(): ByteArray = ByteArray(16).apply {
        secureRandom.nextBytes(this)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun calculateNonce(salt: ByteArray, payload: ByteArray): ByteArray {
        val concat = salt + payload
        return concat.toSHA256().toByteArray()
    }

    override suspend fun attest(request: DeviceAttestation.Request): DeviceAttestation.Result {
        val salt = generateSalt()
        val nonce = calculateNonce(salt = salt, payload = request.scenarioPayload)
        Timber.tag(TAG).d("With salt=%s and payload=%s, we created none=%s", salt, request.scenarioPayload, nonce)

        val report = client.attest(nonce)

        return AttestationContainer(salt, report)
    }

    companion object {
        private const val TAG = "CWASafetyNet"
    }
}
