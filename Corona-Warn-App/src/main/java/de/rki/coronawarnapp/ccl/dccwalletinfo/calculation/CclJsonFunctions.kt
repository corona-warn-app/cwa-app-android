package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.ccl.configuration.model.CclConfiguration
import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.jfn.JsonFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CclJsonFunctions @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    @AppScope private val appScope: CoroutineScope,
    configurationRepository: CclConfigurationRepository,
    private val dispatcher: DispatcherProvider,
) {
    private lateinit var jsonFunctions: JsonFunctions
    private val mutex = Mutex()

    init {
        configurationRepository
            .cclConfigurations
            .distinctUntilChanged()
            .onEach { cclConfigList ->
                mutex.withLock {
                    jsonFunctions = create(cclConfigList)
                }
            }
            .launchIn(appScope)
    }

    suspend fun evaluateFunction(
        functionName: String,
        parameters: JsonNode
    ) = withContext(dispatcher.Default) {
        mutex.withLock {
            jsonFunctions.evaluateFunction(functionName, parameters)
        }
    }

    private fun create(cclConfigurations: List<CclConfiguration>): JsonFunctions {
        return JsonFunctions().apply {
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
