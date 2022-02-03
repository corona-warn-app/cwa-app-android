package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.jfn.JsonFunctions
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CclJsonFunctions @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    private val cclConfigurationRepository: CCLConfigurationRepository,
) {

    private var jsonFunctions: JsonFunctions
    private val mutex = Mutex()

    init {
        runBlocking {
            jsonFunctions = create(cclConfigurationRepository.getCCLConfigurations())
        }
    }

    private fun create(cclConfigurations: List<CCLConfiguration>): JsonFunctions {
        return JsonFunctions().apply {
            cclConfigurations.map {
                it.logic.jfnDescriptors
            }.flatten().forEach {
                registerFunction(it.name, it.definition.toJsonNode())
            }
        }
    }

    suspend fun update(cclConfigurations: List<CCLConfiguration>) = mutex.withLock {
        jsonFunctions = create(cclConfigurations)
    }

    suspend fun evaluateFunction(
        functionName: String,
        parameters: JsonNode
    ) = mutex.withLock {
        jsonFunctions.evaluateFunction(
            functionName,
            parameters
        )
    }

    private fun Any.toJsonNode(): JsonNode = mapper.valueToTree(this)
}
