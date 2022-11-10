package de.rki.coronawarnapp.srs.ui.typeselection

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToCloseDialog
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToMainScreen
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToShareCheckins
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToShareSymptoms
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SrsTypeSelectionFragmentViewModel @AssistedInject constructor(
    private val checkInRepository: CheckInRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val navigation = SingleLiveEvent<SrsTypeSelectionNavigationEvents>()

    private val srsTestTypeInternal = MutableStateFlow(SrsTypeSelectionItem(0, 0, null))
    val srsTestType = srsTestTypeInternal.asLiveData(context = dispatcherProvider.Default)

    fun onCancel() {
        Timber.tag(TAG).d("onCancel()")
        navigation.postValue(NavigateToCloseDialog)
    }

    fun onNextClicked() = launch {
        val completedCheckInsExist = checkInRepository.completedCheckIns.first().isNotEmpty()
        if (completedCheckInsExist) {
            Timber.tag(TAG).d("Navigate to ShareCheckins")
            navigation.postValue(NavigateToShareCheckins)
        } else {
            Timber.tag(TAG).d("Navigate to ShareSymptoms")
            navigation.postValue(NavigateToShareSymptoms)
        }
    }

    fun onCancelConfirmed() {
        Timber.tag(TAG).d("onCancelConfirmed()")
        navigation.postValue(NavigateToMainScreen)
    }

    private fun updateTypeSelection(typeSelection: SrsTypeSelectionItem) {
        srsTestTypeInternal.value = typeSelection
    }

    fun selectTypeListItem(selectionItem: SrsTypeSelectionItem) {
        updateTypeSelection(selectionItem)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SrsTypeSelectionFragmentViewModel>

    companion object {
        private const val TAG = "SrsTypeSelectionFragmentViewModel"
    }
}
