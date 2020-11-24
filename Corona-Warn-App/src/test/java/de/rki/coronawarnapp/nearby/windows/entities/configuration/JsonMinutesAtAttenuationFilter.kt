package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.google.gson.annotations.SerializedName

data class JsonMinutesAtAttenuationFilter(
    @SerializedName("attenuationRange")
    val attenuationRange: Range,
    @SerializedName("dropIfMinutesInRange")
    val dropIfMinutesInRange: Range
)
