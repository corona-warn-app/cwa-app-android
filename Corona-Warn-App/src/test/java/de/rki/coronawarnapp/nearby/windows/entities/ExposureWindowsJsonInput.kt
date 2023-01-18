package de.rki.coronawarnapp.nearby.windows.entities

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.nearby.windows.entities.cases.TestCase
import de.rki.coronawarnapp.nearby.windows.entities.configuration.DefaultRiskCalculationConfiguration

data class ExposureWindowsJsonInput(
    @JsonProperty("__comment__")
    val comment: String,
    @JsonProperty("defaultRiskCalculationConfiguration")
    val defaultRiskCalculationConfiguration: DefaultRiskCalculationConfiguration,
    @JsonProperty("testCases")
    val testCases: List<TestCase>
)
