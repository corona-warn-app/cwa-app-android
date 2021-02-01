package de.rki.coronawarnapp.datadonation.analytics

import android.content.Context
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.ExposureRiskMetadata
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsSettings @Inject constructor(
    @AppContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("analytics_localdata", Context.MODE_PRIVATE)
    }

    val previousExposureRiskMetadata = FlowPreference(
        preferences = prefs,
        key = PREVIOUS_EXPOSURE_RISK_METADATA,
        reader = { key ->
            getString(key, null)?.let {
                ExposureRiskMetadata.parseFrom(ByteString.copyFromUtf8(it))
            }
        },
        writer = { key, value ->
            putString(key, value?.toByteString()?.toStringUtf8())
        }
    )

    companion object {
        private const val PREVIOUS_EXPOSURE_RISK_METADATA = "exposurerisk.metadata.previous"
    }
}
