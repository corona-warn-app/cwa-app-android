package de.rki.coronawarnapp.nearby.modules.exposurewindow

import android.content.Context
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import dagger.Reusable
import de.rki.coronawarnapp.storage.TestSettings.FakeExposureWindowTypes
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseJackson
import java.time.Duration
import javax.inject.Inject

@Reusable
class FakeExposureWindowProvider @Inject constructor(
    @AppContext val context: Context,
    @BaseJackson private val mapper: ObjectMapper,
    val timeStamper: TimeStamper
) {

    fun getExposureWindows(testSettings: FakeExposureWindowTypes): List<ExposureWindow> {
        val jsonInput = when (testSettings) {
            FakeExposureWindowTypes.INCREASED_RISK_DEFAULT -> "exposure-windows-increased-risk-random.json"
            FakeExposureWindowTypes.INCREASED_RISK_DUE_LOW_RISK_ENCOUNTER_DEFAULT -> "exposure-windows-increased-risk-due-to-low-risk-encounter-random.json"
            FakeExposureWindowTypes.LOW_RISK_DEFAULT -> "exposure-windows-lowrisk-random.json"
            else -> throw NotImplementedError()
        }.let { context.assets.open(it) }.readBytes().toString(Charsets.UTF_8)
        val jsonWindows: List<JsonWindow> = mapper.readValue(jsonInput)
        val nowUTC = timeStamper.nowUTC
        return jsonWindows.map { jWindow ->
            ExposureWindow.Builder().apply {
                setDateMillisSinceEpoch(nowUTC.minus(Duration.ofDays(jWindow.ageInDays.toLong())).toEpochMilli())
                setCalibrationConfidence(jWindow.calibrationConfidence)
                setInfectiousness(jWindow.infectiousness)
                setReportType(jWindow.reportType)

                jWindow.scanInstances.map { jScanInstance ->
                    ScanInstance.Builder().apply {
                        setMinAttenuationDb(jScanInstance.minAttenuation)
                        setSecondsSinceLastScan(jScanInstance.secondsSinceLastScan)
                        setTypicalAttenuationDb(jScanInstance.typicalAttenuation)
                    }.build()
                }.let { setScanInstances(it) }
            }.build()
        }
    }
}

private data class JsonScanInstance(
    @JsonProperty("minAttenuation") val minAttenuation: Int,
    @JsonProperty("secondsSinceLastScan") val secondsSinceLastScan: Int,
    @JsonProperty("typicalAttenuation") val typicalAttenuation: Int
)

private data class JsonWindow(
    @JsonProperty("ageInDays") val ageInDays: Int,
    @JsonProperty("calibrationConfidence") val calibrationConfidence: Int,
    @JsonProperty("infectiousness") val infectiousness: Int,
    @JsonProperty("reportType") val reportType: Int,
    @JsonProperty("scanInstances") val scanInstances: List<JsonScanInstance>
)
