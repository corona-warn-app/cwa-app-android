package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.failed

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.BusinessRuleFailedVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.BusinessRuleOpenVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.RuleHeaderVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.TechnicalValidationFailedVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationFaqVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationInputVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationOverallResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationPassedHintVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationResultItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.flowOf

class DccValidationFailedViewModel @AssistedInject constructor(
    @Assisted private val validation: DccValidation,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    val listItems: LiveData<List<ValidationResultItem>> = flowOf(
        listOf(
            ValidationInputVH.Item(validation),
            ValidationOverallResultVH.Item(validation.state),
            TechnicalValidationFailedVH.Item(validation),
            RuleHeaderVH.Item(DccValidation.State.FAILURE),
            BusinessRuleFailedVH.Item(validation.invalidationRules.first()),
            RuleHeaderVH.Item(DccValidation.State.OPEN),
            BusinessRuleOpenVH.Item(validation.acceptanceRules.first()),
            ValidationFaqVH.Item,
            ValidationPassedHintVH.Item,
        )
    ).asLiveData2()

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccValidationFailedViewModel> {
        fun create(validation: DccValidation): DccValidationFailedViewModel
    }
}
