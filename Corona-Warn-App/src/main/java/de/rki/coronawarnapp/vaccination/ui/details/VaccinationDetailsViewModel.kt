package de.rki.coronawarnapp.vaccination.ui.details

import android.graphics.Bitmap
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class VaccinationDetailsViewModel @AssistedInject constructor(
    @Assisted private val vaccinationCertificateId: String,
    private val qrCodeGenerator: QrCodeGenerator,
    vaccinationRepository: VaccinationRepository,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    private var qrCodeText: String? = null
    private val mutableStateFlow = MutableStateFlow<Bitmap?>(null)
    val qrCode = mutableStateFlow.asLiveData(dispatcherProvider.Default)

    val vaccinationCertificate = vaccinationRepository.vaccinationInfos
        .map { persons ->
            val findVaccinationDetails = findVaccinationDetails(persons)
            generateQrCode(findVaccinationDetails.certificate)
            findVaccinationDetails
        }
        .asLiveData(context = dispatcherProvider.Default)

    val errors = SingleLiveEvent<Throwable>()
    val events = SingleLiveEvent<VaccinationDetailsNavigation>()

    fun onClose() = events.postValue(VaccinationDetailsNavigation.Back)

    fun openFullScreen() = qrCodeText?.let { events.postValue(VaccinationDetailsNavigation.FullQrCode(it)) }

    private fun findVaccinationDetails(
        vaccinatedPersons: Set<VaccinatedPerson>
    ): VaccinationDetails {
        val person = vaccinatedPersons.find { p ->
            p.vaccinationCertificates.any { it.certificateId == vaccinationCertificateId }
        }

        val certificate = person?.vaccinationCertificates?.find { it.certificateId == vaccinationCertificateId }
        return VaccinationDetails(
            certificate = certificate,
            isComplete = person?.getVaccinationStatus() == VaccinatedPerson.Status.COMPLETE,
        )
    }

    private fun generateQrCode(certificate: VaccinationCertificate?) = launch {
        try {
            mutableStateFlow.value = certificate?.let {
                qrCodeText = it.certificateId
                qrCodeGenerator.createQrCode(certificate.certificateId)
            }
        } catch (e: Exception) {
            Timber.d(e, "generateQrCode failed for vaccinationCertificate=%s", certificate)
            mutableStateFlow.value = null
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationDetailsViewModel> {
        fun create(
            certificateId: String,
        ): VaccinationDetailsViewModel
    }
}

data class VaccinationDetails(
    val certificate: VaccinationCertificate?,
    val isComplete: Boolean = false
)
