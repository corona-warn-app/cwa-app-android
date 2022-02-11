package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccadmission.model.storage.DccAdmissionCheckScenariosRepository
import de.rki.coronawarnapp.ccl.ui.text.CCLTextFormatter
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class AdmissionScenariosViewModel @AssistedInject constructor(
    admissionCheckScenariosRepository: DccAdmissionCheckScenariosRepository,
    format: CCLTextFormatter
) : CWAViewModel() {

    val admissionCheckScenarios = admissionCheckScenariosRepository.admissionCheckScenarios
        .map {

        }
        .asLiveData2()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<AdmissionScenariosViewModel>
}
