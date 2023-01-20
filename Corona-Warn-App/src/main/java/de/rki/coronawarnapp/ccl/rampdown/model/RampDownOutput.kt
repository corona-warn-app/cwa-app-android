package de.rki.coronawarnapp.ccl.rampdown.model

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CclText

data class RampDownOutput(
    @JsonProperty("visible")
    val visible: Boolean,

    @JsonProperty("titleText")
    val titleText: CclText?,

    @JsonProperty("subtitleText")
    val subtitleText: CclText?,

    @JsonProperty("longText")
    val longText: CclText?,

    @JsonProperty("faqAnchor")
    val faqAnchor: String?,
)
