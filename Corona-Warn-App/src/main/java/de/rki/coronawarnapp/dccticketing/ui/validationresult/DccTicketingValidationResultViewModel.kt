package de.rki.coronawarnapp.dccticketing.ui.validationresult

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ValidationResultItem
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.flow

class DccTicketingValidationResultViewModel @AssistedInject constructor(
    @Assisted private val transactionContext: DccTicketingTransactionContext,
    private val itemCreator: ValidationResultItemCreator,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val navigation = SingleLiveEvent<DccTicketingValidationNavigation>()

    val items: LiveData<List<ValidationResultItem>> = flow {
        emit(generateItems())
    }.asLiveData2()

    private fun generateItems(): List<ValidationResultItem> = with(itemCreator) {
        mutableListOf(
            testingInfoVHItem(transactionContext.resultTokenPayload?.iat?.secondsToInstant()),
            descriptionVHItem(
                transactionContext.resultTokenPayload?.result,
                transactionContext.initializationData.serviceProvider
            ),
            faqVHItem()
        ).apply {
            transactionContext.resultTokenPayload?.results?.forEach {
                add(resultRuleVHItem(it))
            }
        }
    }

    fun onDoneClicked() {
        navigation.postValue(DccTicketingValidationNavigation.Done)
    }

    fun onCloseClicked() {
        navigation.postValue(DccTicketingValidationNavigation.Close)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingValidationResultViewModel> {
        fun create(transactionContext: DccTicketingTransactionContext): DccTicketingValidationResultViewModel
    }
}
