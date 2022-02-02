package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.jfn.JsonFunctions

class JsonFunctionsWrapper(
    cclConfiguration: CCLConfiguration
) {

    private val jsonFunctions = JsonFunctions()
    private val mapper = ObjectMapper()

    init {
        cclConfiguration.logic.jfnDescriptors.forEach {
            jsonFunctions.registerFunction(it.name, it.definition.toJsonNode())
        }
    }

    fun evaluateFunction(
        functionName: String,
        parameters: JsonNode
    ) = jsonFunctions.evaluateFunction(
        functionName,
        parameters
    )

    private fun Any.toJsonNode(): JsonNode = mapper.valueToTree(this)
}
