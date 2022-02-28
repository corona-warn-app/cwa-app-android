package de.rki.coronawarnapp.dccreissuance.ui.consent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber

class DccReissuanceConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    personCertificatesProvider: PersonCertificatesProvider,
    private val dccQrCodeExtractor: DccQrCodeExtractor,
    private val format: CclTextFormatter,
    @Assisted private val personIdentifierCode: String,
    private val personCertificatesSettings: PersonCertificatesSettings,
) : CWAViewModel(dispatcherProvider) {

    internal val certificateLiveData: MutableLiveData<DccV1.MetaData> = MutableLiveData()

    internal val event = SingleLiveEvent<Event>()

    internal val stateLiveData: LiveData<State> =
        personCertificatesProvider.personCertificates.map { certificates ->
            val person = certificates.find { it.personIdentifier?.codeSHA256 == personIdentifierCode }
            person?.personIdentifier?.let {
                personCertificatesSettings.dismissReissuanceBadge(it)
            }
            person?.dccWalletInfo?.certificateReissuance
        }.map { certificateReissuance ->
            certificateReissuance.toState()
        }.catch {
            Timber.tag(TAG).d(it, "dccReissuanceData failed")
        }.asLiveData2()

    internal fun startReissuance() {
        event.postValue(ReissuanceInProgress)
        // call api
        // replace certificate
        // navigate
    }

    private suspend fun CertificateReissuance?.toState(): State {
        var certificate: DccV1.MetaData? = null
        this?.certificateToReissue?.certificateRef?.barcodeData?.let {
            certificate = dccQrCodeExtractor.extract(
                it,
                DccV1Parser.Mode.CERT_SINGLE_STRICT
            ).data.certificate
        }
        return State(
            certificate = certificate,
            divisionVisible = this?.reissuanceDivision?.visible ?: false,
            title = format(this?.reissuanceDivision?.titleText),
            subtitle = format(this?.reissuanceDivision?.subtitleText),
            content = format(this?.reissuanceDivision?.longText),
            url = this?.reissuanceDivision?.faqAnchor
        )
    }

    internal data class State(
        val certificate: DccV1.MetaData?,
        val divisionVisible: Boolean = false,
        val title: String?,
        val subtitle: String?,
        val content: String?,
        val url: String?,
    )

    internal sealed class Event
    internal object ReissuanceInProgress : Event()
    internal object ReissuanceSuccess : Event()
    internal object ReissuanceError : Event()

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccReissuanceConsentViewModel> {
        fun create(
            personIdentifierCode: String
        ): DccReissuanceConsentViewModel
    }

    companion object {
        private val TAG = tag<DccReissuanceConsentViewModel>()
    }
}
