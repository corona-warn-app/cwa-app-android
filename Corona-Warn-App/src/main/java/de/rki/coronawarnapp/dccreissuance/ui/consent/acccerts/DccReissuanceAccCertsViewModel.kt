package de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.expiration.isExpired
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceCertificateCard
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceItem
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber

class DccReissuanceAccCertsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    personCertificatesProvider: PersonCertificatesProvider,
    @Assisted private val personIdentifierCode: String,
    private val dccQrCodeExtractor: DccQrCodeExtractor,
    private val timeStamper: TimeStamper,
) : CWAViewModel(dispatcherProvider) {

    internal val certificatesLiveData: LiveData<List<DccReissuanceItem>> = personCertificatesProvider
        .findPersonByIdentifierCode(personIdentifierCode)
        .map { person ->
            person?.dccWalletInfo?.certificateReissuance?.asCertificateReissuanceCompat()?.toItemList().orEmpty()
        }.asLiveData2()

    private suspend fun CertificateReissuance.toItemList(): List<DccReissuanceItem> {
        return consolidateAccompanyingCertificates().mapNotNull {
            try {
                dccQrCodeExtractor.extract(
                    it.certificateRef.barcodeData,
                    DccV1Parser.Mode.CERT_SINGLE_STRICT
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to extract certificate")
                null
            }
        }.sort().map {
            DccReissuanceCertificateCard.Item(
                it.data.certificate,
                it.data.isExpired(timeStamper.nowUTC)
            )
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccReissuanceAccCertsViewModel> {
        fun create(
            personIdentifierCode: String
        ): DccReissuanceAccCertsViewModel
    }
}
