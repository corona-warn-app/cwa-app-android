package de.rki.coronawarnapp.ccl.dccadmission.storage

import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccadmission.model.DccAdmissionCheckScenarios
import de.rki.coronawarnapp.ccl.dccadmission.model.Scenario
import de.rki.coronawarnapp.ccl.dccadmission.model.ScenarioSelection
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
class DccAdmissionCheckScenariosRepository @Inject constructor(
    private val cclSettings: CCLSettings,
    @BaseJackson private val mapper: ObjectMapper
) {

    val admissionCheckScenarios: Flow<DccAdmissionCheckScenarios?> =
        cclSettings.admissionCheckScenarios.map {
            it?.let { json ->
                try {
                    mapper.readValue(json, DccAdmissionCheckScenarios::class.java)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse admission check scenarios.")
                    null
                }
            } ?: run {
                Timber.v("No admission check scenarios available.")
                null
            }
        }

    suspend fun save(scenarios: DccAdmissionCheckScenarios) {
        val json = mapper.writeValueAsString(scenarios)
        cclSettings.setAdmissionCheckScenarios(json)
    }

    suspend fun clear() {
        cclSettings.setAdmissionCheckScenarios("")
    }
}

val dummy = DccAdmissionCheckScenarios(
    labelText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Status anzeigen für folgendes Bundesland:"),
        parameters = emptyList()
    ),
    scenarioSelection = ScenarioSelection(
        titleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Ihr Bundesland"),
            parameters = emptyList()
        ),
        items = listOf(
            Scenario(
                identifier = "DE",
                titleText = SingleText(
                    type = "string",
                    localizedText = mapOf("de" to "Bundesweit"),
                    parameters = emptyList()
                ),
                enabled = true
            ),
            Scenario(
                identifier = "BW",
                titleText = SingleText(
                    type = "string",
                    localizedText = mapOf("de" to "Baden-Württemberg"),
                    parameters = emptyList()
                ),
                subtitleText = SingleText(
                    type = "string",
                    localizedText = mapOf("de" to "Schön hier"),
                    parameters = emptyList()
                ),
                enabled = true,
            ),
            Scenario(
                identifier = "HE",
                titleText = SingleText(
                    type = "string",
                    localizedText = mapOf("de" to "Hesse"),
                    parameters = emptyList()
                ),
                subtitleText = SingleText(
                    type = "string",
                    localizedText = mapOf("de" to "Für dieses Bundesland liegen momentan keine Regeln vor"),
                    parameters = emptyList()
                ),
                enabled = false,
            ),
        ),
    )
)
