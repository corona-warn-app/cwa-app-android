package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.passed

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.ValidationResultItemCreator
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationResultItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class DccValidationPassedViewModel @AssistedInject constructor(
    @Assisted private val validation: DccValidation,
    val itemCreator: ValidationResultItemCreator,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val navigation = SingleLiveEvent<DccValidationPassedNavigation>()

    val items: LiveData<List<ValidationResultItem>> = flow {
        emit(generateItems())
    }.asLiveData2()

    private fun generateItems(): List<ValidationResultItem> {
        Timber.d("generateItems()")

        if (validation.state != DccValidation.State.PASSED) {
            throw IllegalStateException(
                "Expected validation state to be ${DccValidation.State.PASSED.name} " +
                    "but was ${validation.state.name}"
            )
        }

        return with(itemCreator) {
            listOf(
                validationInputVHItem(userInput = validation.userInput, validatedAt = validation.validatedAt),
                validationOverallResultVHItem(state = validation.state),
                ruleHeaderVHItem(
                    state = validation.state,
                    hideTitle = true,
                    ruleCount = validation.acceptanceRules.size
                ),
                validationPassedHintVHItem(),
                validationFaqVHItem()
            )
        }
    }

    fun onCheckAnotherCountryClicked() {
        Timber.d("onCheckAnotherCountryClicked()")
        navigation.postValue(DccValidationPassedNavigation.Back)
    }

    fun onCloseClicked() {
        Timber.d("onCloseClicked()")
        navigation.postValue(DccValidationPassedNavigation.Back)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccValidationPassedViewModel> {
        fun create(validation: DccValidation): DccValidationPassedViewModel
    }
}
