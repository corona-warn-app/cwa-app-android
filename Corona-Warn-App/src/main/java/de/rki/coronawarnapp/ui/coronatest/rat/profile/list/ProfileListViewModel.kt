package de.rki.coronawarnapp.ui.coronatest.rat.profile.list

import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.coronatest.rat.profile.list.items.ProfileListItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ProfileListViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<ProfileListEvent>()

    //val profiles: LiveData<List<ProfileListItem>> =

    fun onCreateProfileClicked() {
        events.postValue(ProfileListEvent.NavigateToAddProfile)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ProfileListViewModel>
}
