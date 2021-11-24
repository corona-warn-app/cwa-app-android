package de.rki.coronawarnapp.dccticketing.ui.consent.two

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.consent.two.items.CertificateItem
import de.rki.coronawarnapp.dccticketing.ui.consent.two.items.RecoveryCertificateCard
import de.rki.coronawarnapp.dccticketing.ui.consent.two.items.TestCertificateCard
import de.rki.coronawarnapp.dccticketing.ui.consent.two.items.VaccinationCertificateCard
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first

class DccTicketingConsentTwoViewModel @AssistedInject constructor(
    @Assisted private val dccTicketingTransactionContext: DccTicketingTransactionContext,
    @Assisted private val containerId: CertificateContainerId,
    private val certificateProvider: CertificateProvider,
    private val vaccinationRepository: VaccinationRepository,
    private val timeStamper: TimeStamper,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val showCloseDialog = SingleLiveEvent<Unit>()
    private val mutableUiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState>
        get() = mutableUiState

    init {
        launch {
            findCertificate().also {
                mutableUiState.postValue(
                    UiState(
                        dccTicketingTransactionContext = dccTicketingTransactionContext,
                        certificateItem = it
                    )
                )
            }
        }
    }

    private suspend fun findCertificate(): CertificateItem {
        return when (val certificate = certificateProvider.findCertificate(containerId)) {
            is TestCertificate -> TestCertificateCard.Item(certificate)
            is VaccinationCertificate -> {
                val status = vaccinatedPerson(certificate)?.getVaccinationStatus(timeStamper.nowUTC)
                    ?: VaccinatedPerson.Status.INCOMPLETE
                VaccinationCertificateCard.Item(
                    certificate = certificate,
                    status = status
                )
            }
            is RecoveryCertificate -> RecoveryCertificateCard.Item(certificate)
            else -> throw IllegalArgumentException("Certificate $certificate is not supported")
        }
    }

    private suspend fun vaccinatedPerson(certificate: CwaCovidCertificate): VaccinatedPerson? =
        vaccinationRepository.vaccinationInfos.first().find { it.identifier == certificate.personIdentifier }

    fun goBack() {
        showCloseDialog.postValue(Unit)
    }

    data class UiState(
        val dccTicketingTransactionContext: DccTicketingTransactionContext,
        val certificateItem: CertificateItem
    ) {
        val testPartner get() = "TBD (see allowlist PR)"
        val provider get() = dccTicketingTransactionContext.initializationData.serviceProvider
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingConsentTwoViewModel> {
        fun create(
            dccTicketingTransactionContext: DccTicketingTransactionContext,
            containerId: CertificateContainerId
        ): DccTicketingConsentTwoViewModel
    }
}
