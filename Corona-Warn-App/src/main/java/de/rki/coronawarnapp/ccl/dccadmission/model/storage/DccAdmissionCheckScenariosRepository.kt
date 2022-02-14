package de.rki.coronawarnapp.ccl.dccadmission.model.storage

import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccadmission.model.DccAdmissionCheckScenarios
import de.rki.coronawarnapp.ccl.dccadmission.model.Scenario
import de.rki.coronawarnapp.ccl.dccadmission.model.ScenarioSelection
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class DccAdmissionCheckScenariosRepository @Inject constructor(
    private val cclSettings: CCLSettings
) {

    val admissionCheckScenarios: Flow<DccAdmissionCheckScenarios?> = cclSettings.admissionCheckScenarios

    fun save(scenarios: DccAdmissionCheckScenarios) {
        cclSettings.setAdmissionCheckScenarios(scenarios)
    }

    fun clear() {
        cclSettings.setAdmissionCheckScenarios(null)
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
