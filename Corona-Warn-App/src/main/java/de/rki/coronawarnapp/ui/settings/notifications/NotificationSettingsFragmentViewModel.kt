package de.rki.coronawarnapp.ui.settings.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import timber.log.Timber

class NotificationSettingsFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val notificationSettings: NotificationSettings
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val notificationSettingsState: LiveData<NotificationSettingsState> = combine(
        notificationSettings.isNotificationsEnabled,
        notificationSettings.isNotificationsRiskEnabled,
        notificationSettings.isNotificationsTestEnabled
    ) { args ->
        NotificationSettingsState(
            isNotificationsEnabled = args[0],
            isNotificationsRiskEnabled = args[1],
            isNotificationsTestEnabled = args[2]
        ).also {
            Timber.v("New notification state: %s", it)
        }
    }.asLiveData(dispatcherProvider.Default)

    fun toggleNotificationsRiskEnabled() {
        notificationSettings.toggleNotificationsRiskEnabled()
    }

    fun toggleNotificationsTestEnabled() {
        notificationSettings.toggleNotificationsTestEnabled()
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<NotificationSettingsFragmentViewModel>
}
