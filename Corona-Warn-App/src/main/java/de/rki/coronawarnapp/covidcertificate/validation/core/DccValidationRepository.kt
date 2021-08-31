package de.rki.coronawarnapp.covidcertificate.validation.core

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException.ErrorCode
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule.Type
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
        val acceptanceRules = kotlin.run {
            val rawJson = localCache.loadAcceptanceRuleJson()
            try {
                rawJson.toRuleSet()
            } catch (e: Exception) {
                Timber.tag(TAG).w("Failed to parse cached acceptanceRules: %s", rawJson)
                emptyList()
            }
        }
        val invalidationRules = kotlin.run {
            val rawJson = localCache.loadInvalidationRuleJson()
            try {
                rawJson.toRuleSet()
            } catch (e: Exception) {
                Timber.tag(TAG).w("Failed to parse cached invalidationRules: %s", rawJson)
                emptyList()
            }
        }
        val boosterNotificationRules = kotlin.run {
            val rawJson = localCache.loadBoosterNotificationRulesJson()
            try {
                rawJson.toRuleSet()
            } catch (e: Exception) {
                Timber.tag(TAG).w("Failed to parse cached boosterNotificationRules: %s", rawJson)
                emptyList()
            }
        }
        DccValidationData(
            countries = localCache.loadCountryJson()?.let { mapCountries(it) } ?: emptyList(),
            acceptanceRules = acceptanceRules,
            invalidationRules = invalidationRules,
            boosterNotificationRules = boosterNotificationRules
        )
    }

    val dccCountries: Flow<List<DccCountry>> = internalData.data.map { it.countries }

    val acceptanceRules: Flow<List<DccValidationRule>> = internalData.data.map { it.acceptanceRules }

    val invalidationRules: Flow<List<DccValidationRule>> = internalData.data.map { it.invalidationRules }

    val boosterNotificationRules: Flow<List<DccValidationRule>> = internalData.data.map { it.boosterNotificationRules }

    /**
     * The UI calls this before entering the validation flow.
     * Either we have a cached valid data to work with, or this throws an error for the UI to display.
     */
    @Throws(Exception::class)
    suspend fun refresh() {
        Timber.tag(TAG).d("refresh()")
        internalData.updateBlocking {
            val newCountryData = server.dccCountryJson().let {
                localCache.saveCountryJson(it)
                mapCountries(it)
            }
            val newAcceptanceData = server.ruleSetJson(Type.ACCEPTANCE).let { rawJson ->
                try {
                    rawJson.toRuleSet().also { localCache.saveAcceptanceRulesJson(rawJson) }
                } catch (e: Exception) {
                    throw DccValidationException(ErrorCode.ACCEPTANCE_RULE_JSON_DECODING_FAILED, e)
                }
            }
            val newInvalidationData = server.ruleSetJson(Type.INVALIDATION).let { rawJson ->
                try {
                    rawJson.toRuleSet().also { localCache.saveInvalidationRulesJson(rawJson) }
                } catch (e: Exception) {
                    throw DccValidationException(ErrorCode.INVALIDATION_RULE_JSON_DECODING_FAILED, e)
                }
            }
            DccValidationData(
                countries = newCountryData,
                acceptanceRules = newAcceptanceData,
                invalidationRules = newInvalidationData,
                boosterNotificationRules = this.boosterNotificationRules
            )
        }
    }

    /**
     * This only updates the booster notification rules.
     * Falls back to previous cached rules in case of an error.
     * Worst case is an empty list.
     */
    suspend fun updateBoosterNotificationRules(): List<DccValidationRule> {
        Timber.tag(TAG).d("updateBoosterNotificationRules()")
        return internalData.updateBlocking {
            val bnr = try {
                val rawJson = server.ruleSetJson(Type.BOOSTER_NOTIFICATION)
                rawJson.toRuleSet().also { localCache.saveBoosterNotificationRulesJson(rawJson) }
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Updating booster notification rules failed, loading cached rules")
                localCache.loadBoosterNotificationRulesJson().toRuleSet()
            }

            this.copy(boosterNotificationRules = bnr)
        }.let { data ->
            data.boosterNotificationRules.also { Timber.tag(TAG).d("Booster notification rules: %s", it) }
        }
    }

    private fun mapCountries(rawJson: String): List<DccCountry> = try {
        val countryCodes = gson.fromJson<List<String>>(rawJson)

        countryCodes.map { cc -> DccCountry(countryCode = cc) }
    } catch (e: Exception) {
        throw DccValidationException(ErrorCode.ONBOARDED_COUNTRIES_JSON_DECODING_FAILED, e)
    }

    private fun String?.toRuleSet(): List<DccValidationRule> {
        if (this == null) return emptyList()
        return gson.fromJson(this)
    }

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        server.clear()
        localCache.saveCountryJson(null)
        localCache.saveAcceptanceRulesJson(null)
        localCache.saveInvalidationRulesJson(null)
    }

    companion object {
        private const val TAG = "DccValidationRepository"
    }
}
