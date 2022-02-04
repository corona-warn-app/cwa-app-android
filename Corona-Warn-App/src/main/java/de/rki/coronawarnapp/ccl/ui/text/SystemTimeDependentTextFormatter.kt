package de.rki.coronawarnapp.ccl.ui.text

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.getDefaultInputParameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTimeDependentText
import de.rki.coronawarnapp.util.serialization.BaseJackson
import org.joda.time.DateTime
import javax.inject.Inject

class SystemTimeDependentTextFormatter @Inject constructor(
    // jsonFunctions: CclJsonFunctions
    @BaseJackson private val mapper: ObjectMapper,
) {
    suspend fun format(text: SystemTimeDependentText): CCLText? {
        val functionName = text.functionName

        val defaultParameters = getDefaultInputParameters(DateTime.now()).toObjectNode()
        val parameters = text.parameters

        // TODO: merge defaultParameters and parameters

        // val output = jsonFunctions.evaluateFunction(functionName, parameters)
        // mapper.treeToValue(output, SingleText::class.java)
        // mapper.treeToValue(output, PluralText::class.java)
    }

    private fun Any.toObjectNode(): ObjectNode = mapper.valueToTree(this)
}
