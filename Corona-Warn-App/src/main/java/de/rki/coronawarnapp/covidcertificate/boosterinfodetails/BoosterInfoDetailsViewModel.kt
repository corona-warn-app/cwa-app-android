package de.rki.coronawarnapp.covidcertificate.boosterinfodetails

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber

class BoosterInfoDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    personCertificatesProvider: PersonCertificatesProvider,
    @Assisted private val groupKey: String,
    private val format: CclTextFormatter,
    private val personCertificatesSettings: PersonCertificatesSettings,
) : CWAViewModel(dispatcherProvider) {

    val shouldClose = SingleLiveEvent<Unit>()

    private val uiStateFlow =
        personCertificatesProvider.findPersonByIdentifierCode(groupKey).map { person ->
            val boosterNotification = person!!.dccWalletInfo!!.boosterNotification
            boosterNotification.identifier?.let { id ->
                personCertificatesSettings.acknowledgeBoosterRule(
                    personIdentifier = person.personIdentifier,
                    boosterIdentifier = id
                )
            }

            UiState(
                titleText = format(boosterNotification.titleText),
                subtitleText = format(boosterNotification.subtitleText),
                longText = format(boosterNotification.longText),
                faqUrl = format(boosterNotification.faqAnchor)
            )
        }.catch { error ->
            // This should never happen due to checks on previous screen
            Timber.d(error, "No person found for $groupKey")
            shouldClose.postValue(Unit)
        }
    val uiState = uiStateFlow.asLiveData2()

    data class UiState(
        val titleText: String,
        val subtitleText: String,
        val longText: String,
        val faqUrl: String?,
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<BoosterInfoDetailsViewModel> {
        fun create(
            groupKey: String
        ): BoosterInfoDetailsViewModel
    }
}
