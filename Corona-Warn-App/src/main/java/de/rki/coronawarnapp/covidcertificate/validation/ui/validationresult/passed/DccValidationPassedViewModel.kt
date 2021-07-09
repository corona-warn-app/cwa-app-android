package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.passed

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationFaqVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationInputVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationOverallResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationPassedHintVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationResultItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.flow

class DccValidationPassedViewModel @AssistedInject constructor(
    @Assisted private val validation: DccValidation,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val items: LiveData<List<ValidationResultItem>> = flow {
        emit(generateItems())
    }.asLiveData2()

    private fun generateItems(): List<ValidationResultItem> = listOf(
        ValidationInputVH.Item(validation),
        ValidationOverallResultVH.Item(validation.state),
        ValidationPassedHintVH.Item,
        ValidationFaqVH.Item
    )

    fun onCheckAnotherCountryClicked() {
        //TODO(Not yet implemented)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccValidationPassedViewModel> {
        fun create(validation: DccValidation): DccValidationPassedViewModel
    }
}
