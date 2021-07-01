package de.rki.coronawarnapp.covidcertificate.validation.core.country

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.validation.core.country.server.DccCountryServer
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.plus
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Returns the latest set of dcc-country data.
 * If the user is within the validation flow, we want the cached version.
 * Before the user enters the validation flow, refresh needs to be called and only proceeded if successful
 *
 * General validation flow can happen offline,
 * but we want to make sure that we have the latest data set before starting.
 * So call [refresh], wait for the result before proceeding and handle errors, then work with the cache [dccCountries].
 */
@Singleton
class DccCountryRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    @BaseGson private val gson: Gson,
    private val server: DccCountryServer,
    private val localCache: DccCountryLocalCache,
) {

    private val internalData: HotDataFlow<List<DccCountry>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = Duration.standardSeconds(5).millis,
            replayExpirationMillis = 0
        ),
    ) {
        localCache.loadJson()?.let { mapCountries(it) } ?: emptyList()
    }

    val dccCountries: Flow<List<DccCountry>> = internalData.data

    private fun mapCountries(rawJson: String): List<DccCountry> {
        val countryCodes = gson.fromJson<List<String>>(rawJson)

        return countryCodes.map { cc ->
            DccCountry(countryCode = cc)
        }
    }

    /**
     * Refreshes via server request, throws exception if the server request fails.
     */
    @Throws(Exception::class)
    suspend fun refresh(): List<DccCountry> = internalData.updateBlocking {
        val newCountryData = server.dccCountryJson()
        localCache.saveJson(newCountryData)
        mapCountries(newCountryData)
    }

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        server.clear()
        localCache.saveJson(null)
    }

    companion object {
        private const val TAG = "DccCountryRepository"
    }
}
