package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.open

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.BusinessRuleOpenVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.RuleHeaderVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationFaqVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationInputVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationOverallResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationResultItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class DccValidationOpenViewModel @AssistedInject constructor(
    @Assisted private val validation: DccValidation,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    val listItems: LiveData<List<ValidationResultItem>> = flow {
        emit(generateItems())
    }.asLiveData2()

    private suspend fun generateItems(): List<ValidationResultItem> {
        val items = mutableListOf(
            ValidationInputVH.Item(validation),
            ValidationOverallResultVH.Item(DccValidation.State.OPEN)
        )

        Timber.d("Generating items for state ${validation.state}")
        when (validation.state) {
            DccValidation.State.OPEN -> {
                val openRules = validation.rules.filter { it.result == DccValidationRule.Result.OPEN }
                items.add(RuleHeaderVH.Item(type = DccValidation.State.OPEN, showTitle = false))
                openRules.forEach { items.add(BusinessRuleOpenVH.Item(it)) }
            }
            DccValidation.State.PASSED -> {
                Timber.e("State PASSED but we are on screen 'OPEN', wrong navigation?")
            }
            DccValidation.State.TECHNICAL_FAILURE -> {
                Timber.e("State TECHNICAL_FAILURE but we are on screen 'OPEN', wrong navigation?")
            }
            DccValidation.State.FAILURE -> {
                Timber.e("State FAILURE but we are on screen 'OPEN', wrong navigation?")
            }
        }

        items.add(ValidationFaqVH.Item)

        return items.toList()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccValidationOpenViewModel> {
        fun create(validation: DccValidation): DccValidationOpenViewModel
    }
}
