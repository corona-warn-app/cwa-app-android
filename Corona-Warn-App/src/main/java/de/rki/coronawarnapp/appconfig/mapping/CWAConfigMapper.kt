package de.rki.coronawarnapp.appconfig.mapping

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.AppFeaturesOuterClass
import de.rki.coronawarnapp.server.protocols.internal.AppVersionConfig
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CWAConfigMapper @Inject constructor() : CWAConfig.Mapper {
    override fun map(rawConfig: AppConfig.ApplicationConfiguration): CWAConfig {
        return CWAConfigContainer(
            appVersion = rawConfig.appVersion,
            supportedCountries = rawConfig.getMappedSupportedCountries(),
            appFeatureus = rawConfig.appFeatures
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun AppConfig.ApplicationConfiguration.getMappedSupportedCountries(): List<String> =
        when {
            supportedCountriesList == null -> emptyList()
            supportedCountriesList.size == 1 && !VALID_CC.matches(supportedCountriesList.single()) -> {
                Timber.w("Invalid country data, clearing. (%s)", supportedCountriesList)
                emptyList()
            }
            else -> supportedCountriesList
        }

    data class CWAConfigContainer(
        override val appVersion: AppVersionConfig.ApplicationVersionConfiguration,
        override val supportedCountries: List<String>,
        override val appFeatureus: AppFeaturesOuterClass.AppFeatures
    ) : CWAConfig

    companion object {
        private val VALID_CC = "^([A-Z]{2,3})$".toRegex()
    }
}
