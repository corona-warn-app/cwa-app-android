package de.rki.coronawarnapp.datadonation.safetynet

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.safetynet.SafetyNetClient
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.Reusable
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException.Type
import de.rki.coronawarnapp.environment.EnvironmentSetup
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import okio.ByteString.Companion.decodeBase64
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Reusable
class SafetyNetClientWrapper @Inject constructor(
    private val safetyNetClient: SafetyNetClient,
    private val environmentSetup: EnvironmentSetup
) {

    suspend fun attest(nonce: ByteArray): Report {
        val response = try {
            withTimeout(30 * 1000L) { callClient(nonce) }
        } catch (e: TimeoutCancellationException) {
            throw SafetyNetException(Type.ATTESTATION_FAILED, "Attestation timeout.", e)
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

    private fun String.decodeBase64Json(): JsonObject {
        val rawJson = decodeBase64()!!.string(Charsets.UTF_8)
        return JsonParser.parseString(rawJson).asJsonObject
    }

    private suspend fun callClient(nonce: ByteArray): SafetyNetApi.AttestationResponse = suspendCoroutine { cont ->
        safetyNetClient.attest(nonce, environmentSetup.safetyNetApiKey)
            .addOnSuccessListener {
                Timber.tag(TAG).v("Attestation finished with %s", it)
                cont.resume(it)
            }
            .addOnFailureListener {
                Timber.tag(TAG).w(it, "Attestation failed.")
                val wrappedError = if (it is ApiException && it.statusCode == CommonStatusCodes.NETWORK_ERROR) {
                    SafetyNetException(Type.ATTESTATION_REQUEST_FAILED, "Network error", it)
                } else {
                    SafetyNetException(Type.ATTESTATION_FAILED, "SafetyNet client returned an error.", it)
                }
                cont.resumeWithException(wrappedError)
            }
    }

    data class Report(
        val jwsResult: String,
        val header: JsonObject,
        val body: JsonObject,
        val signature: ByteArray
    ) {
        val nonce: String? = body.get("nonce")?.asString?.decodeBase64()?.utf8()

        val apkPackageName: String? = body.get("apkPackageName")?.asString

        val basicIntegrity: Boolean = body.get("basicIntegrity")?.asBoolean == true
        val ctsProfileMatch = body.get("ctsProfileMatch")?.asBoolean == true

        val evaluationTypes = body.get("evaluationType")?.asString
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()

        val error: String? = body.get("error")?.asString
        val advice: String? = body.get("advice")?.asString

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
