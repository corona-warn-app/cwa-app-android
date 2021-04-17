package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map

class CheckInsConsentViewModel @AssistedInject constructor(
    @Assisted private val savedState: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    checkInRepository: CheckInRepository,
) : CWAViewModel(dispatcherProvider) {

    val checkIns: LiveData<List<CheckInsConsentItem>> = checkInRepository
        .checkInsWithinRetention
        .map {
            mutableListOf<CheckInsConsentItem>().apply {
                add(
                    HeaderCheckInsVH.Item(selectAll = {})
                )
            }
        }.asLiveData(context = dispatcherProvider.Default)

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CheckInsConsentViewModel> {
        fun create(
            savedState: SavedStateHandle,
        ): CheckInsConsentViewModel
    }
}
