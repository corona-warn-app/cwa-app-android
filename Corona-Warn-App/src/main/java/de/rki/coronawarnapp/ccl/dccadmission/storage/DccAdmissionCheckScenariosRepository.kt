package de.rki.coronawarnapp.ccl.dccadmission.storage

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccadmission.model.DccAdmissionCheckScenarios
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
class DccAdmissionCheckScenariosRepository @Inject constructor(
    private val cclSettings: CCLSettings,
    @BaseJackson private val mapper: ObjectMapper
) {

    val admissionCheckScenarios: Flow<DccAdmissionCheckScenarios?> =
        cclSettings.admissionCheckScenarios.map {
            it?.let { json ->
                try {
                    mapper.readValue(json)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse admission check scenarios.")
                    null
                }
            } ?: run {
                Timber.v("No admission check scenarios available.")
                null
            }
        }

    suspend fun save(scenarios: DccAdmissionCheckScenarios) {
        val json = mapper.writeValueAsString(scenarios)
        cclSettings.setAdmissionCheckScenarios(json)
    }

    suspend fun clear() {
        cclSettings.setAdmissionCheckScenarios(null)
    }
}
