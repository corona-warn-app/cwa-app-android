package de.rki.coronawarnapp.ccl.rampdown.model

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTime

data class RampDownInput(
    @JsonProperty("os")
    val os: String,

    @JsonProperty("language")
    val language: String,

    @JsonProperty("now")
    val now: SystemTime,
)
