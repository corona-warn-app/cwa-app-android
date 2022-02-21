package de.rki.coronawarnapp.ccl.dccadmission.model

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CclText

data class DccAdmissionCheckScenarios(
    @JsonProperty("labelText")
    val labelText: CclText,

    @JsonProperty("scenarioSelection")
    val scenarioSelection: ScenarioSelection,
)

data class ScenarioSelection(
    @JsonProperty("titleText")
    val titleText: CclText,

    @JsonProperty("items")
    val items: List<Scenario>
)

data class Scenario(
    @JsonProperty("identifier")
    val identifier: String,

    @JsonProperty("titleText")
    val titleText: CclText,

    @JsonProperty("subtitleText")
    val subtitleText: CclText? = null,

    @JsonProperty("enabled")
    val enabled: Boolean,
)
