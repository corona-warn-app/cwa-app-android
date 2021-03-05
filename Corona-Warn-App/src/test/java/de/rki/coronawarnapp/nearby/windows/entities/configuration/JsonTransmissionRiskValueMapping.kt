package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.google.gson.annotations.SerializedName

data class JsonTransmissionRiskValueMapping(
    @SerializedName("transmissionRiskLevel")
    val transmissionRiskLevel: Int,
    @SerializedName("transmissionRiskValue")
    val transmissionRiskValue: Double
)
