package de.rki.coronawarnapp.nearby.windows.entities

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.nearby.windows.entities.cases.TestCase
import de.rki.coronawarnapp.nearby.windows.entities.configuration.DefaultRiskCalculationConfiguration

data class ExposureWindowsJsonInput(
    @SerializedName("__comment__")
    val comment: String,
    @SerializedName("defaultRiskCalculationConfiguration")
    val defaultRiskCalculationConfiguration: DefaultRiskCalculationConfiguration,
    @SerializedName("testCases")
    val testCases: List<TestCase>
)
