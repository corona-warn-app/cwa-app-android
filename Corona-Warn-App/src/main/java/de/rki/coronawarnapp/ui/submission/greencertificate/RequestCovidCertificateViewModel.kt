package de.rki.coronawarnapp.ui.submission.greencertificate

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.LocalDate

class RequestCovidCertificateViewModel @AssistedInject constructor(
    @Assisted private val coronaTestQrCode: CoronaTestQRCode,
    @Assisted private val coronaTestConsent: Boolean,
) : CWAViewModel() {

    fun birthDateChanged(localDate: LocalDate?) {
        // TODO
    }

    fun onAgreeGC() {
        // TODO
    }

    fun onDisagreeGC() {
        // TODO
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<RequestCovidCertificateViewModel> {
        fun create(
            coronaTestQrCode: CoronaTestQRCode,
            coronaTestConsent: Boolean
        ): RequestCovidCertificateViewModel
    }
}
