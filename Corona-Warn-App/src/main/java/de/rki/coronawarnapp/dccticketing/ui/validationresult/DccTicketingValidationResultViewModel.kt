package de.rki.coronawarnapp.dccticketing.ui.validationresult

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ValidationResultItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DccTicketingValidationResultViewModel @Inject constructor(
    @Assisted private val dccTicketingSharedViewModel: DccTicketingSharedViewModel,
    private val itemCreator: ValidationResultItemCreator,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val navigation = SingleLiveEvent<DccTicketingValidationNavigation>()

    val uiStateFlow: LiveData<UiState> = dccTicketingSharedViewModel.transactionContext.map { context ->
        UiState(
            result = context.resultTokenPayload?.result ?: error("resultTokenPayload is null"),
            listItems = itemCreator.generateItems(
                context.resultTokenPayload,
                context.initializationData.serviceProvider
            )
        )
    }.asLiveData2()

    fun onDoneClicked() {
        navigation.postValue(DccTicketingValidationNavigation.Done)
    }

    fun onCloseClicked() {
        navigation.postValue(DccTicketingValidationNavigation.Close)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingValidationResultViewModel> {
        fun create(
            dccTicketingSharedViewModel: DccTicketingSharedViewModel,
        ): DccTicketingValidationResultViewModel
    }

    data class UiState(val result: DccTicketingResultToken.DccResult, val listItems: List<ValidationResultItem>)
}
