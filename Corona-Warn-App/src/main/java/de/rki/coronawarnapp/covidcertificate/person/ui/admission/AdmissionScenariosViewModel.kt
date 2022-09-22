package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import androidx.annotation.VisibleForTesting
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccadmission.storage.DccAdmissionCheckScenariosRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.model.AdmissionScenarios
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class AdmissionScenariosViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val admissionCheckScenariosRepository: DccAdmissionCheckScenariosRepository,
    @Assisted private val admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel,
    private val cclSettings: CclSettings,
    private val dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger
) : CWAViewModel(dispatcherProvider) {

    val calculationState = SingleLiveEvent<CalculationState>()
    val state = admissionScenariosSharedViewModel.admissionScenarios.map { it.toScenarioItems() }.asLiveData2()

    private fun AdmissionScenarios.toScenarioItems() = State(
        title = title,
        scenarios = scenarios.map { scenario ->
            AdmissionItemCard.Item(
                identifier = scenario.identifier,
                title = scenario.title,
                subtitle = scenario.subtitle,
                enabled = scenario.enabled
            ) {
                selectScenario(scenario.identifier)
            }
        }
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun selectScenario(admissionScenarioId: String) = launch {
        runCatching {
            calculationState.postValue(Calculating) // Shows busy indicator
            // Save Admission Scenarios and selected Admission scenario Id
            admissionCheckScenariosRepository.save(
                json = admissionScenariosSharedViewModel.admissionScenarios.first().scenariosAsJson
            )
            cclSettings.saveAdmissionScenarioId(admissionScenarioId)
            // Calculate DccWalletInfo for certificate holders
            dccWalletInfoUpdateTrigger.triggerNow(admissionScenarioId = admissionScenarioId)
            calculationState.postValue(CalculationDone) // Dismiss busy indicator
        }.onFailure {
            Timber.d(it, "selectScenario() failed")
            calculationState.postValue(CalculationDone)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<AdmissionScenariosViewModel> {
        fun create(
            admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel
        ): AdmissionScenariosViewModel
    }

    data class State(
        val title: String,
        val scenarios: List<AdmissionItemCard.Item>
    )

    sealed interface CalculationState
    object Calculating : CalculationState
    object CalculationDone : CalculationState
}
