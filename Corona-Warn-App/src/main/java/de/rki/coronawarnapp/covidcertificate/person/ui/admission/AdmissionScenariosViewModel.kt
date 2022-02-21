package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccadmission.storage.DccAdmissionCheckScenariosRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AdmissionScenariosViewModel @AssistedInject constructor(
    format: CclTextFormatter,
    private val admissionCheckScenariosRepository: DccAdmissionCheckScenariosRepository,
    @Assisted private val admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel,
    private val cclSettings: CclSettings,
    private val dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger
) : CWAViewModel() {

    val state = admissionScenariosSharedViewModel.admissionScenarios
        .map { dccCheckScenarios ->
            State(
                title = format(dccCheckScenarios.scenarioSelection.titleText),
                scenarios = dccCheckScenarios.scenarioSelection.items.map { scenario ->
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
        }
        .asLiveData2()

    private fun selectScenario(admissionScenarioId: String) = launch {
        dccWalletInfoUpdateTrigger.triggerNow(admissionScenarioId)
        admissionCheckScenariosRepository.save(
            admissionScenariosSharedViewModel.admissionScenarios.first()
        )

        cclSettings.setAdmissionScenarioId(admissionScenarioId)
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
}
