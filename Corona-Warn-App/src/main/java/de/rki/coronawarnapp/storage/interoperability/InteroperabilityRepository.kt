package de.rki.coronawarnapp.storage.interoperability

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.LocalData
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteroperabilityRepository @Inject constructor() {

    fun saveInteroperabilityUsed() {
        LocalData.isInteroperabilityShownAtLeastOnce = true
    }

    private val _countryList: MutableLiveData<List<String>> = MutableLiveData(listOf())
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
                val countries =
                    ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()
                        .supportedCountriesList
                        ?.map { it.toLowerCase(Locale.ROOT) } ?: listOf()
                _countryList.postValue(countries)
                Timber.d("Country list: ${TextUtils.join(System.lineSeparator(), countries)}")
            } catch (e: Exception) {
                Timber.e(e)
                _countryList.postValue(listOf())
            }
        }
    }
}
