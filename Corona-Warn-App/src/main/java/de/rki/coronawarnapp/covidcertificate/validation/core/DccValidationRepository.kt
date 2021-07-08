package de.rki.coronawarnapp.covidcertificate.validation.core

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
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
    private val server: DccValidationServer,
    private val localCache: DccValidationCache,
) {
    private val internalData: HotDataFlow<DccValidationData> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = Duration.standardSeconds(5).millis,
            replayExpirationMillis = 0
        ),
    ) {
        DccValidationData(localCache.loadCountryJson()?.let { mapCountries(it) } ?: emptyList())
    }

    val dccCountries: Flow<List<DccCountry>> = internalData.data.map { it.countries }

    /**
     * The UI calls this before entering the validation flow.
     * Either we have a cached valid data to work with, or this throws an error for the UI to display.
     */
    @Throws(Exception::class)
    suspend fun refresh() {
        internalData.updateBlocking {
            val newCountryData = server.dccCountryJson()
            localCache.saveCountryJson(newCountryData)
            DccValidationData(mapCountries(newCountryData))
        }
    }

    private fun mapCountries(rawJson: String): List<DccCountry> {
        val countryCodes = gson.fromJson<List<String>>(rawJson)

        return countryCodes.map { cc ->
            DccCountry(countryCode = cc)
        }
    }

    suspend fun acceptanceRules(arrivalCountry: DccCountry): List<DccValidationRule> {
        val data = server.ruleSetJson(DccValidationRule.Type.ACCEPTANCE)
        localCache.saveRulesJson(data)
        return gson.fromJson(data)
    }

    suspend fun invalidationRules(arrivalCountry: DccCountry): List<DccValidationRule> {
        val data = server.ruleSetJson(DccValidationRule.Type.INVALIDATION)
        localCache.saveRulesJson(data)
        return gson.fromJson(data)
    }

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        server.clear()
        localCache.saveCountryJson(null)
        localCache.saveRulesJson(null)
    }

    companion object {
        private const val TAG = "DccCountryRepository"
    }
}
