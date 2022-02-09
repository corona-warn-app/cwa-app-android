package de.rki.coronawarnapp.covidcertificate.booster

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationCache
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleConverter
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer.RuleSetSource.CACHE
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer.RuleSetSource.SERVER
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
     * This updates the booster notification rules and returns true if new rules were fetched from the server.
     * Falls back to previous cached rules in case of an error.
     * Worst case is an empty list.
     */
    suspend fun update(): UpdateResult {
        Timber.tag(TAG).d("updateBoosterNotificationRules()")

        var updateResult = UpdateResult.NO_UPDATE

        internalData.updateBlocking {
            return@updateBlocking try {
                val ruleSetResult = server.ruleSetJson(DccValidationRule.Type.BOOSTER_NOTIFICATION)
                when (ruleSetResult.source) {
                    SERVER -> {
                        updateResult = UpdateResult.UPDATE
                        ruleSetResult.ruleSetJson.toRuleSet()
                            .also { localCache.saveBoosterNotificationRulesJson(ruleSetResult.ruleSetJson) }
                    }
                    CACHE -> {
                        updateResult = UpdateResult.NO_UPDATE
                        ruleSetResult.ruleSetJson.toRuleSet()
                    }
                }
            } catch (e: Exception) {
                updateResult = UpdateResult.FAIL
                Timber.tag(TAG).w(e, "Updating booster notification rules failed, loading cached rules")
                localCache.loadBoosterNotificationRulesJson().toRuleSet()
            }
        }.let { boosterNotificationRules ->
            boosterNotificationRules.also { Timber.tag(TAG).d("Booster notification rules size=%s: %s", it.size, it) }
        }

        return updateResult
    }

    enum class UpdateResult {
        UPDATE, NO_UPDATE, FAIL
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
