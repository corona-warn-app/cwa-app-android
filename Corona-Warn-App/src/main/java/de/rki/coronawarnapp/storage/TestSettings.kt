package de.rki.coronawarnapp.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.serialization.BaseGson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSettings @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson
) {
    private val prefs by lazy {
        context.getSharedPreferences("test_settings", Context.MODE_PRIVATE)
    }

    val isDeviceTimeCheckDisabled = prefs.createFlowPreference(
        key = "config.devicetimecheck.disabled",
        defaultValue = false
    )

    val fakeMeteredConnection = prefs.createFlowPreference(
        key = "connections.metered.fake",
        defaultValue = false
    )

    val fakeExposureWindows = FlowPreference(
        preferences = prefs,
        key = "riskleve.exposurewindows.fake",
        reader = FlowPreference.gsonReader<FakeExposureWindowTypes>(gson, FakeExposureWindowTypes.DISABLED),
        writer = FlowPreference.gsonWriter(gson)
    )

    enum class FakeExposureWindowTypes {
        @SerializedName("DISABLED")
        DISABLED,

        @SerializedName("INCREASED_RISK_DEFAULT")
        INCREASED_RISK_DEFAULT,

        @SerializedName("LOW_RISK_DEFAULT")
        LOW_RISK_DEFAULT
    }
}
