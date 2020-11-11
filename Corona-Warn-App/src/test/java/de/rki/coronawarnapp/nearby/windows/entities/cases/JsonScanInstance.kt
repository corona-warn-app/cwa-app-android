package de.rki.coronawarnapp.nearby.windows.entities.cases

import com.google.gson.annotations.SerializedName

data class JsonScanInstance(
    @SerializedName("minAttenuation")
    val minAttenuation: Int,
    @SerializedName("secondsSinceLastScan")
    val secondsSinceLastScan: Int,
    @SerializedName("typicalAttenuation")
    val typicalAttenuation: Int
)
