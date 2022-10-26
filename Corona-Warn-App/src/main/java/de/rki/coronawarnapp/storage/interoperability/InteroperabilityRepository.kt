package de.rki.coronawarnapp.storage.interoperability

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteroperabilityRepository @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    private val settings: CWASettings,
    networkStateProvider: NetworkStateProvider,
) {

    private val hasInternetFlow = networkStateProvider.networkState
        .map { it.isInternetAvailable }
        .distinctUntilChanged()
        .onEach { hasInternet ->
            // Refresh appConfig on false -> true changes
            if (hasInternet) {
                Timber.v("Trying app config refresh for interop country list.")
                appConfigProvider.getAppConfig()
            }
        }

    val countryList = combine(
        appConfigProvider.currentConfig,
        hasInternetFlow,
    ) { configData, _ ->
        try {
            configData
                .supportedCountries
                .mapNotNull { rawCode ->
                    val countryCode = rawCode.lowercase()

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

    suspend fun saveInteroperabilityUsed() {
        settings.updateWasInteroperabilityShownAtLeastOnce(true)
    }
}
