package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.google.gson.annotations.SerializedName

data class Range(
    @SerializedName("min")
    val min: Double,
    @SerializedName("minExclusive")
    val minExclusive: Boolean,
    @SerializedName("max")
    val max: Double,
    @SerializedName("maxExclusive")
    val maxExclusive: Boolean
)
