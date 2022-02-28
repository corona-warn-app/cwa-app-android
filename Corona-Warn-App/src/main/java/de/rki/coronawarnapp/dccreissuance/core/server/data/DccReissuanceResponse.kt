package de.rki.coronawarnapp.dccreissuance.core.server.data

import com.google.gson.annotations.SerializedName

data class DccReissuanceResponse(
    val dccReissuances: List<DccReissuance>
) {

    data class DccReissuance(
        @SerializedName("certificate") val certificate: String,
        @SerializedName("relations") val relations: List<Relation>
    )

    data class Relation(
        @SerializedName("index") val index: Int,
        @SerializedName("action") val action: String,
    )
}
