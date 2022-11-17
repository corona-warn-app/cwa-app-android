package de.rki.coronawarnapp.srs.ui.typeselection

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToCloseDialog
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToMainScreen
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToShareCheckins
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionNavigationEvents.NavigateToShareSymptoms
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class SrsTypeSelectionFragmentViewModel @AssistedInject constructor(
    private val checkInRepository: CheckInRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {
    private val srsTypes = flow {
        emit(
            SrsSubmissionType.values().filterNot { it == SrsSubmissionType.SRS_SELF_TEST }
        )
    }

    private val selectedType = MutableStateFlow<SrsSubmissionType?>(null)
    val navigation = SingleLiveEvent<SrsTypeSelectionNavigationEvents>()
    val types = combine(
        srsTypes,
        selectedType
    ) { srsSubmissionTypes, selectedType ->
        srsSubmissionTypes.map { type ->
            SrsTypeSelectionItem(
                checked = type == selectedType,
                submissionType = type
            )
        }
    }.asLiveData2()

    fun onCancel() {
        Timber.tag(TAG).d("onCancel()")
        navigation.postValue(NavigateToCloseDialog)
    }

    fun onNextClicked() = launch {
        val completedCheckInsExist = checkInRepository.completedCheckIns.first().isNotEmpty()
        val type = selectedType.value ?: run {
            Timber.tag(TAG).e("No type was selected")
            return@launch
        }
        if (completedCheckInsExist) {
            Timber.tag(TAG).d("Navigate to ShareCheckins")
            navigation.postValue(NavigateToShareCheckins(type))
        } else {
            Timber.tag(TAG).d("Navigate to ShareSymptoms")
            navigation.postValue(NavigateToShareSymptoms(type))
        }
    }

    fun onCancelConfirmed() {
        Timber.tag(TAG).d("onCancelConfirmed()")
        navigation.postValue(NavigateToMainScreen)
    }

    fun selectTypeListItem(selectionItem: SrsTypeSelectionItem) {
        selectedType.value = selectionItem.submissionType
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SrsTypeSelectionFragmentViewModel>

    companion object {
        private const val TAG = "SrsTypeSelectionFragmentViewModel"
    }
}
