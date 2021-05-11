package de.rki.coronawarnapp.vaccination.ui.list

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.ProofCertificate
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.INCOMPLETE
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListCertificateCardItemVH.VaccinationListCertificateCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListIncompleteTopCardItemVH.VaccinationListIncompleteTopCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH.VaccinationListVaccinationCardItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import org.joda.time.Days

class VaccinationListViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    private val timeStamper: TimeStamper,
    private val qrCodeGenerator: QrCodeGenerator,
    @Assisted private val vaccinatedPersonIdentifier: String
) : CWAViewModel() {

    val events = SingleLiveEvent<Event>()

    val vaccinationInfoFlow = vaccinationRepository.vaccinationInfos.map { vaccinatedPersonSet ->
        // val vaccinatedPerson = vaccinatedPersonSet.single { it.identifier.code == vaccinatedPersonIdentifier }
        vaccinatedPersonSet.first()
    }

    private val proofQrCode: Flow<Bitmap?> = vaccinationRepository.vaccinationInfos.transform { vaccinationInfos ->

        emit(null)

        // TODO: use actual values from repository instead of these mocked ones once we can retrieve proof Certificates
        val proofCertificates = setOf(
            getMockProofCertificate()
        )

        if (proofCertificates.isNotEmpty()) {
            emit(qrCodeGenerator.createQrCode("TODO create qrCode from actual value"))
        }
    }

    private val vaccinationStatusFlow = MutableStateFlow(INCOMPLETE)

    val uiState: LiveData<UiState> = combine(
        vaccinationInfoFlow,
        proofQrCode,
        vaccinationStatusFlow
    ) { vaccinatedPerson, proofQrCode, vaccinationStatus ->

        val proofCertificates = setOf(
            getMockProofCertificate()
        )

        val listItems = assembleItemList(
            vaccinatedPerson = vaccinatedPerson,
            vaccinationStatus = vaccinationStatus,
            proofCertificates = proofCertificates,
            proofQrCode = proofQrCode
        )

        UiState(
            listItems,
            vaccinationStatus = vaccinationStatus
        )
    }.catch {
        // TODO Error Handling in an upcoming subtask
    }.asLiveData()

    private fun assembleItemList(
        vaccinatedPerson: VaccinatedPerson,
        vaccinationStatus: VaccinatedPerson.Status,
        proofCertificates: Set<ProofCertificate>,
        proofQrCode: Bitmap?
    ) = mutableListOf<VaccinationListItem>().apply {
        if (vaccinationStatus == COMPLETE) {
            if (proofCertificates.isNotEmpty()) {

                val proofCertificate = proofCertificates.first()
                val expiresAt = proofCertificate.expiresAt.toLocalDateUserTz()
                val today = timeStamper.nowUTC.toLocalDateUserTz()
                val remainingValidityInDays = Days.daysBetween(today, expiresAt).days

                add(
                    VaccinationListCertificateCardItem(
                        qrCode = proofQrCode,
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
            with(vaccinationCertificate) {
                add(
                    VaccinationListVaccinationCardItem(
                        vaccinationCertificateId = certificateId,
                        doseNumber = doseNumber.toString(),
                        totalSeriesOfDoses = totalSeriesOfDoses.toString(),
                        vaccinatedAt = vaccinatedAt.toDayFormat(),
                        vaccinationStatus = vaccinationStatus,
                        isFinalVaccination = doseNumber == totalSeriesOfDoses,
                        onCardClick = { certificateId ->
                            events.postValue(Event.NavigateToVaccinationCertificateDetails(certificateId))
                        }
                    )
                )
            }
        }
    }.toList()

    fun onRegisterNewVaccinationClick() {
        events.postValue(Event.NavigateToVaccinationQrCodeScanScreen)
    }

    fun onRefreshClick() {
        if (vaccinationStatusFlow.value == COMPLETE) {
            vaccinationStatusFlow.value = INCOMPLETE
        } else {
            vaccinationStatusFlow.value = COMPLETE
        }
    }

    data class UiState(
        val listItems: List<VaccinationListItem>,
        val vaccinationStatus: VaccinatedPerson.Status
    )

    sealed class Event {
        data class NavigateToVaccinationCertificateDetails(val vaccinationCertificateId: String) : Event()
        object NavigateToVaccinationQrCodeScanScreen : Event()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            vaccinatedPersonIdentifier: String
        ): VaccinationListViewModel
    }
}
