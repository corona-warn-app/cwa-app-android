package de.rki.coronawarnapp.environment

import android.content.Context
import androidx.core.content.edit
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import de.rki.coronawarnapp.environment.EnvironmentSetup.EndPoint.DOWNLOAD
import de.rki.coronawarnapp.environment.EnvironmentSetup.EndPoint.SUBMISSION
import de.rki.coronawarnapp.environment.EnvironmentSetup.EndPoint.VERIFICATION
import de.rki.coronawarnapp.util.CWADebug
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentSetup @Inject constructor(
    private val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("environment_setup", Context.MODE_PRIVATE)
    }

    private val environmentJson: JsonObject by lazy {
        val gson = GsonBuilder().create()
        gson.fromJson(BuildConfigWrap.TEST_ENVIRONMENT_JSONDATA, JsonObject::class.java).also {
            Timber.d("Parsed test environment: %s", it)
        }
    }

    val defaultEnvironment: Type by lazy {
        BuildConfigWrap.TEST_ENVIRONMENT_DEFAULTTYPE.toEnvironmentType()
    }

    var currentEnvironment: Type
        get() = if (CWADebug.isDebugBuildOrMode) {
            prefs
                .getString(PKEY_CURRENT_ENVINROMENT, null)
                ?.toEnvironmentType() ?: defaultEnvironment
        } else {
            Type.PRODUCTION
        }
        set(value) {
            prefs.edit {
                putString(PKEY_CURRENT_ENVINROMENT, value.rawKey)
            }
        }

    private fun getEndpointUrl(endpoint: EndPoint): String = if (!CWADebug.isDebugBuildOrMode) {
        when (endpoint) {
            SUBMISSION -> BuildConfigWrap.SUBMISSION_CDN_URL
            VERIFICATION -> BuildConfigWrap.VERIFICATION_CDN_URL
            DOWNLOAD -> BuildConfigWrap.DOWNLOAD_CDN_URL
        }
    } else {
        try {
            environmentJson
                .getAsJsonObject(currentEnvironment.rawKey)
                .getAsJsonPrimitive(endpoint.key)
                .asString
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve endpoint URL for %s", endpoint)
            throw IllegalStateException("Failed to setup test environment", e)
        }
    }.also { Timber.v("getEndpointUrl(endpoint=%s): %s", endpoint, it) }

    val cdnUrlSubmission: String
        get() = getEndpointUrl(SUBMISSION)
    val cdnUrlVerification: String
        get() = getEndpointUrl(VERIFICATION)
    val cdnUrlDownload: String
        get() = getEndpointUrl(DOWNLOAD)

    enum class Type(val rawKey: String) {
        PRODUCTION(""),
        INT("INT"),
        DEV("DEV"),
        WRU("WRU"),
        WRU_XA("WRU-XA"), // (aka ACME)
        WRU_XD("WRU-XD"); //(aka Germany)
    }

    private fun String.toEnvironmentType(): Type = Type.values().single {
        it.rawKey == this
    }

    enum class EndPoint(val key: String) {
        SUBMISSION("SUBMISSION_CDN_URL"),
        VERIFICATION("VERIFICATION_CDN_URL"),
        DOWNLOAD("DOWNLOAD_CDN_URL")
    }

    companion object {
        private const val PKEY_CURRENT_ENVINROMENT = "environment.current"
    }
}
