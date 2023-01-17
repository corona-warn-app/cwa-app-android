package de.rki.coronawarnapp.ccl.rampdown.calculation

import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.ccl.configuration.model.CclInputParameters
import de.rki.coronawarnapp.ccl.configuration.model.getDefaultInputParameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CclJsonFunctions
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTime
import de.rki.coronawarnapp.ccl.rampdown.model.RampDownInput
import de.rki.coronawarnapp.ccl.rampdown.model.RampDownOutput
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RampDownCalculation @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    private val cclJsonFunctions: CclJsonFunctions,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun getStatusTabNotice(
        dateTime: ZonedDateTime = ZonedDateTime.now(),
    ): RampDownOutput = withContext(dispatcherProvider.IO) {
        val output = cclJsonFunctions.evaluateFunction(
            "getStatusTabNotice",
            getDefaultInputParameters(dateTime).toInput(mapper)
        )

        Timber.d(output.toPrettyString())
        mapper.treeToValue(output, RampDownOutput::class.java)
    }
}

@VisibleForTesting
internal fun CclInputParameters.toInput(mapper: ObjectMapper) = mapper.valueToTree<JsonNode>(
    RampDownInput(
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
