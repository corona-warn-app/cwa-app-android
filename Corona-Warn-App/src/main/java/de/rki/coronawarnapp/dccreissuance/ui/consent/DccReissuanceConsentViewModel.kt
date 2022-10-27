package de.rki.coronawarnapp.dccreissuance.ui.consent

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.expiration.isExpired
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.dccreissuance.core.reissuer.DccReissuer
import de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts.sort
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class DccReissuanceConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    personCertificatesProvider: PersonCertificatesProvider,
    @Assisted private val groupKey: String,
    private val dccReissuer: DccReissuer,
    private val format: CclTextFormatter,
    private val dccQrCodeExtractor: DccQrCodeExtractor,
    private val personCertificatesSettings: PersonCertificatesSettings,
    private val timeStamper: TimeStamper,
) : CWAViewModel(dispatcherProvider) {

    internal val event = SingleLiveEvent<Event>()

    private val reissuanceData = personCertificatesProvider.findPersonByIdentifierCode(groupKey)
        .distinctUntilChangedBy { it?.dccWalletInfo?.certificateReissuance }
        .map { person ->
            person?.personIdentifier?.let { personCertificatesSettings.dismissReissuanceBadge(it) }
            person?.dccWalletInfo?.certificateReissuance?.asCertificateReissuanceCompat()
        }
    internal val stateLiveData: LiveData<State> = reissuanceData.map {
        // Make sure DccReissuance exists, otherwise screen is dismissed
        it!!.toState()
    }.catch {
        Timber.tag(TAG).d(it, "dccReissuanceData failed")
        if (event.value !in listOf(ReissuanceInProgress, ReissuanceSuccess)) // only if not in progress
            event.postValue(Back) // Fallback in case reissuance is removed
    }.asLiveData2()

    internal fun startReissuance() = launch {
        runCatching {
            event.postValue(ReissuanceInProgress)
            dccReissuer.startReissuance(reissuanceData.first()!!)
        }.onFailure { e ->
            Timber.d(e, "startReissuance() failed")
            event.postValue(ReissuanceError(e))
        }.onSuccess {
            Timber.d("startReissuance() succeeded")
            event.postValue(ReissuanceSuccess)
        }
    }

    fun navigateBack() = event.postValue(Back)

    private suspend fun CertificateReissuance.toState(): State {
        val certificates = certificates?.mapNotNull {
            try {
                dccQrCodeExtractor.extract(
                    it.certificateToReissue.certificateRef.barcodeData,
                    DccV1Parser.Mode.CERT_SINGLE_STRICT
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to extract certificate")
                null
            }
        }.orEmpty().sort().map {
            DccReissuanceCertificateCard.Item(
                it.data.certificate,
                it.data.isExpired(timeStamper.nowUTC)
            )
        }

        val accompanyingCertificatesVisible = consolidateAccompanyingCertificates().isNotEmpty()

        return State(
            certificateList = certificates,
            accompanyingCertificatesVisible = accompanyingCertificatesVisible,
            divisionVisible = reissuanceDivision.visible,
            listItemsTitle = format(reissuanceDivision.listTitleText),
            title = format(reissuanceDivision.titleText),
            subtitle = format(reissuanceDivision.consentSubtitleText),
            content = format(reissuanceDivision.longText),
            url = format(reissuanceDivision.faqAnchor)
        )
    }

    fun openPrivacyScreen() = event.postValue(OpenPrivacyScreen)

    fun openAccompanyingCertificatesScreen() = event.postValue(OpenAccompanyingCertificatesScreen)

    internal data class State(
        val certificateList: List<DccReissuanceItem>,
        val accompanyingCertificatesVisible: Boolean,
        val divisionVisible: Boolean,
        val listItemsTitle: String?,
        val title: String?,
        val subtitle: String?,
        val content: String?,
        val url: String?,
    )

    internal sealed class Event
    internal object ReissuanceInProgress : Event()
    internal object ReissuanceSuccess : Event()
    internal object Back : Event()
    internal object OpenPrivacyScreen : Event()
    internal object OpenAccompanyingCertificatesScreen : Event()
    internal data class ReissuanceError(val error: Throwable) : Event()

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccReissuanceConsentViewModel> {
        fun create(
            groupKey: String
        ): DccReissuanceConsentViewModel
    }

    companion object {
        private val TAG = tag<DccReissuanceConsentViewModel>()
    }
}
