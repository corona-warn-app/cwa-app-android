package de.rki.coronawarnapp.storage.interoperability

import android.text.TextUtils
import androidx.lifecycle.asLiveData
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.Country
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteroperabilityRepository @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) {

    fun saveInteroperabilityUsed() {
        LocalData.isInteroperabilityShownAtLeastOnce = true
    }

    private val countryListFlowInternal = MutableStateFlow(listOf<Country>())
    val countryListFlow: Flow<List<Country>> = countryListFlowInternal

    @Deprecated("Use  countryListFlow")
    val countryList = countryListFlow.asLiveData()

    init {
        getAllCountries()
    }

    /**
     * Gets all countries from @see ApplicationConfigurationService.asyncRetrieveApplicationConfiguration
     * Also changes every country code to lower case
     */
    fun getAllCountries() {
        runBlocking {
            try {
                val countries = appConfigProvider.getAppConfig()
                    .supportedCountriesList
                    .mapNotNull { rawCode ->
                        val countryCode = rawCode.toLowerCase(Locale.ROOT)

                        val mappedCountry = Country.values().singleOrNull { it.code == countryCode }
                        if (mappedCountry == null) Timber.e("Unknown countrycode: %s", rawCode)
                        mappedCountry
                    }
                countryListFlowInternal.value = countries
                Timber.d("Country list: ${TextUtils.join(System.lineSeparator(), countries)}")
            } catch (e: Exception) {
                Timber.e(e)
                countryListFlowInternal.value = emptyList()
            }
        }
    }
}
