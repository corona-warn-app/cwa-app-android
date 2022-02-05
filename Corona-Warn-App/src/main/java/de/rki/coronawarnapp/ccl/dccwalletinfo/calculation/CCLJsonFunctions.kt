package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.jfn.JsonFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CCLJsonFunctions @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    @AppScope private val appScope: CoroutineScope,
    private val configurationRepository: CCLConfigurationRepository,
    private val dispatcher: DispatcherProvider,
) {
    private lateinit var jsonFunctions: JsonFunctions
    private val mutex = Mutex()

    init {
        appScope.launch {
            jsonFunctions = create(configurationRepository.getCCLConfigurations())
        }
    }

    suspend fun update(cclConfigurations: List<CCLConfiguration>) {
        jsonFunctions = create(cclConfigurations)
    }

    suspend fun evaluateFunction(
        functionName: String,
        parameters: JsonNode
    ) = withContext(dispatcher.Default) {
        mutex.withLock {
            jsonFunctions.evaluateFunction(functionName, parameters)
        }
    }

    private suspend fun create(cclConfigurations: List<CCLConfiguration>) = mutex.withLock {
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
    }

    private fun Any.toJsonNode(): JsonNode = mapper.valueToTree(this)
}
