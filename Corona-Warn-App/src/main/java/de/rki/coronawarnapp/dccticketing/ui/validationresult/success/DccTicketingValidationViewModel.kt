package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.ValidationResultItemCreator
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.items.ValidationResultItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class DccTicketingValidationViewModel @AssistedInject constructor(
    @Assisted private val transactionContext: DccTicketingTransactionContext,
    val itemCreator: ValidationResultItemCreator,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val navigation = SingleLiveEvent<DccTicketingValidationNavigation>()

    val items: LiveData<List<ValidationResultItem>> = flow {
        emit(generateItems())
    }.asLiveData2()

    private fun generateItems(): List<ValidationResultItem> = with(itemCreator) {
        listOf(
            validationFaqVHItem()
        )
    }

    fun onDoneClicked() {
        Timber.d("onDoneClicked()")
        navigation.postValue(DccTicketingValidationNavigation.Back)
    }

    fun onCloseClicked() {
        Timber.d("onCloseClicked()")
        navigation.postValue(DccTicketingValidationNavigation.Back)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingValidationViewModel> {
        fun create(transactionContext: DccTicketingTransactionContext): DccTicketingValidationViewModel
    }
}
