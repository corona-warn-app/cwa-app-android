package de.rki.coronawarnapp.covidcertificate.validation.core

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccValidationCache
import de.rki.coronawarnapp.covidcertificate.validation.core.country.server.DccCountryServer
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.server.DccValidationRulesServer
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
 * Returns the latest set of dcc-country data and rules.
 * If the user is within the validation flow, we want the cached version.
 * Before the user enters the validation flow, refresh needs to be called and only proceeded if successful
 *
 * General validation flow can happen offline,
 * but we want to make sure that we have the latest data set before starting.
 * So call [refresh], wait for the result before proceeding and handle errors, then work with the cache [dccCountries].
 */
@Singleton
class DccValidationRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    @BaseGson private val gson: Gson,
    private val countryServer: DccCountryServer,
    private val localCache: DccValidationCache,
    private val rulesServer: DccValidationRulesServer,
) {

    private val internalCountries: HotDataFlow<List<DccCountry>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = Duration.standardSeconds(5).millis,
            replayExpirationMillis = 0
        ),
    ) {
        localCache.loadJson()?.let { mapCountries(it) } ?: emptyList()
    }

    val dccCountries: Flow<List<DccCountry>> = internalCountries.data

    /**
     * The UI calls this before entering the validation flow.
     * Either we have a cached valid data to work with, or this throws an error for the UI to display.
     */
    @Throws(Exception::class)
    suspend fun refresh() {
        internalCountries.updateBlocking {
            val newCountryData = countryServer.dccCountryJson()
            localCache.saveJson(newCountryData)
            mapCountries(newCountryData)
        }
        // TODO refresh current rule data
    }

    private fun mapCountries(rawJson: String): List<DccCountry> {
        val countryCodes = gson.fromJson<List<String>>(rawJson)

        return countryCodes.map { cc ->
            DccCountry(countryCode = cc)
        }
    }

    suspend fun acceptanceRules(arrivalCountry: DccCountry): List<DccValidationRule> {
        return emptyList() // TODO
    }

    suspend fun invalidationRules(arrivalCountry: DccCountry): List<DccValidationRule> {
        return emptyList() // TODO
    }

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        countryServer.clear()
        localCache.saveJson(null)
        // TODO clear rules
    }

    companion object {
        private const val TAG = "DccCountryRepository"
    }
}

