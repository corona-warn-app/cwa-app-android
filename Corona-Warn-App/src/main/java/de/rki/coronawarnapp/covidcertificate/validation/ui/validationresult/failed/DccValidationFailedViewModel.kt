package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.failed

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.BusinessRuleFailedVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.BusinessRuleOpenVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.RuleHeaderVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.TechnicalValidationFailedVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationFaqVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationInputVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationOverallResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationResultItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class DccValidationFailedViewModel @AssistedInject constructor(
    @Assisted private val validation: DccValidation,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    val listItems: LiveData<List<ValidationResultItem>> = flow {
        emit(generateItems())
    }.asLiveData2()

    private suspend fun generateItems(): List<ValidationResultItem> {
        val items = mutableListOf(
            ValidationInputVH.Item(validation),
            ValidationOverallResultVH.Item(DccValidation.State.FAILURE)
        )

        Timber.d("Generating items for state ${validation.state}")
        when (validation.state) {
            DccValidation.State.PASSED -> {
                Timber.e("State PASSED but we are on screen 'FAILED', wrong navigation?")
            }
            DccValidation.State.OPEN -> {
                Timber.e("State OPEN but we are on screen 'FAILED', wrong navigation?")
            }
            DccValidation.State.TECHNICAL_FAILURE -> {
                items.add(RuleHeaderVH.Item(DccValidation.State.TECHNICAL_FAILURE))
                items.add(TechnicalValidationFailedVH.Item(validation))
            }
            DccValidation.State.FAILURE -> {
                val failedRules = validation.rules.filter { it.result == DccValidationRule.Result.FAILED }
                if (failedRules.isNotEmpty()) {
                    items.add(RuleHeaderVH.Item(DccValidation.State.FAILURE))
                    failedRules.forEach { items.add(BusinessRuleFailedVH.Item(it)) }
                }

                val openRules = validation.rules.filter { it.result == DccValidationRule.Result.OPEN }
                if (openRules.isNotEmpty()) {
                    items.add(RuleHeaderVH.Item(DccValidation.State.OPEN))
                    openRules.forEach { items.add(BusinessRuleOpenVH.Item(it)) }
                }
            }
        }

        items.add(ValidationFaqVH.Item)

        return items.toList()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccValidationFailedViewModel> {
        fun create(validation: DccValidation): DccValidationFailedViewModel
    }
}
