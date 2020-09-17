package de.rki.coronawarnapp.test.api.ui

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TestForApiFragmentViewModel @AssistedInject constructor() : CWAViewModel() {

    val last3HourToggleEvent = SingleLiveEvent<Boolean>()

    fun setLast3HoursMode(isLast3HoursModeEnabled: Boolean) {
        LocalData.last3HoursMode(isLast3HoursModeEnabled)
        last3HourToggleEvent.postValue(isLast3HoursModeEnabled)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<TestForApiFragmentViewModel>

}
