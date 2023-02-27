package de.rki.coronawarnapp.datadonation.safetynet

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.safetynet.SafetyNetClient
import dagger.Reusable
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException.Type
import de.rki.coronawarnapp.environment.EnvironmentSetup
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Reusable
class SafetyNetClientWrapper @Inject constructor(
    private val safetyNetClient: SafetyNetClient,
    private val environmentSetup: EnvironmentSetup
) {

    suspend fun attest(nonce: ByteArray): Report {
        val response = try {
            withTimeout(180_000) { callClient(nonce) }
        } catch (e: TimeoutCancellationException) {
            throw SafetyNetException(Type.ATTESTATION_REQUEST_FAILED, "Attestation timeout (us).", e)
        }

        val jwsResult = response.jwsResult ?: throw SafetyNetException(
            Type.ATTESTATION_FAILED,
            "JWS was null"
        )

        val components = jwsResult.split(".")
        if (components.size != 3) throw SafetyNetException(
            Type.ATTESTATION_FAILED,
            "Invalid JWS: Components are missing."
        )

        val header = try {
            components[0].decodeBase64Json()
        } catch (e: Exception) {
            throw SafetyNetException(Type.ATTESTATION_FAILED, "Failed to decode JWS header.", e)
        }
        val body = try {
            components[1].decodeBase64Json()
        } catch (e: Exception) {
            throw SafetyNetException(Type.ATTESTATION_FAILED, "Failed to decode JWS body.", e)
        }

        val signature = try {
            components[2].decodeBase64()!!.toByteArray()
        } catch (e: Exception) {
            throw SafetyNetException(Type.ATTESTATION_FAILED, "Failed to decode JWS signature.", e)
        }

        return Report(
            jwsResult = jwsResult,
            header = header,
            body = body,
            signature = signature
        )
    }

    private fun String.decodeBase64Json(): JsonNode {
        val rawJson = decodeBase64()!!.string(Charsets.UTF_8)
        return ObjectMapper().readTree(rawJson)
    }

    private suspend fun callClient(nonce: ByteArray): SafetyNetApi.AttestationResponse =
        suspendCancellableCoroutine { cont ->
            safetyNetClient.attest(nonce, environmentSetup.safetyNetApiKey)
                .addOnSuccessListener {
                    Timber.tag(TAG).v("Attestation finished with %s", it)
                    cont.resume(it)
                }
                .addOnFailureListener {
                    Timber.tag(TAG).w(it, "Attestation failed.")
                    val defaultError =
                        SafetyNetException(Type.ATTESTATION_FAILED, "SafetyNet client returned an error.", it)

                    if (it !is ApiException) {
                        cont.resumeWithException(defaultError)
                        return@addOnFailureListener
                    }

                    val apiError = when (it.statusCode) {
                        CommonStatusCodes.TIMEOUT -> SafetyNetException(
                            Type.ATTESTATION_REQUEST_FAILED,
                            "Timeout (them)",
                            it
                        )
                        // com.google.android.gms.common.api.ApiException: 20:
                        // The connection to Google Play services was lost due to service disconnection.
                        // Last reason for disconnect: Timing out service connection.
                        CommonStatusCodes.CONNECTION_SUSPENDED_DURING_CALL,
                        CommonStatusCodes.RECONNECTION_TIMED_OUT_DURING_UPDATE,
                        CommonStatusCodes.RECONNECTION_TIMED_OUT,
                        CommonStatusCodes.NETWORK_ERROR -> SafetyNetException(
                            Type.ATTESTATION_REQUEST_FAILED,
                            "Network error (${it.statusCode})",
                            it
                        )
                        else -> defaultError
                    }

                    cont.resumeWithException(apiError)
                }
        }

    data class Report(
        val jwsResult: String,
        val header: JsonNode,
        val body: JsonNode,
        val signature: ByteArray
    ) {
        val nonce: ByteString? = body.get("nonce")?.asText()?.decodeBase64()

        val apkPackageName: String? = body.get("apkPackageName")?.asText()

        val basicIntegrity: Boolean = body.get("basicIntegrity")?.asBoolean() == true
        val ctsProfileMatch = body.get("ctsProfileMatch")?.asBoolean() == true

        val evaluationTypes = body.get("evaluationType")?.asText()
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()

        val error: String? = body.get("error")?.asText()
        val advice: String? = body.get("advice")?.asText()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Report
            if (jwsResult != other.jwsResult) return false
            return true
        }

        override fun hashCode(): Int = jwsResult.hashCode()
    }

    companion object {
        private const val TAG = "SafetyNetWrapper"
    }
}
