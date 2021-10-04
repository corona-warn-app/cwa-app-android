package de.rki.coronawarnapp.ui.settings.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class NotificationSettingsFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    notificationSettings: NotificationSettings
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val notificationSettingsState: LiveData<NotificationSettingsState> = notificationSettings
        .isNotificationsEnabled
        .map { NotificationSettingsState(it) }
        .asLiveData(dispatcherProvider.Default)

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<NotificationSettingsFragmentViewModel>
}
