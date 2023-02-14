package de.rki.coronawarnapp.profile.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import de.rki.coronawarnapp.profile.ui.list.items.ProfileCard
import de.rki.coronawarnapp.profile.ui.list.items.ProfileListItem
import de.rki.coronawarnapp.profile.ui.qrcode.VCard
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ProfileListViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    profileRepository: ProfileRepository,
    private val vCard: VCard,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<ProfileListEvent>()

    val profiles: LiveData<List<ProfileListItem>> = profileRepository.profilesFlow
        .map { profiles ->
            profiles.sortedWith(compareBy({ it.firstName }, { it.lastName }))
        }
        .map { profiles ->
            profiles.map { profile ->
                ProfileCard.Item(
                    profile = profile,
                    qrCode = profile.let { vCard.create(it) },
                    onClickAction = { _, position ->
                        profile.id?.let {
                            events.postValue(ProfileListEvent.OpenProfile(profile.id, position))
                        }
                    }
                )
            }
        }.asLiveData(context = dispatcherProvider.Default)

    fun onCreateProfileClicked() {
        events.postValue(ProfileListEvent.NavigateToAddProfile)
    }
}
