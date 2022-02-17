package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid.ApplicationConfigurationAndroid
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CWAConfigMapper @Inject constructor() : CWAConfig.Mapper {

    override fun map(rawConfig: ApplicationConfigurationAndroid): CWAConfig {
        return CWAConfigContainer(
            latestVersionCode = rawConfig.latestVersionCode,
            minVersionCode = rawConfig.minVersionCode,
            supportedCountries = rawConfig.getMappedSupportedCountries(),
            isDeviceTimeCheckEnabled = !rawConfig.isDeviceTimeCheckDisabled(),
            isUnencryptedCheckInsEnabled = rawConfig.isUnencryptedCheckInsEnabled(),
            validationServiceMinVersion = rawConfig.validationServiceMinVersionCode(),
            dccPersonCountMax = rawConfig.dccPersonCountMax(),
            dccPersonWarnThreshold = rawConfig.dccPersonWarnThreshold(),
            admissionScenariosEnabled = !rawConfig.dccAdmissionCheckScenariosDisabled()
        )
    }

    private fun ApplicationConfigurationAndroid.getMappedSupportedCountries(): List<String> =
        when {
            supportedCountriesList == null -> emptyList()
            supportedCountriesList.size == 1 && !VALID_CC.matches(supportedCountriesList.single()) -> {
                Timber.w("Invalid country data, clearing. (%s)", supportedCountriesList)
                emptyList()
            }
            else -> supportedCountriesList
        }

    private fun ApplicationConfigurationAndroid.isDeviceTimeCheckDisabled() = findBoolean(
        labelValue = "disable-device-time-check",
        defaultValue = false
    )

    private fun ApplicationConfigurationAndroid.isUnencryptedCheckInsEnabled() = findBoolean(
        labelValue = "unencrypted-checkins-enabled",
        defaultValue = false
    )

    private fun ApplicationConfigurationAndroid.validationServiceMinVersionCode(): Int {
        if (!hasAppFeatures()) return DEFAULT_VALIDATION_SERVICE_MIN_VERSION
        return try {
            (0 until appFeatures.appFeaturesCount)
                .map { appFeatures.getAppFeatures(it) }
                .firstOrNull { it.label == "validation-service-android-min-version-code" }?.value
                ?: DEFAULT_VALIDATION_SERVICE_MIN_VERSION
        } catch (e: Exception) {
            Timber.e(e, "Failed to find `validation-service-android-min-version-code` from %s", this)
            DEFAULT_VALIDATION_SERVICE_MIN_VERSION
        }
    }

    private fun ApplicationConfigurationAndroid.dccPersonWarnThreshold(): Int {
        if (!hasAppFeatures()) return DCC_PERSON_WARN_THRESHOLD
        return try {
            (0 until appFeatures.appFeaturesCount)
                .map { appFeatures.getAppFeatures(it) }
                .firstOrNull { it.label == "dcc-person-warn-threshold" }?.value
                ?.takeIf { it >= 0 }
                ?: DCC_PERSON_WARN_THRESHOLD
        } catch (e: Exception) {
            Timber.e(e, "Failed to find `dcc-person-warn-threshold` from %s", this)
            DCC_PERSON_WARN_THRESHOLD
        }
    }

    private fun ApplicationConfigurationAndroid.dccPersonCountMax(): Int {
        if (!hasAppFeatures()) return DCC_PERSON_COUNT_MAX
        return try {
            (0 until appFeatures.appFeaturesCount)
                .map { appFeatures.getAppFeatures(it) }
                .firstOrNull { it.label == "dcc-person-count-max" }?.value
                ?.takeIf { it >= 0 }
                ?: DCC_PERSON_COUNT_MAX
        } catch (e: Exception) {
            Timber.e(e, "Failed to find `dcc-person-count-max` from %s", this)
            DCC_PERSON_COUNT_MAX
        }
    }

    private fun ApplicationConfigurationAndroid.dccAdmissionCheckScenariosDisabled() = findBoolean(
        labelValue = "dcc-admission-check-scenarios-disabled",
        defaultValue = DCC_ADMISSION_CHECK_SCENARIOS_DISABLED
    )

    private fun ApplicationConfigurationAndroid.findBoolean(labelValue: String, defaultValue: Boolean): Boolean {
        if (!hasAppFeatures()) return defaultValue

        return try {
            val value = (0 until appFeatures.appFeaturesCount)
                .map { appFeatures.getAppFeatures(it) }
                .firstOrNull { it.label == labelValue }
                ?.value

            return when (value) {
                null -> defaultValue // no item found
                1 -> true // found item has value '1'
                else -> defaultValue // found item has value different to '1'
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to find `%s` from %s", labelValue, this)
            defaultValue
        }
    }

    data class CWAConfigContainer(
        override val latestVersionCode: Long,
        override val minVersionCode: Long,
        override val supportedCountries: List<String>,
        override val isDeviceTimeCheckEnabled: Boolean,
        override val isUnencryptedCheckInsEnabled: Boolean,
        override val validationServiceMinVersion: Int,
        override val dccPersonWarnThreshold: Int,
        override val dccPersonCountMax: Int,
        override val admissionScenariosEnabled: Boolean,
    ) : CWAConfig

    companion object {
        private val VALID_CC = "^([A-Z]{2,3})$".toRegex()
        private const val DEFAULT_VALIDATION_SERVICE_MIN_VERSION = 0

        private const val DCC_PERSON_WARN_THRESHOLD: Int = 10
        private const val DCC_PERSON_COUNT_MAX: Int = 20

        private const val DCC_ADMISSION_CHECK_SCENARIOS_DISABLED = false
    }
}
