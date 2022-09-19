package de.rki.coronawarnapp.covidcertificate.booster

import dagger.Reusable
import de.rki.coronawarnapp.ccl.configuration.storage.DccBoosterRulesStorage
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleConverter
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer.RuleSetSource.CACHE
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer.RuleSetSource.SERVER
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.repositories.UpdateResult
import de.rki.coronawarnapp.util.reset.Resettable
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
    private val storage: DccBoosterRulesStorage
) : Resettable {

    private val internalData: HotDataFlow<List<DccValidationRule>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Eagerly
    ) {
        val rawJson = storage.loadBoosterRulesJson()
        return@HotDataFlow try {
            rawJson.toRuleSet()
        } catch (e: Exception) {
            Timber.tag(TAG).w("Failed to parse booster rules: %s", rawJson)
            emptyList()
        }
    }

    val rules: Flow<List<DccValidationRule>> = internalData.data

    /**
     * This updates the booster notification rules.
     * Falls back to previous stored rules in case of an error.
     * Worst case is an empty list.
     *
     * @return UpdateResult.UPDATE if new booster rules got downloaded from the server, UpdateResult.NO_UPDATE when
     * there were no new rules from the server, or UpdateResult.FAIL if the request or parsing failed.
     */
    suspend fun update(): UpdateResult {
        Timber.tag(TAG).d("updateB booster rules from server")

        var updateResult = UpdateResult.NO_UPDATE

        internalData.updateBlocking {
            return@updateBlocking try {
                val ruleSetResult = server.ruleSetJson(DccValidationRule.Type.BOOSTER_NOTIFICATION)
                when (ruleSetResult.source) {
                    SERVER -> {
                        updateResult = UpdateResult.UPDATE
                        ruleSetResult.ruleSetJson.toRuleSet()
                            .also { storage.saveBoosterRulesJson(ruleSetResult.ruleSetJson) }
                    }
                    CACHE -> {
                        updateResult = UpdateResult.NO_UPDATE
                        ruleSetResult.ruleSetJson.toRuleSet()
                    }
                }
            } catch (e: Exception) {
                updateResult = UpdateResult.FAIL
                Timber.tag(TAG).w(e, "Updating booster rules from server failed, loading stored rules")
                storage.loadBoosterRulesJson().toRuleSet()
            }
        }.let { boosterRules ->
            boosterRules.also { Timber.tag(TAG).d("Booster rules size=%s: %s", it.size, it) }
        }

        return updateResult
    }

    private fun String?.toRuleSet(): List<DccValidationRule> = converter.jsonToRuleSet(this)

    override suspend fun reset() {
        Timber.tag(TAG).i("reset()")
        internalData.updateBlocking { emptyList() }
    }
}

private const val TAG = "BoosterRulesRepository"
