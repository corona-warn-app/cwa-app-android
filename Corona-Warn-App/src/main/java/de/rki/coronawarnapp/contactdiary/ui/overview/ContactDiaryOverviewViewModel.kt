package de.rki.coronawarnapp.contactdiary.ui.overview

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ContactDiaryOverviewViewModel @com.squareup.inject.assisted.AssistedInject constructor() : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<ContactDiaryOverviewNavigationEvents> = SingleLiveEvent()

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryOverviewViewModel>

    fun onBackButtonPress() {
        routeToScreen.postValue(ContactDiaryOverviewNavigationEvents.NavigateToMainActivity)
    }
}
