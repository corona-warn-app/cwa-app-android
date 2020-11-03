package de.rki.coronawarnapp.nearby.windows.entities.cases

import com.google.gson.annotations.SerializedName

data class ExposureWindow(
    @SerializedName("ageInDays")
    val ageInDays: Int,
    @SerializedName("calibrationConfidence")
    val calibrationConfidence: Int,
    @SerializedName("infectiousness")
    val infectiousness: Int,
    @SerializedName("reportType")
    val reportType: Int,
    @SerializedName("scanInstances")
    val scanInstances: List<ScanInstance>
)
