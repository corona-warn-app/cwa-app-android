package de.rki.coronawarnapp.storage.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SettingsData constructor(
    private val sharedPreferences: SharedPreferences
) {

    private val isNotificationsRiskEnabledFlowInternal by lazy {
        MutableStateFlow(isNotificationsRiskEnabled)
    }
    val isNotificationsRiskEnabledFlow: Flow<Boolean> by lazy {
        isNotificationsRiskEnabledFlowInternal
    }
    var isNotificationsRiskEnabled: Boolean
        get() = sharedPreferences.getBoolean(PKEY_NOTIFICATIONS_RISK_ENABLED, true)
        set(value) = sharedPreferences.edit(true) {
            putBoolean(PKEY_NOTIFICATIONS_RISK_ENABLED, value)
            isNotificationsRiskEnabledFlowInternal.value = value
        }

    fun clear() {
        sharedPreferences.edit(true) {
            remove(PKEY_NOTIFICATIONS_RISK_ENABLED)
        }
    }

    fun containsData(): Boolean {
        return sharedPreferences.contains(PKEY_NOTIFICATIONS_RISK_ENABLED)
    }

    companion object {
        private const val PKEY_NOTIFICATIONS_RISK_ENABLED = "preference_notifications_risk_enabled"
    }
}
