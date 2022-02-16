package de.rki.coronawarnapp.covidcertificate.person.ui

import de.rki.coronawarnapp.ccl.dccadmission.model.DccAdmissionCheckScenarios
import de.rki.coronawarnapp.ccl.dccadmission.model.Scenario
import de.rki.coronawarnapp.ccl.dccadmission.model.ScenarioSelection
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText

val dccAdmissionCheckScenarios = DccAdmissionCheckScenarios(
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
