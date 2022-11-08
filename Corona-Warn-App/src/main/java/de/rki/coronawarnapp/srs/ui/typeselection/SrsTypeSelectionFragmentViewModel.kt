package de.rki.coronawarnapp.srs.ui.typeselection

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToCloseDialog
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToMainScreen
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToShareCheckins
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow

class SrsTypeSelectionFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val navigation = SingleLiveEvent<SrsTypeSelectionNavigationEvents>()

    private val srsTestTypeInternal = MutableStateFlow(SrsSubmissionType.SRS_OTHER)
    val srsTestType = srsTestTypeInternal.asLiveData(context = dispatcherProvider.Default)

    fun onCancel() {
        navigation.postValue(NavigateToCloseDialog)
    }

    fun onNextClicked() {
        navigation.postValue(NavigateToShareCheckins)
    }

    fun onCancelConfirmed() {
        navigation.postValue(NavigateToMainScreen)
    }

    fun onRatRegisteredNoResult() {
        updateTypeSelection(SrsSubmissionType.SRS_RAT)
    }

    fun onRatNotRegistered() {
        // TODO: RAT unregistered
        updateTypeSelection(SrsSubmissionType.SRS_RAT)
    }

    fun onPcrRegisteredNoResult() {
        updateTypeSelection(SrsSubmissionType.SRS_REGISTERED_PCR)
    }

    fun onPcrNotRegistered() {
        updateTypeSelection(SrsSubmissionType.SRS_UNREGISTERED_PCR)
    }

    fun onRapidPcr() {
        updateTypeSelection(SrsSubmissionType.SRS_RAPID_PCR)
    }

    fun onOther() {
        updateTypeSelection(SrsSubmissionType.SRS_OTHER)
    }

    private fun updateTypeSelection(typeSelection: SrsSubmissionType) {
        srsTestTypeInternal.value = typeSelection
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SrsTypeSelectionFragmentViewModel>
}
