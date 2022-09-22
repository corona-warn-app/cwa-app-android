package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.model.AdmissionScenarios
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class AdmissionScenariosSharedViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val currentAdmissionScenarios = MutableStateFlow(savedAdmissionScenarios)

    val admissionScenarios: Flow<AdmissionScenarios> = currentAdmissionScenarios.filterNotNull()

    init {
        admissionScenarios
            .onEach { savedAdmissionScenarios = it }
            .catch {
                Timber.tag(TAG).e(it, "Failed to save savedAdmissionCheckScenarios")
            }
            .launchIn(viewModelScope)
    }

    suspend fun setAdmissionScenarios(admissionScenarios: AdmissionScenarios) =
        currentAdmissionScenarios.emit(admissionScenarios)

    private var savedAdmissionScenarios: AdmissionScenarios?
        get() = savedState[ADMISSION_SCENARIOS]
        set(value) {
            Timber.tag(TAG).v("Saving %s into savedStateHandle", value)
            savedState[ADMISSION_SCENARIOS] = value
        }

    companion object {
        private val TAG = tag<AdmissionScenariosSharedViewModel>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val ADMISSION_SCENARIOS = "admission_scenarios_key"
    }
}
