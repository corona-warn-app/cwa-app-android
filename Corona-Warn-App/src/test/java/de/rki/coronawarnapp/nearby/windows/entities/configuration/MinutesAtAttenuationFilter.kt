package de.rki.coronawarnapp.nearby.windows.entities.configuration


import com.google.gson.annotations.SerializedName

data class MinutesAtAttenuationFilter(
    @SerializedName("attenuationRange")
    val attenuationRange: Range,
    @SerializedName("dropIfMinutesInRange")
    val dropIfMinutesInRange: Range
)
