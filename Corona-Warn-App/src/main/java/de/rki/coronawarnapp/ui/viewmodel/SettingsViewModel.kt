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

    // Todo bind to os settings, care API 23 / API 24 onwards
    // Will impact UI if background activity is not permitted, persistent storing is not necessary
    val isBackgroundJobEnabled: LiveData<Boolean> = SettingsRepository.isBackgroundJobEnabled

    /**
     * Is manual key retrieval enabled
     * Used for "Update" button on the Risk Card and in the Risk Details
     *
     * @see SettingsRepository.isManualKeyRetrievalEnabled
     */
    val isManualKeyRetrievalEnabled: LiveData<Boolean> = SettingsRepository.isManualKeyRetrievalEnabled

    /**
     * Update button on the Risk Card and in the Risk Details live text
     *
     * @see SettingsRepository.manualKeyRetrievalText
     */
    val manualKeyRetrievalText: LiveData<String> = SettingsRepository.manualKeyRetrievalText

    /**
     * Refresher and toggler for settings
     * - Notifications overall
     *  - Risk updates
     *  - Test updates
     *  - News updates
     *  - App updates
     * - Mobile data // TODO should be removed
     * - Background jobs // TODO could be removed
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
     * Update background job enabled
     *
     * @param value
     */
    fun updateBackgroundJobEnabled(value: Boolean) {
        SettingsRepository.updateBackgroundJobEnabled(value)
    }
}
