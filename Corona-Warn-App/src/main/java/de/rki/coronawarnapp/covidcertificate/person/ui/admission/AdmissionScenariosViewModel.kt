package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccadmission.model.storage.DccAdmissionCheckScenariosRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.ccl.ui.text.CCLTextFormatter
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AdmissionScenariosViewModel @AssistedInject constructor(
    format: CCLTextFormatter,
    private val admissionCheckScenariosRepository: DccAdmissionCheckScenariosRepository,
    @Assisted private val admissionSharedViewModel: AdmissionSharedViewModel,
    private val cclSettings: CCLSettings,
    private val dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger
) : CWAViewModel() {

    val admissionCheckScenarios = admissionSharedViewModel.admissionScenarios
        .map {
        }
        .asLiveData2()

    fun selectScenario(admissionScenarioId: String) = launch {
        dccWalletInfoUpdateTrigger.triggerDccWalletInfoUpdateAfterCertificateChange()
        admissionCheckScenariosRepository.save(
            admissionSharedViewModel.admissionScenarios.first()
        )

        cclSettings.setAdmissionScenarioId(admissionScenarioId)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<AdmissionScenariosViewModel> {
        fun create(
            admissionSharedViewModel: AdmissionSharedViewModel
        ): AdmissionScenariosViewModel
    }
}
