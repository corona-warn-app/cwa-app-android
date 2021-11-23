package de.rki.coronawarnapp.dccticketing.ui.consent.two

import androidx.lifecycle.asLiveData
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class DccTicketingConsentTwoViewModel @AssistedInject constructor(
    @Assisted private val dccTicketingTransactionContext: DccTicketingTransactionContext,
    @Assisted private val containerId: CertificateContainerId,
    private val certificateProvider: CertificateProvider,
    private val vaccinationRepository: VaccinationRepository,
    private val timeStamper: TimeStamper,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val selectedCertificate = flow {
        emit(mutableListOf<CertificateItem>().addCardItem())
    }.asLiveData2()

    val showCloseDialog = SingleLiveEvent<Unit>()

    val uiState = MutableStateFlow(UiState(dccTicketingTransactionContext)).asLiveData()

    private suspend fun MutableList<CertificateItem>.addCardItem() {
        when (val certificate = certificateProvider.findCertificate(containerId)) {
            is TestCertificate -> add(TestCertificateCard.Item(certificate))
            is VaccinationCertificate -> {
                val status = vaccinatedPerson(certificate)?.getVaccinationStatus(timeStamper.nowUTC)
                    ?: VaccinatedPerson.Status.INCOMPLETE
                add(
                    VaccinationCertificateCard.Item(
                        certificate = certificate,
                        status = status
                    )
                )
            }
            is RecoveryCertificate -> add(RecoveryCertificateCard.Item(certificate))
        }
    }

    private suspend fun vaccinatedPerson(certificate: CwaCovidCertificate): VaccinatedPerson? =
        vaccinationRepository.vaccinationInfos.first().find { it.identifier == certificate.personIdentifier }

    fun goBack() {
        showCloseDialog.postValue(Unit)
    }

    data class UiState(
        val dccTicketingTransactionContext: DccTicketingTransactionContext
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
