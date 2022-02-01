package de.rki.coronawarnapp.covidcertificate.boosterinfodetails

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.text.textResource
import de.rki.coronawarnapp.ccl.dccwalletinfo.text.urlResource
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber

class BoosterInfoDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    personCertificatesProvider: PersonCertificatesProvider,
    private val vaccinationRepository: VaccinationRepository,
    @Assisted private val personIdentifierCode: String
) : CWAViewModel(dispatcherProvider) {

    private val uiStateFlow = personCertificatesProvider.personCertificates.mapNotNull { certificateSet ->
        UiState(
            certificateSet.first { it.personIdentifier?.codeSHA256 == personIdentifierCode }
                .dccWalletInfo!!.boosterNotification.also {
                it.identifier?.let { id ->
                    vaccinationRepository.acknowledgeBoosterRule(
                        personIdentifierCode = personIdentifierCode,
                        boosterIdentifier = id
                    )
                }
            }
        )
    }.catch { error ->
        // This should never happen due to checks on previous screen
        Timber.d(error, "No person found for $personIdentifierCode")
    }
    val uiState = uiStateFlow.asLiveData2()

    data class UiState(
        val boosterNotification: BoosterNotification
    ) {
        val titleText by textResource(boosterNotification.titleText)
        val subtitleText by textResource(boosterNotification.subtitleText)
        val longText by textResource(boosterNotification.longText)
        val faqUrl by urlResource(boosterNotification.faqAnchor)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<BoosterInfoDetailsViewModel> {
        fun create(
            personIdentifierCode: String
        ): BoosterInfoDetailsViewModel
    }
}
