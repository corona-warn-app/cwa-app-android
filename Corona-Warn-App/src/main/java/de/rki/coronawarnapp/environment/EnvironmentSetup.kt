package de.rki.coronawarnapp.environment

import android.content.Context
import androidx.core.content.edit
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import de.rki.coronawarnapp.environment.EnvironmentSetup.ENVKEY.DOWNLOAD
import de.rki.coronawarnapp.environment.EnvironmentSetup.ENVKEY.SUBMISSION
import de.rki.coronawarnapp.environment.EnvironmentSetup.ENVKEY.VERIFICATION
import de.rki.coronawarnapp.environment.EnvironmentSetup.ENVKEY.VERIFICATION_KEYS
import de.rki.coronawarnapp.util.CWADebug
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentSetup @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PKEY_CURRENT_ENVINROMENT = "environment.current"
    }

    enum class ENVKEY(val rawKey: String) {
        SUBMISSION("SUBMISSION_CDN_URL"),
        VERIFICATION("VERIFICATION_CDN_URL"),
        DOWNLOAD("DOWNLOAD_CDN_URL"),
        VERIFICATION_KEYS("PUB_KEYS_SIGNATURE_VERIFICATION")
    }

    enum class Type(val rawKey: String) {
        PRODUCTION("PROD"),
        INT("INT"),
        DEV("DEV"),
        WRU("WRU"),
        WRU_XA("WRU-XA"), // (aka ACME)
        WRU_XD("WRU-XD") // (aka Germany)
    }

    private val prefs by lazy {
        context.getSharedPreferences("environment_setup", Context.MODE_PRIVATE)
    }

    private val environmentJson: JsonObject by lazy {
        val gson = GsonBuilder().create()
        gson.fromJson(BuildConfigWrap.ENVIRONMENT_JSONDATA, JsonObject::class.java).also {
            Timber.d("Parsed test environment: %s", it)
        }
    }

    val defaultEnvironment: Type
        get() = BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT.toEnvironmentType()

    val alternativeEnvironment: Type
        get() = BuildConfigWrap.ENVIRONMENT_TYPE_ALTERNATIVE.toEnvironmentType()

    var currentEnvironment: Type
        get() {
            return prefs
                .getString(PKEY_CURRENT_ENVINROMENT, null)
                ?.toEnvironmentType() ?: defaultEnvironment
        }
        set(value) {
            if (CWADebug.isDebugBuildOrMode) {
                prefs.edit {
                    putString(PKEY_CURRENT_ENVINROMENT, value.rawKey)
                }
            } else {
                Timber.w("Tried to change currentEnvironment in PRODUCTION mode.")
            }
        }

    private fun getEnvironmentValue(variableKey: ENVKEY): String = run {
        try {
            val targetEnvKey = if (environmentJson.has(currentEnvironment.rawKey)) {
                currentEnvironment.rawKey
            } else {
                Timber.e("Tried to use unavailable environment: $variableKey on $currentEnvironment")
                Type.PRODUCTION.rawKey
            }
            environmentJson
                .getAsJsonObject(targetEnvKey)
                .getAsJsonPrimitive(variableKey.rawKey)
                .asString
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve endpoint URL for $currentEnvironment:$variableKey")
            throw IllegalStateException("Failed to setup test environment", e)
        }
    }.also { Timber.v("getEndpointUrl(endpoint=%s): %s", variableKey, it) }

    val submissionCdnUrl: String
        get() = getEnvironmentValue(SUBMISSION)
    val verificationCdnUrl: String
        get() = getEnvironmentValue(VERIFICATION)
    val downloadCdnUrl: String
        get() = getEnvironmentValue(DOWNLOAD)

    val appConfigVerificationKey: String
        get() = getEnvironmentValue(VERIFICATION_KEYS)

    private fun String.toEnvironmentType(): Type = Type.values().single {
        it.rawKey == this
    }
}
