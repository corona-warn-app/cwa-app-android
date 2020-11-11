package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.google.gson.annotations.SerializedName

data class JsonTrlEncoding(
    @SerializedName("infectiousnessOffsetHigh")
    val infectiousnessOffsetHigh: Int,
    @SerializedName("infectiousnessOffsetStandard")
    val infectiousnessOffsetStandard: Int,
    @SerializedName("reportTypeOffsetConfirmedClinicalDiagnosis")
    val reportTypeOffsetConfirmedClinicalDiagnosis: Int,
    @SerializedName("reportTypeOffsetConfirmedTest")
    val reportTypeOffsetConfirmedTest: Int,
    @SerializedName("reportTypeOffsetRecursive")
    val reportTypeOffsetRecursive: Int,
    @SerializedName("reportTypeOffsetSelfReport")
    val reportTypeOffsetSelfReport: Int
)
