package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid.ApplicationConfigurationAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.AppFeaturesOuterClass
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CWAConfigMapper @Inject constructor() : CWAConfig.Mapper {
    override fun map(rawConfig: ApplicationConfigurationAndroid): CWAConfig {
        return CWAConfigContainer(
            latestVersionCode = rawConfig.latestVersionCode,
            minVersionCode = rawConfig.minVersionCode,
            supportedCountries = rawConfig.getMappedSupportedCountries(),
            appFeatures = rawConfig.mapAppFeatures()
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

    private fun ApplicationConfigurationAndroid.mapAppFeatures(): List<AppFeaturesOuterClass.AppFeature> =
        if (hasAppFeatures()) {
            val parsedFeatures = mutableListOf<AppFeaturesOuterClass.AppFeature>()
            for (index in 0 until appFeatures.appFeaturesCount) {
                parsedFeatures.add(appFeatures.getAppFeatures(index))
            }
            parsedFeatures
        } else {
            emptyList()
        }

    data class CWAConfigContainer(
        override val latestVersionCode: Long,
        override val minVersionCode: Long,
        override val supportedCountries: List<String>,
        override val appFeatures: List<AppFeaturesOuterClass.AppFeature>
    ) : CWAConfig

    companion object {
        private val VALID_CC = "^([A-Z]{2,3})$".toRegex()
    }
}
