package de.rki.coronawarnapp.environment

import android.content.Context
import androidx.core.content.edit
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvKey.CROWD_NOTIFIER_PUBLIC_KEY
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvKey.DATA_DONATION
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvKey.DCC
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvKey.DOWNLOAD
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvKey.LOG_UPLOAD
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvKey.SAFETYNET_API_KEY
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvKey.SUBMISSION
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvKey.USE_EUR_KEY_PKGS
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvKey.VERIFICATION
import de.rki.coronawarnapp.environment.EnvironmentSetup.EnvKey.VERIFICATION_KEYS
import de.rki.coronawarnapp.environment.EnvironmentSetup.Type.Companion.toEnvironmentType
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseJackson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentSetup @Inject constructor(
    @AppContext private val context: Context,
    @BaseJackson private val objectMapper: ObjectMapper,
) : Initializer {

    enum class EnvKey(val rawKey: String) {
        USE_EUR_KEY_PKGS("USE_EUR_KEY_PKGS"),
        SUBMISSION("SUBMISSION_CDN_URL"),
        VERIFICATION("VERIFICATION_CDN_URL"),
        DOWNLOAD("DOWNLOAD_CDN_URL"),
        VERIFICATION_KEYS("PUB_KEYS_SIGNATURE_VERIFICATION"),
        DATA_DONATION("DATA_DONATION_CDN_URL"),
        LOG_UPLOAD("LOG_UPLOAD_SERVER_URL"),
        SAFETYNET_API_KEY("SAFETYNET_API_KEY"),
        CROWD_NOTIFIER_PUBLIC_KEY("CROWD_NOTIFIER_PUBLIC_KEY"),
        DCC("DCC_SERVER_URL"),
        DCC_REISSUANCE_SERVER_URL("DCC_REISSUANCE_SERVER_URL"),
    }

    enum class Type(val rawKey: String) {
        PRODUCTION("PROD"),
        INT("INT"),
        DEV("DEV"),
        WRU("WRU"),
        WRU_XA("WRU-XA"), // (aka ACME),
        WRU_XD("WRU-XD"), // (aka Germany)
        TESTER_MOCK("TESTER-MOCK"), // (aka Germany)
        LOCAL("LOCAL"), // Emulator/CLI tooling
        MOCK_CLOUD("MOCK-CLOUD"), // Mock-cloud
        MOCK_TSI_MMS("MOCK-TSI-MMS"); // MOCK-TSI-MMS

        companion object {
            internal fun String.toEnvironmentType(): Type = values().single {
                it.rawKey == this
            }
        }
    }

    private val prefs by lazy {
        context.getSharedPreferences("environment_setup", Context.MODE_PRIVATE)
    }

    private val environmentJson: JsonNode by lazy {
        objectMapper.readTree(BuildConfigWrap.ENVIRONMENT_JSONDATA).also {
            Timber.d("Parsed test environment: %s", it)
        }
    }

    val defaultEnvironment: Type
        get() = BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT.toEnvironmentType()

    var currentEnvironment: Type
        get() {
            return prefs
                .getString(PKEY_CURRENT_ENVIRONMENT, null)
                ?.toEnvironmentType() ?: defaultEnvironment
        }
        set(value) {
            if (CWADebug.buildFlavor == CWADebug.BuildFlavor.DEVICE_FOR_TESTERS) {
                Timber.i("Changing currentEnvironment to $value")
                prefs.edit(commit = true) {
                    putString(PKEY_CURRENT_ENVIRONMENT, value.rawKey)
                }
            } else {
                Timber.w("Tried to change currentEnvironment in PRODUCTION mode.")
            }
        }

    var launchEnvironment: JsonNode?
        get() {
            return prefs
                .getString(PKEY_LAUNCHER_ENVIRONMENT_DATA, null)?.let {
                    objectMapper.readTree(it)
                }
        }
        set(value) {
            if (CWADebug.buildFlavor == CWADebug.BuildFlavor.DEVICE_FOR_TESTERS) {
                prefs.edit(commit = true) {
                    if (value == null) {
                        remove(PKEY_LAUNCHER_ENVIRONMENT_DATA)
                    } else {
                        putString(PKEY_LAUNCHER_ENVIRONMENT_DATA, objectMapper.writeValueAsString(value))
                    }
                }
                Timber.i("Changing launchEnvironment to $value")
            } else {
                Timber.w("Tried to change launchEnvironment in PRODUCTION mode.")
            }
        }

    private fun getEnvironmentValue(variableKey: EnvKey): JsonNode = run {
        try {
            val targetEnvKey = if (environmentJson.has(currentEnvironment.rawKey)) {
                currentEnvironment.rawKey
            } else {
                Timber.e("Tried to use unavailable environment: $currentEnvironment")
                Type.PRODUCTION.rawKey
            }

            val value = (launchEnvironment ?: environmentJson.get(targetEnvKey))
                .get(variableKey.rawKey)

            return@run if (value != null) {
                Timber.v("getEnvironmentValue(endpoint=%s): %s", variableKey, value)
                value
            } else {
                throw IllegalStateException("$currentEnvironment:$variableKey is missing in your *_environment.json")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to retrieve $currentEnvironment:$variableKey", e)
        }
    }

    override fun initialize() {
        EnvKey.values().forEach { getEnvironmentValue(it) }
        Timber.i("sanityCheck() - passed")
    }

    val submissionCdnUrl: String
        get() = getEnvironmentValue(SUBMISSION).asText()

    val verificationCdnUrl: String
        get() = getEnvironmentValue(VERIFICATION).asText()

    val downloadCdnUrl: String
        get() = getEnvironmentValue(DOWNLOAD).asText()

    val dataDonationCdnUrl: String
        get() = getEnvironmentValue(DATA_DONATION).asText()

    val appConfigPublicKey: String
        get() = getEnvironmentValue(VERIFICATION_KEYS).asText()

    val useEuropeKeyPackageFiles: Boolean
        get() = getEnvironmentValue(USE_EUR_KEY_PKGS).asBoolean()

    val safetyNetApiKey: String
        get() = getEnvironmentValue(SAFETYNET_API_KEY).asText()

    val crowdNotifierPublicKey: String
        get() = getEnvironmentValue(CROWD_NOTIFIER_PUBLIC_KEY).asText()

    val logUploadServerUrl: String
        get() = getEnvironmentValue(LOG_UPLOAD).asText()

    val dccServerUrl: String
        get() = getEnvironmentValue(DCC).asText()

    val dccReissuanceServerUrl: String
        get() = getEnvironmentValue(EnvKey.DCC_REISSUANCE_SERVER_URL).asText()

    companion object {
        private const val PKEY_CURRENT_ENVIRONMENT = "environment.current"
        private const val PKEY_LAUNCHER_ENVIRONMENT_DATA = "environment.launcher"
    }
}
