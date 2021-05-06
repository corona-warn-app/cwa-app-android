package de.rki.coronawarnapp.vaccination.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListCertificateCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListIncompleteTopCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListVaccinationCardItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.joda.time.Days

class VaccinationListViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    private val timeStamper: TimeStamper,
    @Assisted private val vaccinatedPersonIdentifier: String
) : CWAViewModel() {

    val uiState: LiveData<UiState> = vaccinationRepository.vaccinationInfos.map { vaccinatedPersonSet ->
        val vaccinatedPerson = vaccinatedPersonSet.single { it.identifier.code == vaccinatedPersonIdentifier }
        val isVaccinationComplete = vaccinatedPerson.vaccinationStatus == COMPLETE
        val listItems = assembleItemList(isVaccinationComplete, vaccinatedPerson)
        UiState(listItems, vaccinationStatus = vaccinatedPerson.vaccinationStatus)
    }.catch {
        // TODO Error Handling
    }.asLiveData()

    private fun assembleItemList(
        isVaccinationComplete: Boolean,
        vaccinatedPerson: VaccinatedPerson
    ) = mutableListOf<VaccinationListItem>().apply {
        if (isVaccinationComplete) {
            if (vaccinatedPerson.proofCertificates.isNotEmpty()) {

                val proofCertificate = vaccinatedPerson.proofCertificates.first()
                val expiresAt = proofCertificate.expiresAt.toLocalDateUserTz()
                val today = timeStamper.nowUTC.toLocalDateUserTz()
                val remainingValidityInDays = Days.daysBetween(today, expiresAt).days

                add(
                    VaccinationListCertificateCardItem(
                        qrCode = null, // TODO: Generate QR-code
                        remainingValidityInDays = remainingValidityInDays
                    )
                )
            }
        } else {
            add(VaccinationListIncompleteTopCardItem)
        }
        add(
            VaccinationListNameCardItem(
                fullName = "${vaccinatedPerson.firstName} ${vaccinatedPerson.lastName}",
                dayOfBirth = vaccinatedPerson.dateOfBirth.toDayFormat()
            )
        )
        vaccinatedPerson.vaccinationCertificates.forEach { vaccinationCertificate ->
            add(
                VaccinationListVaccinationCardItem(
                    vaccinationCertificateId = vaccinationCertificate.certificateId,
                    doseNumber = vaccinationCertificate.doseNumber.toString(),
                    totalSeriesOfDoses = vaccinationCertificate.totalSeriesOfDoses.toString(),
                    vaccinatedAt = vaccinationCertificate.vaccinatedAt.toDayFormat(),
                    vaccinationStatus = vaccinatedPerson.vaccinationStatus,
                    isFinalVaccination =
                    vaccinationCertificate.doseNumber == vaccinationCertificate.totalSeriesOfDoses
                )
            )
        }
    }.toList()

    data class UiState(
        val listItems: List<VaccinationListItem>,
        val vaccinationStatus: VaccinatedPerson.Status
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            vaccinatedPersonIdentifier: String
        ): VaccinationListViewModel
    }
}
