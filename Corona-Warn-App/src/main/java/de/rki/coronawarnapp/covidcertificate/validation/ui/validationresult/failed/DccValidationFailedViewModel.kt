package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.failed

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.ValidationResultItemCreator
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationResultItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class DccValidationFailedViewModel @AssistedInject constructor(
    @Assisted private val validation: DccValidation,
    @Assisted private val containerId: CertificateContainerId,
    private val certificateProvider: CertificateProvider,
    private val itemCreator: ValidationResultItemCreator,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    val listItems: LiveData<List<ValidationResultItem>> = flow {
        emit(generateItems())
    }.asLiveData2()

    private suspend fun generateItems(): List<ValidationResultItem> = with(itemCreator) {
        val items = mutableListOf(
            validationInputVHItem(userInput = validation.userInput, validatedAt = validation.validatedAt),
            validationOverallResultVHItem(state = DccValidation.State.FAILURE)
        )

        Timber.d("Generating items for state ${validation.state}")
        when (validation.state) {
            DccValidation.State.PASSED,
            DccValidation.State.OPEN -> throw IllegalArgumentException(
                "Expected state to be ${DccValidation.State.FAILURE.name} or" +
                    " ${DccValidation.State.TECHNICAL_FAILURE.name} but was ${validation.state.name}"
            )
            DccValidation.State.TECHNICAL_FAILURE -> {
                items.add(ruleHeaderVHItem(state = DccValidation.State.TECHNICAL_FAILURE))
                items.add(technicalValidationFailedVHItem(validation = validation))
            }
            DccValidation.State.FAILURE -> {
                val certificate = certificateProvider.findCertificate(containerId)
                val failedRules = validation.rules
                    .filter { it.result == DccValidationRule.Result.FAILED }
                    .map {
                        businessRuleVHItem(
                            rule = it.rule,
                            result = it.result,
                            certificate = certificate
                        )
                    }
                if (failedRules.isNotEmpty()) {
                    items.add(ruleHeaderVHItem(state = DccValidation.State.FAILURE))
                    items.addAll(failedRules)
                }

                val openRules = validation.rules
                    .filter { it.result == DccValidationRule.Result.OPEN }
                    .map {
                        businessRuleVHItem(
                            rule = it.rule,
                            result = it.result,
                            certificate = certificate
                        )
                    }
                if (openRules.isNotEmpty()) {
                    items.add(ruleHeaderVHItem(state = DccValidation.State.OPEN))
                    items.addAll(openRules)
                }
            }
        }

        items.add(validationFaqVHItem())

        return items.toList()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccValidationFailedViewModel> {
        fun create(
            validation: DccValidation,
            containerId: CertificateContainerId
        ): DccValidationFailedViewModel
    }
}
