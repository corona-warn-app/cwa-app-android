package de.rki.coronawarnapp.vaccination.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.first

class VaccinationListViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    @Assisted private val vaccinatedPersonIdentifier: String,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel() {

    private val _uiState: MutableLiveData<UiState> = MutableLiveData()
    val uiState: LiveData<UiState> = _uiState

    init {
        // TODO: load real values from repository
        launch {
            val person = when (vaccinatedPersonIdentifier) {
                "vaccinated-person-incomplete" -> vaccinationRepository.vaccinationInfos.first().first()
                "vaccinated-person-complete" -> vaccinationRepository.vaccinationInfos.first().elementAt(1)
                else -> throw IllegalArgumentException()
            }

            _uiState.postValue(
                UiState(
                    // TODO: fullName = "${person.firstName} ${person.lastName}",
                    fullName = "Andrea Schneider",
                    dayOfBirth = person.dateOfBirth.toDayFormat(),
                    vaccinations = person.vaccinationCertificates.toVaccinationUiDataList(),
                    vaccinationStatus = person.vaccinationStatus,
                    isLoading = person.isRefreshing
                )
            )
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            vaccinatedPersonIdentifier: String
        ): VaccinationListViewModel
    }

    data class UiState(
        val fullName: String,
        val dayOfBirth: String,
        val vaccinations: List<VaccinationUiData>,
        val vaccinationStatus: VaccinatedPerson.Status,
        val isLoading: Boolean
    ) {

        data class VaccinationUiData(
            val doseNumber: String,
            val totalSeriesOfDoses: String,
            val vaccinatedAt: String
        )
    }

    private fun VaccinationCertificate.toVaccinationUiData(): UiState.VaccinationUiData {
        return UiState.VaccinationUiData(
            // TODO: map from real model value
            doseNumber = "1",
            totalSeriesOfDoses = "2",
            vaccinatedAt = vaccinatedAt.toDayFormat()
        )
    }

    private fun Collection<VaccinationCertificate>.toVaccinationUiDataList(): List<UiState.VaccinationUiData> {
        return map { it.toVaccinationUiData() }
    }
}
