package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import com.fasterxml.jackson.annotation.JsonProperty

data class DccRevocationCalculationTestCase(
    @JsonProperty("description")
    val description: String,

    @JsonProperty("barcodeData")
    val barcodeData: String,

    @JsonProperty("expUCI")
    val expUCI: String,

    @JsonProperty("expCOUNTRYCODEUCI")
    val expCOUNTRYCODEUCI: String,

    @JsonProperty("expSIGNATURE")
    val expSIGNATURE: String,
)
