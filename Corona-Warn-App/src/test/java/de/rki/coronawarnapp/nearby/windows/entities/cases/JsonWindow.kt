package de.rki.coronawarnapp.nearby.windows.entities.cases

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonWindow(
    @JsonProperty("ageInDays")
    val ageInDays: Int,
    @JsonProperty("calibrationConfidence")
    val calibrationConfidence: Int,
    @JsonProperty("infectiousness")
    val infectiousness: Int,
    @JsonProperty("reportType")
    val reportType: Int,
    @JsonProperty("scanInstances")
    val scanInstances: List<JsonScanInstance>
)
