package de.rki.coronawarnapp.storage.interoperability

import android.text.TextUtils
import androidx.lifecycle.asLiveData
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DefaultDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteroperabilityRepository @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DefaultDispatcherProvider
) {

    private val countryListFlowInternal = MutableStateFlow(listOf<Country>())
    val countryListFlow: Flow<List<Country>> = countryListFlowInternal

    @Deprecated("Use  countryListFlow")
    val countryList = countryListFlow.asLiveData()

    init {
        getAllCountries()
    }

    fun getAllCountries() {
        // TODO Make this reactive, the AppConfigProvider should refresh itself on network changes.
        appScope.launch(context = dispatcherProvider.IO) {
            try {
                val countries = appConfigProvider.getAppConfig()
                    .supportedCountries
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

    fun clear() {
        countryListFlowInternal.value = emptyList()
    }

    fun saveInteroperabilityUsed() {
        LocalData.isInteroperabilityShownAtLeastOnce = true
    }
}
