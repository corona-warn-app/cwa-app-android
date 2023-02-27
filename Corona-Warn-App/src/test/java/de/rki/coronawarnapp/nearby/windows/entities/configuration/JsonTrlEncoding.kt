package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonTrlEncoding(
    @JsonProperty("infectiousnessOffsetHigh")
    val infectiousnessOffsetHigh: Int,
    @JsonProperty("infectiousnessOffsetStandard")
    val infectiousnessOffsetStandard: Int,
    @JsonProperty("reportTypeOffsetConfirmedClinicalDiagnosis")
    val reportTypeOffsetConfirmedClinicalDiagnosis: Int,
    @JsonProperty("reportTypeOffsetConfirmedTest")
    val reportTypeOffsetConfirmedTest: Int,
    @JsonProperty("reportTypeOffsetRecursive")
    val reportTypeOffsetRecursive: Int,
    @JsonProperty("reportTypeOffsetSelfReport")
    val reportTypeOffsetSelfReport: Int
)
