package de.rki.coronawarnapp.covidcertificate.booster

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationCache
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleConverter
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject

@Reusable
class BoosterRulesRepository @Inject constructor(
    @AppScope appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    private val converter: DccValidationRuleConverter,
    private val server: DccValidationServer,
    private val localCache: DccValidationCache
) {

    private val internalData: HotDataFlow<List<DccValidationRule>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Eagerly
    ) {
        val rawJson = localCache.loadBoosterNotificationRulesJson()
        return@HotDataFlow try {
            rawJson.toRuleSet()
        } catch (e: Exception) {
            Timber.tag(TAG).w("Failed to parse cached boosterNotificationRules: %s", rawJson)
            emptyList()
        }
    }

    val rules: Flow<List<DccValidationRule>> = internalData.data

    /**
     * This updates the booster notification rules and returns them.
     * Falls back to previous cached rules in case of an error.
     * Worst case is an empty list.
     */
    suspend fun update(): List<DccValidationRule> {
        Timber.tag(TAG).d("updateBoosterNotificationRules()")
        return internalData.updateBlocking {
            return@updateBlocking try {
                val rawJson = server.ruleSetJson(DccValidationRule.Type.BOOSTER_NOTIFICATION)
                rawJson.toRuleSet().also { localCache.saveBoosterNotificationRulesJson(rawJson) }
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Updating booster notification rules failed, loading cached rules")
                localCache.loadBoosterNotificationRulesJson().toRuleSet()
            }
        }.let { boosterNotificationRules ->
            boosterNotificationRules.also { Timber.tag(TAG).d("Booster notification rules size=%s: %s", it.size, it) }
        }
    }

    suspend fun updateNew(): Boolean {
        Timber.tag(TAG).d("updateBoosterNotificationRules()")
        return internalData.updateBlocking {
            return@updateBlocking try {
                val rawJson = server.ruleSetJson(DccValidationRule.Type.BOOSTER_NOTIFICATION)
                rawJson.toRuleSet().also { localCache.saveBoosterNotificationRulesJson(rawJson) }
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Updating booster notification rules failed, loading cached rules")
                localCache.loadBoosterNotificationRulesJson().toRuleSet()
            }
        }.let { boosterNotificationRules ->
            boosterNotificationRules.also { Timber.tag(TAG).d("Booster notification rules size=%s: %s", it.size, it) }
            true
        }
    }

    private fun String?.toRuleSet(): List<DccValidationRule> = converter.jsonToRuleSet(this)

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        server.clear()
        localCache.saveBoosterNotificationRulesJson(null)
        internalData.updateBlocking { emptyList() }
    }
}

private const val TAG = "BoosterRulesRepository"
