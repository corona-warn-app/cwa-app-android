package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import com.google.gson.annotations.SerializedName

data class RevocationCalculationTestCase(
    @SerializedName("description")
    val description: String,

    @SerializedName("barcodeData")
    val barcodeData: String,

    @SerializedName("expUCI")
    val expUCI: String,

    @SerializedName("expCOUNTRYCODEUCI")
    val expCOUNTRYCODEUCI: String,

    @SerializedName("expSIGNATURE")
    val expSIGNATURE: String,
)
