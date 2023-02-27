package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonTrlFilter(
    @JsonProperty("dropIfTrlInRange")
    val dropIfTrlInRange: Range
)
