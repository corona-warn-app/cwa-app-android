package de.rki.coronawarnapp.ccl.dccadmission.model.storage

import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccadmission.model.DccAdmissionCheckScenarios
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DccAdmissionCheckScenariosRepository @Inject constructor(
    private val cclSettings: CCLSettings
) {

    val admissionCheckScenarios: Flow<DccAdmissionCheckScenarios?> = cclSettings.admissionCheckScenarios

    fun save(scenarios: DccAdmissionCheckScenarios) {
        cclSettings.setAdmissionCheckScenarios(scenarios)
    }

    fun clear() {
        cclSettings.setAdmissionCheckScenarios(null)
    }
}
