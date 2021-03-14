package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.google.gson.annotations.SerializedName

data class JsonMinutesAtAttenuationWeight(
    @SerializedName("attenuationRange")
    val attenuationRange: Range,
    @SerializedName("weight")
    val weight: Double
)
