package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid.ApplicationConfigurationAndroid
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.CWADebug
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CWAConfigMapper @Inject constructor(
    private val testSettings: TestSettings
) : CWAConfig.Mapper {

    override fun map(rawConfig: ApplicationConfigurationAndroid): CWAConfig {
        return CWAConfigContainer(
            latestVersionCode = rawConfig.latestVersionCode,
            minVersionCode = rawConfig.minVersionCode,
            supportedCountries = rawConfig.getMappedSupportedCountries(),
            isDeviceTimeCheckEnabled = !rawConfig.isDeviceTimeCheckDisabled()
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

    private fun ApplicationConfigurationAndroid.isDeviceTimeCheckDisabled(): Boolean {
        if (CWADebug.isDeviceForTestersBuild && testSettings.isDeviceTimeCheckDisabled.value) {
            Timber.w("Device time check is disabled via test settings!")
            return true
        }
        if (!hasAppFeatures()) return false

        return try {
            (0 until appFeatures.appFeaturesCount)
                .map { appFeatures.getAppFeatures(it) }
                .firstOrNull { it.label == "disable-device-time-check" }
                ?.let { it.value == 1 }
                ?: false
        } catch (e: Exception) {
            Timber.e(e, "Failed to map `disable-device-time-check` from %s", this)
            false
        }
    }

    data class CWAConfigContainer(
        override val latestVersionCode: Long,
        override val minVersionCode: Long,
        override val supportedCountries: List<String>,
        override val isDeviceTimeCheckEnabled: Boolean
    ) : CWAConfig

    companion object {
        private val VALID_CC = "^([A-Z]{2,3})$".toRegex()
    }
}
