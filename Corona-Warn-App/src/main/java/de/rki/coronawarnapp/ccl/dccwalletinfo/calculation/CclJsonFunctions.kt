package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.jfn.JsonFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CclJsonFunctions @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    @AppScope private val appScope: CoroutineScope,
    private val configurationRepository: CCLConfigurationRepository,
) {

    private lateinit var jsonFunctions: JsonFunctions

    init {
        appScope.launch {
            jsonFunctions = create(configurationRepository.getCCLConfigurations())
        }
    }

    fun update(cclConfigurations: List<CCLConfiguration>) {
        jsonFunctions = create(cclConfigurations)
    }

    fun evaluateFunction(
        functionName: String,
        parameters: JsonNode
    ) = jsonFunctions.evaluateFunction(functionName, parameters)

    private fun create(cclConfigurations: List<CCLConfiguration>) =
        JsonFunctions().apply {
            cclConfigurations
                .map { it.logic.jfnDescriptors }
                .flatten()
                .forEach { (name, definition) ->
                    runCatching { registerFunction(name, definition.toJsonNode()) }.onFailure {
                        Timber.e(it, "Registering jfn=%s with definition=%s failed.", name, definition)
                    }
                }
        }

    private fun Any.toJsonNode(): JsonNode = mapper.valueToTree(this)
}
