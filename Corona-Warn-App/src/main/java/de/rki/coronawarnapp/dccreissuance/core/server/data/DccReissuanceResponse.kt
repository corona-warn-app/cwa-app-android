package de.rki.coronawarnapp.dccreissuance.core.server.data

import com.fasterxml.jackson.annotation.JsonProperty

data class DccReissuanceResponse(
    val dccReissuances: List<DccReissuance>
) {

    data class DccReissuance(
        @JsonProperty("certificate") val certificate: String,
        @JsonProperty("relations") val relations: List<Relation>
    )

    data class Relation(
        @JsonProperty("index") val index: Int,
        @JsonProperty("action") val action: String,
    )
}
