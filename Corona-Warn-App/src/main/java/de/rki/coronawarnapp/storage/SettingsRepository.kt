package de.rki.coronawarnapp.storage

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import de.rki.coronawarnapp.util.BackgroundPrioritization
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Settings Repository maps all setting states from different sources to MutableLiveData.
 * These are than used in the corresponding view model. Variables which consume the shared
 * preferences over the LocalData have a refresh and a toggle or update function. Data which is
 * consumed directly from the operating system only has a refresh function.
 *
 * @see LocalData
 */
@Singleton
class SettingsRepository @Inject constructor(
    @AppContext private val context: Context,
    private val backgroundPrioritization: BackgroundPrioritization
) {

    // public mutable live data
    val isNotificationsEnabled = MutableLiveData(true)
    val isNotificationsRiskEnabled = MutableLiveData(true)
    val isNotificationsTestEnabled = MutableLiveData(true)
    val isConnectionEnabled = MutableLiveData(true)
    val isBluetoothEnabled = MutableLiveData(true)
    val isLocationEnabled = MutableLiveData(true)
    val isBackgroundJobEnabled = MutableLiveData(true)
    val isBackgroundPriorityEnabled = MutableLiveData(false)

    /**
     * Get the current notifications state. Only relevant for the ui.
     *
     * @see LocalData
     */
    fun refreshNotificationsEnabled() {
        isNotificationsEnabled.value =
            NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Toggle notifications risk updates.
     *
     * @see LocalData
     */
    fun toggleNotificationsRiskEnabled() {
        LocalData.toggleNotificationsRiskEnabled()
        refreshNotificationsRiskEnabled()
    }

    /**
     * Refresh notifications for risk updates with the current shared preferences state.
     *
     * @see LocalData
     */
    fun refreshNotificationsRiskEnabled() {
        isNotificationsRiskEnabled.value = LocalData.isNotificationsRiskEnabled()
    }

    /**
     * Toggle notifications for test updates in shared preferences and refresh it afterwards.
     *
     * @see LocalData
     */
    fun toggleNotificationsTestEnabled() {
        LocalData.toggleNotificationsTestEnabled()
        refreshNotificationsTestEnabled()
    }

    /**
     * Refresh notifications for test updates with the current shared preferences state.
     *
     * @see LocalData
     */
    fun refreshNotificationsTestEnabled() {
        isNotificationsTestEnabled.value = LocalData.isNotificationsTestEnabled()
    }

    /**
     * Toggle notifications for test updates in SharedPreferences and refreshes it afterwards
     *
     * @see LocalData
     */
    fun updateConnectionEnabled(value: Boolean) {
        isConnectionEnabled.postValue(value)
    }

    /**
     * Refresh global bluetooth state to point out that tracing isn't working
     *
     * @see ConnectivityHelper
     */
    fun updateBluetoothEnabled(value: Boolean) {
        isBluetoothEnabled.postValue(value)
    }

    /**
     * Refresh global location state to point out that tracing isn't working
     *
     * @see ConnectivityHelper
     */
    fun updateLocationEnabled(value: Boolean) {
        isLocationEnabled.postValue(value)
    }

    /**
     * Refresh global bluetooth state to point out that tracing isn't working
     *
     * @see ConnectivityHelper
     */
    fun updateBackgroundJobEnabled(value: Boolean) {
        isBackgroundJobEnabled.postValue(value)
    }

    private val internalIsManualKeyRetrievalEnabled = MutableStateFlow(true)
    val isManualKeyRetrievalEnabledFlow: Flow<Boolean> = internalIsManualKeyRetrievalEnabled
    val isManualKeyRetrievalEnabled = isManualKeyRetrievalEnabledFlow.asLiveData()

    /**
     * Refresh manual key retrieval button status
     */
    fun updateManualKeyRetrievalEnabled(value: Boolean) {
        internalIsManualKeyRetrievalEnabled.value = value
    }

    private val internalManualKeyRetrievalTime = MutableStateFlow(0L)
    val manualKeyRetrievalTimeFlow: Flow<Long> = internalManualKeyRetrievalTime
    val manualKeyRetrievalTime = manualKeyRetrievalTimeFlow.asLiveData()

    /**
     * Refresh manual key retrieval button status
     */
    fun updateManualKeyRetrievalTime(value: Long) {
        internalManualKeyRetrievalTime.value = value
    }

    /**
     * Refresh the current background priority state.
     */
    fun refreshBackgroundPriorityEnabled() {
        isBackgroundPriorityEnabled.value = backgroundPrioritization.isBackgroundActivityPrioritized
    }
}
