package de.rki.coronawarnapp.ccl.dccadmission.model

import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccAdmissionCheckScenariosTest : BaseTest() {

    private val mapper = SerializationModule().jacksonObjectMapper()

    private val outputJson = """
        {
          "labelText": {
            "type": "string",
            "localizedText": {
              "de": "Status anzeigen für folgendes Bundesland:"
            },
            "parameters": []
          },
          "scenarioSelection": {
            "titleText": {
              "type": "string",
              "localizedText": {
                "de": "Ihr Bundesland"
              },
              "parameters": []
            },
            "items": [
              {
                "identifier": "DE",
                "titleText": {
                  "type": "string",
                  "localizedText": {
                    "de": "Bundesweit"
                  },
                  "parameters": []
                },
                "enabled": true
              },
              {
                "identifier": "BW",
                "titleText": {
                  "type": "string",
                  "localizedText": {
                    "de": "Baden-Württemberg"
                  },
                  "parameters": []
                },
                "subtitleText": {
                  "type": "string",
                  "localizedText": {
                    "de": "Schön hier"
                  },
                  "parameters": []
                },
                "enabled": true
              },
              {
                "identifier": "HE",
                "titleText": {
                  "type": "string",
                  "localizedText": {
                    "de": "Hesse"
                  },
                  "parameters": []
                },
                "subtitleText": {
                  "type": "string",
                  "localizedText": {
                    "de": "Für dieses Bundesland liegen momentan keine Regeln vor"
                  },
                  "parameters": []
                },
                "enabled": false
              }
            ]
          }
        }
    """.trimIndent()

    private val output = DccAdmissionCheckScenarios(
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

    @Test
    fun `parse output`() {
        mapper.readValue<DccAdmissionCheckScenarios>(outputJson) shouldBe output
    }
}
