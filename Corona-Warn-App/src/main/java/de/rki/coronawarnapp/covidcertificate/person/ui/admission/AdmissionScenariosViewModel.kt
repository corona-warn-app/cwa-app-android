package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccadmission.model.DccAdmissionCheckScenarios
import de.rki.coronawarnapp.ccl.dccadmission.model.storage.DccAdmissionCheckScenariosRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.ccl.ui.text.CCLTextFormatter
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AdmissionScenariosViewModel @AssistedInject constructor(
    private val format: CCLTextFormatter,
    private val admissionCheckScenariosRepository: DccAdmissionCheckScenariosRepository,
    @Assisted private val admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel,
    private val cclSettings: CCLSettings,
    private val dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger
) : CWAViewModel() {

    private val _calculationState = MutableLiveData<CalculationState>()
    val state = admissionScenariosSharedViewModel.admissionScenarios.map { it.toScenarioItems() }.asLiveData2()
    val calculationState: LiveData<CalculationState> = _calculationState

    private suspend fun DccAdmissionCheckScenarios.toScenarioItems() = State(
        title = format(scenarioSelection.titleText),
        scenarios = scenarioSelection.items.map { scenario ->
            AdmissionItemCard.Item(
                identifier = scenario.identifier,
                title = format(scenario.titleText),
                subtitle = format(scenario.subtitleText),
                enabled = scenario.enabled
            ) {
                selectScenario(scenario.identifier)
            }
        }
    )

    private fun selectScenario(admissionScenarioId: String) = launch {
        runCatching {
            _calculationState.postValue(Calculating)
            dccWalletInfoUpdateTrigger.triggerDccWalletInfoUpdateAfterCertificateChange()
        }.onFailure {
            _calculationState.postValue(CalculationError(it))
        }.onSuccess {
            admissionCheckScenariosRepository.save(admissionScenariosSharedViewModel.admissionScenarios.first())
            cclSettings.setAdmissionScenarioId(admissionScenarioId)
            _calculationState.postValue(CalculationDone)
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
    data class CalculationError(val error: Throwable) : CalculationState
}
