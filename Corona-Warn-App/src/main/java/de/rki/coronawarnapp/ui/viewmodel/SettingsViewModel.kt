package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.util.di.Injector

/**
 * ViewModel for everything settings related.
 *
 * @see SettingsRepository
 */
class SettingsViewModel : ViewModel() {

    private val settingsRepository by lazy {
        Injector.component.settingsRepository
    }

    val isNotificationsEnabled: LiveData<Boolean> = settingsRepository.isNotificationsEnabled
    val isNotificationsRiskEnabled: LiveData<Boolean> =
        settingsRepository.isNotificationsRiskEnabled
    val isNotificationsTestEnabled: LiveData<Boolean> =
        settingsRepository.isNotificationsTestEnabled
    val isConnectionEnabled: LiveData<Boolean> = settingsRepository.isConnectionEnabled
    val isBluetoothEnabled: LiveData<Boolean> = settingsRepository.isBluetoothEnabled
    val isLocationEnabled: LiveData<Boolean> = settingsRepository.isLocationEnabled

    // Will impact UI if background activity is not permitted, persistent storing is not necessary
    val isBackgroundJobEnabled: LiveData<Boolean> = settingsRepository.isBackgroundJobEnabled

    val isBackgroundPriorityEnabled: LiveData<Boolean> =
        settingsRepository.isBackgroundPriorityEnabled

    /**
     * Is manual key retrieval enabled
     * Used for "Update" button on the Risk Card and in the Risk Details
     *
     * @see SettingsRepository.isManualKeyRetrievalEnabled
     */
    val isManualKeyRetrievalEnabled: LiveData<Boolean> =
        settingsRepository.isManualKeyRetrievalEnabled

    /**
     * Manual update button timer value
     *
     * @see SettingsRepository.manualKeyRetrievalTime
     */
    val manualKeyRetrievalTime: LiveData<Long> = settingsRepository.manualKeyRetrievalTime

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
     * @see settingsRepository
     */
    fun refreshNotificationsEnabled() {
        settingsRepository.refreshNotificationsEnabled()
    }

    /**
     * Refresh & toggle risk notifications
     */
    fun refreshNotificationsRiskEnabled() {
        settingsRepository.refreshNotificationsRiskEnabled()
    }

    fun toggleNotificationsRiskEnabled() {
        settingsRepository.toggleNotificationsRiskEnabled()
    }

    /**
     * Refresh & toggle test notifications
     */
    fun refreshNotificationsTestEnabled() {
        settingsRepository.refreshNotificationsTestEnabled()
    }

    fun toggleNotificationsTestEnabled() {
        settingsRepository.toggleNotificationsTestEnabled()
    }

    /**
     * Update connection enabled
     *
     * @param value
     */
    fun updateConnectionEnabled(value: Boolean) {
        settingsRepository.updateConnectionEnabled(value)
    }

    /**
     * Update bluetooth enabled
     *
     * @param value
     */
    fun updateBluetoothEnabled(value: Boolean) {
        settingsRepository.updateBluetoothEnabled(value)
    }

    /**
     * Update location enabled
     *
     * @param value
     */
    fun updateLocationEnabled(value: Boolean) {
        settingsRepository.updateLocationEnabled(value)
    }

    /**
     * Update background job enabled
     *
     * @param value
     */
    fun updateBackgroundJobEnabled(value: Boolean) {
        settingsRepository.updateBackgroundJobEnabled(value)
    }

    /**
     * Update manual key button enabled
     *
     * @param value
     */
    fun updateManualKeyRetrievalEnabled(value: Boolean) {
        settingsRepository.updateManualKeyRetrievalEnabled(value)
    }

    fun refreshBackgroundPriorityEnabled() {
        settingsRepository.refreshBackgroundPriorityEnabled()
    }
}
