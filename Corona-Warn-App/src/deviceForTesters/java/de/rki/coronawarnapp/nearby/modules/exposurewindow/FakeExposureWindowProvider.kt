package de.rki.coronawarnapp.nearby.modules.exposurewindow

import android.content.Context
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.Reusable
import de.rki.coronawarnapp.storage.TestSettings.FakeExposureWindowTypes
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import org.joda.time.Duration
import javax.inject.Inject

@Reusable
class FakeExposureWindowProvider @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val gson: Gson,
    val timeStamper: TimeStamper
) {

    fun getExposureWindows(testSettings: FakeExposureWindowTypes): List<ExposureWindow> {
        val jsonInput = when (testSettings) {
            FakeExposureWindowTypes.INCREASED_RISK_DEFAULT -> "exposure-windows-increased-risk-random.json"
            FakeExposureWindowTypes.LOW_RISK_DEFAULT -> "exposure-windows-lowrisk-random.json"
            else -> throw NotImplementedError()
        }.let { context.assets.open(it) }.readBytes().toString(Charsets.UTF_8)
        val jsonWindows: List<JsonWindow> = gson.fromJson(jsonInput)
        val nowUTC = timeStamper.nowUTC
        return jsonWindows.map { jWindow ->
            ExposureWindow.Builder().apply {
                setDateMillisSinceEpoch(nowUTC.minus(Duration.standardDays(jWindow.ageInDays.toLong())).millis)
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
    @SerializedName("minAttenuation") val minAttenuation: Int,
    @SerializedName("secondsSinceLastScan") val secondsSinceLastScan: Int,
    @SerializedName("typicalAttenuation") val typicalAttenuation: Int
)

private data class JsonWindow(
    @SerializedName("ageInDays") val ageInDays: Int,
    @SerializedName("calibrationConfidence") val calibrationConfidence: Int,
    @SerializedName("infectiousness") val infectiousness: Int,
    @SerializedName("reportType") val reportType: Int,
    @SerializedName("scanInstances") val scanInstances: List<JsonScanInstance>
)
