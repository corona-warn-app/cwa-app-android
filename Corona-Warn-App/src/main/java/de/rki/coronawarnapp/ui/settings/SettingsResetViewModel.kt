package de.rki.coronawarnapp.ui.settings

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.DataReset
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SettingsResetViewModel @AssistedInject constructor() : CWAViewModel() {
    val clickEvent: SingleLiveEvent<SettingsEvents> = SingleLiveEvent()

    fun resetAllData() {
        clickEvent.postValue(SettingsEvents.ResetApp)
    }

    fun goBack() {
        clickEvent.postValue(SettingsEvents.GoBack)
    }

    @AssistedInject.Factory
    interface  Factory: SimpleCWAViewModelFactory<SettingsResetViewModel>
}
