package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.google.gson.annotations.SerializedName

data class Range(
    @SerializedName("min")
    val min: Int,
    @SerializedName("minExclusive")
    val minExclusive: Boolean,
    @SerializedName("max")
    val max: Int,
    @SerializedName("maxExclusive")
    val maxExclusive: Boolean
)
