package de.rki.coronawarnapp.vaccination.ui.list

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListImmunityInformationCardItemVH.VaccinationListImmunityInformationCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListQrCodeCardItemVH.VaccinationListQrCodeCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH.VaccinationListVaccinationCardItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

class VaccinationListViewModel @AssistedInject constructor(
    vaccinationRepository: VaccinationRepository,
    private val qrCodeGenerator: QrCodeGenerator,
    @Assisted private val personIdentifierCode: String
) : CWAViewModel() {

    val events = SingleLiveEvent<Event>()

    private val vaccinatedPersonFlow = vaccinationRepository.vaccinationInfos.map { vaccinatedPersonSet ->
        vaccinatedPersonSet.single { it.identifier.code == personIdentifierCode }
    }

    private val vaccinationQrCodeFlow: Flow<Bitmap?> = vaccinatedPersonFlow.transform {
        emit(null)

        // TODO: get qr code string from VaccinatedPerson
        val qrCode = qrCodeGenerator.createQrCode("TODO: get qr code String from VaccinatedPerson")
        emit(qrCode)
    }

    val uiState: LiveData<UiState> = combine(vaccinatedPersonFlow, vaccinationQrCodeFlow) { vaccinatedPerson, qrCode ->
        UiState(
            listItems = assembleItemList(vaccinatedPerson = vaccinatedPerson, qrCode),
            vaccinationStatus = vaccinatedPerson.getVaccinationStatus()
        )
    }.catch {
        // TODO Error Handling in an upcoming subtask
    }.asLiveData()

    private fun assembleItemList(vaccinatedPerson: VaccinatedPerson, qrCode: Bitmap?) =
        mutableListOf<VaccinationListItem>().apply {

            val vaccinationCertificate = vaccinatedPerson.getMostRecentVaccinationCertificate

            add(
                VaccinationListQrCodeCardItem(
                    qrCode = qrCode,
                    doseNumber = vaccinationCertificate.doseNumber,
                    totalSeriesOfDoses = vaccinationCertificate.totalSeriesOfDoses,
                    vaccinatedAt = vaccinatedPerson.getMostRecentVaccinationCertificate.vaccinatedAt,
                    expiresAt = vaccinatedPerson.getMostRecentVaccinationCertificate.expiresAt
                )
            )

            add(
                VaccinationListNameCardItem(
                    fullName = vaccinatedPerson.fullName,
                    dayOfBirth = vaccinatedPerson.dateOfBirth.toDayFormat()
                )
            )

            if (vaccinatedPerson.getVaccinationStatus() == VaccinatedPerson.Status.COMPLETE) {
                val timeUntilImmunity = vaccinatedPerson.getTimeUntilImmunity()
                if (timeUntilImmunity != null) {
                    add(
                        VaccinationListImmunityInformationCardItem(timeUntilImmunity)
                    )
                }
            }

            vaccinatedPerson.vaccinationCertificates.forEach { vaccinationCertificate ->
                with(vaccinationCertificate) {
                    add(
                        VaccinationListVaccinationCardItem(
                            vaccinationCertificateId = certificateId,
                            doseNumber = doseNumber.toString(),
                            totalSeriesOfDoses = totalSeriesOfDoses.toString(),
                            vaccinatedAt = vaccinatedAt.toDayFormat(),
                            vaccinationStatus = vaccinatedPerson.getVaccinationStatus(),
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
            personIdentifierCode: String
        ): VaccinationListViewModel
    }
}
