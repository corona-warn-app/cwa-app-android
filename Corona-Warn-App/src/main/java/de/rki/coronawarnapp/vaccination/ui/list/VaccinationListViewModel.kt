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
import de.rki.coronawarnapp.vaccination.core.ProofCertificate
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.INCOMPLETE
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListCertificateCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListIncompleteTopCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListVaccinationCardItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.joda.time.Days
import org.joda.time.LocalDate

class VaccinationListViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    private val timeStamper: TimeStamper,
    @Assisted private val vaccinatedPersonIdentifier: String
) : CWAViewModel() {

    val uiState: LiveData<UiState> = vaccinationRepository.vaccinationInfos.map { vaccinatedPersonSet ->

        // TODO: use the line below once the repository returns actual values
        // val vaccinatedPerson = vaccinatedPersonSet.single { it.identifier.code == vaccinatedPersonIdentifier }

        // For now, use mock data
        val vaccinationStatus = INCOMPLETE
        // val vaccinationStatus = COMPLETE

        val vaccinationCertificates = setOf(
            getMockVaccinationCertificate(),
            getMockVaccinationCertificate().copy(
                doseNumber = 2
            )
        )

        val proofCertificates = setOf(
            getMockProofCertificate()
        )

        val listItems = assembleItemList(
            vaccinationCertificates = vaccinationCertificates,
            proofCertificates = proofCertificates,
            firstName = "François-Joan",
            lastName = "d'Arsøns - van Halen",
            dateOfBirth = LocalDate.parse("2009-02-28"),
            vaccinationStatus
        )

        UiState(
            listItems,
            vaccinationStatus = vaccinationStatus,
            // TODO replace once isEligibleForProof is there
            isEligibleForProofCertificate = vaccinationStatus == COMPLETE
        )
    }.catch {
        // TODO Error Handling in an upcoming subtask
    }.asLiveData()

    // TODO: after using actual values from the repository, we only pass VaccinatedPerson here instead of all these
    // arguments
    @Suppress("LongParameterList")
    private fun assembleItemList(
        vaccinationCertificates: Set<VaccinationCertificate>,
        proofCertificates: Set<ProofCertificate>,
        firstName: String,
        lastName: String,
        dateOfBirth: LocalDate,
        vaccinationStatus: VaccinatedPerson.Status
    ) = mutableListOf<VaccinationListItem>().apply {
        if (vaccinationStatus == COMPLETE) {
            if (proofCertificates.isNotEmpty()) {

                val proofCertificate = proofCertificates.first()
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
                fullName = "$firstName $lastName",
                dayOfBirth = dateOfBirth.toDayFormat()
            )
        )
        vaccinationCertificates.forEach { vaccinationCertificate ->
            with(vaccinationCertificate) {
                add(
                    VaccinationListVaccinationCardItem(
                        vaccinationCertificateId = certificateId,
                        doseNumber = doseNumber.toString(),
                        totalSeriesOfDoses = totalSeriesOfDoses.toString(),
                        vaccinatedAt = vaccinatedAt.toDayFormat(),
                        vaccinationStatus = vaccinationStatus,
                        isFinalVaccination =
                            doseNumber == totalSeriesOfDoses,
                    )
                )
            }
        }
    }.toList()

    data class UiState(
        val listItems: List<VaccinationListItem>,
        val vaccinationStatus: VaccinatedPerson.Status,
        val isEligibleForProofCertificate: Boolean
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            vaccinatedPersonIdentifier: String
        ): VaccinationListViewModel
    }
}
