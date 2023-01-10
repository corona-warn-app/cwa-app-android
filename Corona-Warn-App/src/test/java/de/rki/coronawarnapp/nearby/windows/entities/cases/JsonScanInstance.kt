package de.rki.coronawarnapp.nearby.windows.entities.cases

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonScanInstance(
    @JsonProperty("minAttenuation")
    val minAttenuation: Int,
    @JsonProperty("secondsSinceLastScan")
    val secondsSinceLastScan: Int,
    @JsonProperty("typicalAttenuation")
    val typicalAttenuation: Int
)
