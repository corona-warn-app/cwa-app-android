package de.rki.coronawarnapp.nearby.windows.entities.configuration

import com.google.gson.annotations.SerializedName

data class JsonTrlFilter(
    @SerializedName("dropIfTrlInRange")
    val dropIfTrlInRange: Range
)
