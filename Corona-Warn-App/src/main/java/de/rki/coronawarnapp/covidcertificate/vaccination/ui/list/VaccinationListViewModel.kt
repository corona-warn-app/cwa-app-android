package de.rki.coronawarnapp.covidcertificate.vaccination.ui.list

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListImmunityInformationCardItemVH.VaccinationListImmunityInformationCardItem
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH.VaccinationListNameCardItem
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListQrCodeCardItemVH.VaccinationListQrCodeCardItem
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH.VaccinationListVaccinationCardItem
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import timber.log.Timber

class VaccinationListViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    valueSetsRepository: ValueSetsRepository,
    @AppContext context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val qrCodeGenerator: QrCodeGenerator,
    @Assisted private val personIdentifierCodeSha256: String
) : CWAViewModel() {

    init {
        valueSetsRepository.triggerUpdateValueSet(languageCode = context.getLocale())
    }

    val events = SingleLiveEvent<Event>()
    val errors = SingleLiveEvent<Throwable>()

    private val vaccinatedPersonFlow = vaccinationRepository.vaccinationInfos.map { vaccinatedPersonSet ->
        vaccinatedPersonSet.single { it.identifier.codeSHA256 == personIdentifierCodeSha256 }
    }

    private val vaccinationQrCodeFlow: Flow<Bitmap?> = vaccinatedPersonFlow.transform {
        // emit null initially, so that the UI can show the list with a loading indicator for the qrcode
        // immediately ...
        emit(null)
        // ... and after the QR code was generated, it is emitted
        emit(qrCodeGenerator.createQrCode(it.getMostRecentVaccinationCertificate.qrCode))
    }

    val uiState: LiveData<UiState> = combine(vaccinatedPersonFlow, vaccinationQrCodeFlow) { vaccinatedPerson, qrCode ->
        UiState(
            listItems = assembleItemList(vaccinatedPerson = vaccinatedPerson, qrCode),
            vaccinationStatus = vaccinatedPerson.getVaccinationStatus()
        )
    }.catch { exception ->
        when (exception) {
            is NoSuchElementException -> {
                Timber.d(exception, "Seems like all vaccination certificates got deleted. Navigate back ...")
                events.postValue(Event.NavigateBack)
            }
            else -> {
                Timber.e(exception, "Something unexpected went wrong... Let's navigate back...")
                events.postValue(Event.NavigateBack)
            }
        }
    }.asLiveData()

    private fun assembleItemList(vaccinatedPerson: VaccinatedPerson, qrCode: Bitmap?) =
        mutableListOf<VaccinationListItem>().apply {

            val vaccinationCertificate = vaccinatedPerson.getMostRecentVaccinationCertificate

            add(
                VaccinationListQrCodeCardItem(
                    qrCode = qrCode,
                    doseNumber = vaccinationCertificate.doseNumber,
                    totalSeriesOfDoses = vaccinationCertificate.totalSeriesOfDoses,
                    vaccinatedAtFormatted = vaccinatedPerson.getMostRecentVaccinationCertificate.vaccinatedAtFormatted,
                    expiresAt = vaccinatedPerson.getMostRecentVaccinationCertificate.expiresAt,
                    onQrCodeClick = {
                        events.postValue(
                            Event.NavigateToQrCodeFullScreen(
                                qrCode = vaccinatedPerson.getMostRecentVaccinationCertificate.qrCode,
                                positionInList = 0
                            )
                        )
                    }
                )
            )

            add(
                VaccinationListNameCardItem(
                    fullName = vaccinatedPerson.fullName,
                    dayOfBirth = vaccinatedPerson.dateOfBirthFormatted
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

            vaccinatedPerson.vaccinationCertificates.sortedBy { it.vaccinatedAtFormatted }
                .forEach { vaccinationCertificate ->
                    with(vaccinationCertificate) {
                        add(
                            VaccinationListVaccinationCardItem(
                                vaccinationCertificateId = certificateId,
                                doseNumber = doseNumber,
                                totalSeriesOfDoses = totalSeriesOfDoses,
                                vaccinatedAt = vaccinatedAtFormatted,
                                vaccinationStatus = vaccinatedPerson.getVaccinationStatus(),
                                isFinalVaccination = doseNumber == totalSeriesOfDoses,
                                onCardClick = { certificateId ->
                                    events.postValue(Event.NavigateToVaccinationCertificateDetails(certificateId))
                                },
                                onDeleteClick = { certificateId ->
                                    events.postValue(Event.DeleteVaccinationEvent(certificateId))
                                },
                                onSwipeToDelete = { certificateId, position ->
                                    events.postValue(Event.DeleteVaccinationEvent(certificateId, position))
                                }
                            )
                        )
                    }
                }
        }.toList()

    fun onRegisterNewVaccinationClick() {
        events.postValue(Event.NavigateToQrCodeScanScreen)
    }

    fun deleteVaccination(vaccinationCertificateId: String) {
        launch(scope = appScope) {
            try {
                vaccinationRepository.deleteVaccinationCertificate(vaccinationCertificateId)
            } catch (exception: Exception) {
                errors.postValue(exception)
                Timber.e(exception, "Something went wrong when trying to delete a vaccination certificate.")
            }
        }
    }

    data class UiState(
        val listItems: List<VaccinationListItem>,
        val vaccinationStatus: VaccinatedPerson.Status
    )

    sealed class Event {
        data class NavigateToVaccinationCertificateDetails(val vaccinationCertificateId: String) : Event()
        object NavigateToQrCodeScanScreen : Event()
        data class NavigateToQrCodeFullScreen(val qrCode: String, val positionInList: Int) : Event()
        data class DeleteVaccinationEvent(val vaccinationCertificateId: String, val position: Int? = null) : Event()
        object NavigateBack : Event()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            personIdentifierCode: String
        ): VaccinationListViewModel
    }
}
