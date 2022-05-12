package de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceConsentCard
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceItem
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
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
    private val personCertificatesSettings: PersonCertificatesSettings,
) : CWAViewModel(dispatcherProvider) {

    private val reissuanceData = personCertificatesProvider.findPersonByIdentifierCode(personIdentifierCode)
        .map { person ->
            person?.personIdentifier?.let { personCertificatesSettings.dismissReissuanceBadge(it) }
            person?.dccWalletInfo?.certificateReissuance?.migrateLegacyCertificate()
        }

    internal val certificatesLiveData: LiveData<List<DccReissuanceItem>> = reissuanceData.map {
        it?.toList() ?: emptyList()
    }.asLiveData2()

    private suspend fun CertificateReissuance.toList(): List<DccReissuanceItem> {
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
        }.sortedByDescending {
            when (it) {
                is TestCertificateQRCode -> it.data.certificate.test.sampleCollectedAt?.toLocalDateUserTz()
                is VaccinationCertificateQRCode -> it.data.certificate.vaccination.vaccinatedOn
                is RecoveryCertificateQRCode -> it.data.certificate.recovery.testedPositiveOn
                else -> null
            }
        }.map {
            DccReissuanceConsentCard.Item(it.data.certificate)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccReissuanceAccCertsViewModel> {
        fun create(
            personIdentifierCode: String
        ): DccReissuanceAccCertsViewModel
    }
}
