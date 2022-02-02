package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.jfn.JsonFunctions
import javax.inject.Inject

class JsonFunctionsWrapper @Inject constructor(
    @BaseJackson val mapper: ObjectMapper
) {

    private lateinit var jsonFunctions: JsonFunctions

    fun init(cclConfiguration: CCLConfiguration) {
        jsonFunctions = JsonFunctions()
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
