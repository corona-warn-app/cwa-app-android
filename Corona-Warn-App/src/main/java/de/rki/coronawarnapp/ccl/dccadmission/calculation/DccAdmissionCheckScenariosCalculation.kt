package de.rki.coronawarnapp.ccl.dccadmission.calculation

import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.ccl.configuration.model.CclInputParameters
import de.rki.coronawarnapp.ccl.configuration.model.getDefaultInputParameters
import de.rki.coronawarnapp.ccl.dccadmission.model.DccAdmissionCheckScenarios
import de.rki.coronawarnapp.ccl.dccadmission.model.DccAdmissionCheckScenariosInput
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CclJsonFunctions
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTime
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import javax.inject.Inject

class DccAdmissionCheckScenariosCalculation @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    private val cclJsonFunctions: CclJsonFunctions,
    private val dispatcherProvider: DispatcherProvider
) {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getDccAdmissionCheckScenarios(
        dateTime: DateTime = DateTime.now(),
    ): DccAdmissionCheckScenarios = withContext(dispatcherProvider.IO) {
        val output = cclJsonFunctions.evaluateFunction(
            "getDccAdmissionCheckScenarios",
            getDefaultInputParameters(dateTime).toInput(mapper)
        )

        mapper.treeToValue(output, DccAdmissionCheckScenarios::class.java)
    }
}

@VisibleForTesting
internal fun CclInputParameters.toInput(mapper: ObjectMapper) = mapper.valueToTree<JsonNode>(
    DccAdmissionCheckScenariosInput(
        os = os,
        language = language,
        now = SystemTime(
            timestamp = now.timestamp,
            localDate = now.localDate,
            localDateTime = now.localDateTime,
            localDateTimeMidnight = now.localDateTimeMidnight,
            utcDate = now.utcDate,
            utcDateTime = now.utcDateTime,
            utcDateTimeMidnight = now.utcDateTimeMidnight,
        )
    )
)
