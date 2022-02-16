package de.rki.coronawarnapp.ccl.dccadmission.model

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText

data class DccAdmissionCheckScenarios(
    @JsonProperty("labelText")
    val labelText: CCLText,

    @JsonProperty("scenarioSelection")
    val scenarioSelection: ScenarioSelection,
)

data class ScenarioSelection(
    @JsonProperty("titleText")
    val titleText: CCLText,

    @JsonProperty("items")
    val items: List<Scenario>
)

data class Scenario(
    @JsonProperty("identifier")
    val identifier: String,

    @JsonProperty("titleText")
    val titleText: CCLText,

    @JsonProperty("subtitleText")
    val subtitleText: CCLText? = null,

    @JsonProperty("enabled")
    val enabled: Boolean,
)
