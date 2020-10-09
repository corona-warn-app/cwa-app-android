package de.rki.coronawarnapp.storage.interoperability

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.Country
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

    private val _countryList: MutableLiveData<List<Country>> = MutableLiveData(listOf())
    val countryList = Transformations.distinctUntilChanged(_countryList)

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
                _countryList.postValue(countries)
                Timber.d("Country list: ${TextUtils.join(System.lineSeparator(), countries)}")
            } catch (e: Exception) {
                Timber.e(e)
                _countryList.postValue(listOf())
            }
        }
    }
}
