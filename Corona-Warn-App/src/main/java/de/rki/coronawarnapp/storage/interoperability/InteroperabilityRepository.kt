package de.rki.coronawarnapp.storage.interoperability

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.Country
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteroperabilityRepository @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) {

    val countryList = appConfigProvider.currentConfig
        .map { configData ->
            try {
                configData
                    .supportedCountries
                    .mapNotNull { rawCode ->
                        val countryCode = rawCode.toLowerCase(Locale.ROOT)

                        val mappedCountry = Country.values().singleOrNull { it.code == countryCode }
                        if (mappedCountry == null) Timber.e("Unknown countrycode: %s", rawCode)
                        mappedCountry
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to map country list.")
                emptyList()
            }
        }
        .onEach { Timber.d("Country list: %s", it.joinToString(",")) }

    suspend fun refreshCountries() {
        appConfigProvider.getAppConfig()
    }

    fun saveInteroperabilityUsed() {
        LocalData.isInteroperabilityShownAtLeastOnce = true
    }
}
