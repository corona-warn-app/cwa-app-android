package de.rki.coronawarnapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.storage.SettingsRepository

/**
 * ViewModel for everything settings related.
 *
 * @see SettingsRepository
 */
class SettingsViewModel : ViewModel() {

    val isNotificationsEnabled: LiveData<Boolean> = SettingsRepository.isNotificationsEnabled
    val isNotificationsRiskEnabled: LiveData<Boolean> =
        SettingsRepository.isNotificationsRiskEnabled
    val isNotificationsTestEnabled: LiveData<Boolean> =
        SettingsRepository.isNotificationsTestEnabled
    val isConnectionEnabled: LiveData<Boolean> =
        SettingsRepository.isConnectionEnabled
    val isBluetoothEnabled: LiveData<Boolean> =
        SettingsRepository.isBluetoothEnabled
    val isLocationEnabled: LiveData<Boolean> =
        SettingsRepository.isLocationEnabled

    // Will impact UI if background activity is not permitted, persistent storing is not necessary
    val isBackgroundJobEnabled: LiveData<Boolean> = SettingsRepository.isBackgroundJobEnabled

    val isBackgroundPriorityEnabled: LiveData<Boolean> =
        SettingsRepository.isBackgroundPriorityEnabled

    /**
     * Is manual key retrieval enabled
     * Used for "Update" button on the Risk Card and in the Risk Details
     *
     * @see SettingsRepository.isManualKeyRetrievalEnabled
     */
    val isManualKeyRetrievalEnabled: LiveData<Boolean> =
        SettingsRepository.isManualKeyRetrievalEnabled

    /**
     * Manual update button timer value
     *
     * @see SettingsRepository.manualKeyRetrievalTime
     */
    val manualKeyRetrievalTime: LiveData<Long> = SettingsRepository.manualKeyRetrievalTime

    /**
     * Refresher and toggles for settings
     * - Notifications overall
     *  - Risk updates
     *  - Test updates
     *  - News updates
     *  - App updates
     *  - Connectivity
     *  - Background activity
     *
     * @see SettingsRepository
     */
    fun refreshNotificationsEnabled(context: Context) {
        SettingsRepository.refreshNotificationsEnabled(context)
    }

    /**
     * Refresh & toggle risk notifications
     */
    fun refreshNotificationsRiskEnabled() {
        SettingsRepository.refreshNotificationsRiskEnabled()
    }

    fun toggleNotificationsRiskEnabled() {
        SettingsRepository.toggleNotificationsRiskEnabled()
    }

    /**
     * Refresh & toggle test notifications
     */
    fun refreshNotificationsTestEnabled() {
        SettingsRepository.refreshNotificationsTestEnabled()
    }

    fun toggleNotificationsTestEnabled() {
        SettingsRepository.toggleNotificationsTestEnabled()
    }

    /**
     * Update connection enabled
     *
     * @param value
     */
    fun updateConnectionEnabled(value: Boolean) {
        SettingsRepository.updateConnectionEnabled(value)
    }

    /**
     * Update bluetooth enabled
     *
     * @param value
     */
    fun updateBluetoothEnabled(value: Boolean) {
        SettingsRepository.updateBluetoothEnabled(value)
    }

    /**
     * Update location enabled
     *
     * @param value
     */
    fun updateLocationEnabled(value: Boolean) {
        SettingsRepository.updateLocationEnabled(value)
    }

    /**
     * Update background job enabled
     *
     * @param value
     */
    fun updateBackgroundJobEnabled(value: Boolean) {
        SettingsRepository.updateBackgroundJobEnabled(value)
    }

    /**
     * Update manual key button enabled
     *
     * @param value
     */
    fun updateManualKeyRetrievalEnabled(value: Boolean) {
        SettingsRepository.updateManualKeyRetrievalEnabled(value)
    }

    fun refreshBackgroundPriorityEnabled(context: Context) {
        SettingsRepository.refreshBackgroundPriorityEnabled(context)
    }
}
