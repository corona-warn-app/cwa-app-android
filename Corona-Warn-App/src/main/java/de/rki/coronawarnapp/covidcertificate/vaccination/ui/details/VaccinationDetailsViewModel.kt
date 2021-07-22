package de.rki.coronawarnapp.covidcertificate.vaccination.ui.details

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class VaccinationDetailsViewModel @AssistedInject constructor(
    @Assisted private val containerId: VaccinationCertificateContainerId,
    private val vaccinationRepository: VaccinationRepository,
    private val dccValidationRepository: DccValidationRepository,
    private val certificateProvider: CertificateProvider,
    @AppScope private val appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    private var qrCodeText: String? = null

    val vaccinationCertificate = vaccinationRepository.vaccinationInfos
        .map { persons ->
            val findVaccinationDetails = findVaccinationDetails(persons)
            qrCodeText = findVaccinationDetails.certificate?.qrCode
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
            p.vaccinationContainers.any { it.containerId == containerId }
        }

        val certificate = person?.vaccinationCertificates?.find { it.containerId == containerId }
        return VaccinationDetails(
            certificate = certificate,
            isImmune = person?.getVaccinationStatus() == VaccinatedPerson.Status.IMMUNITY,
        )
    }

    fun getCovidCertificate(): CwaCovidCertificate {
        return runBlocking {
            certificateProvider.findCertificate(containerId)
        }
    }

    fun onDeleteVaccinationCertificateConfirmed() {
        launch(scope = appScope) {
            try {
                vaccinationRepository.deleteCertificate(containerId)
                events.postValue(VaccinationDetailsNavigation.Back)
            } catch (exception: Exception) {
                errors.postValue(exception)
                Timber.e(exception, "Something went wrong when trying to delete a vaccination certificate.")
            }
        }
    }

    fun startValidationRulesDownload() = launch {
        try {
            dccValidationRepository.refresh()
            events.postValue(VaccinationDetailsNavigation.ValidationStart(containerId))
        } catch (e: Exception) {
            Timber.d(e, "validation rule download failed for covidCertificate=%s", containerId)
            errors.postValue(e)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationDetailsViewModel> {
        fun create(
            containerId: VaccinationCertificateContainerId,
        ): VaccinationDetailsViewModel
    }
}

data class VaccinationDetails(
    val certificate: VaccinationCertificate?,
    val isImmune: Boolean = false
)
