package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.person.core.toCertificateSortOrder
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class DccExportAllOverviewViewModel @AssistedInject constructor(
    dispatcher: DispatcherProvider,
    personCertificatesProvider: CertificateProvider,
    template: CertificateTemplate,
) : CWAViewModel(dispatcher) {

    val dccData = personCertificatesProvider.certificateContainer.map { container ->
        HTML_TEMPLATE.replace(
            oldValue = "++certificates++",
            newValue = container.allCwaCertificates
                .sortedBy { cert -> cert.fullNameFormatted }
                .toCertificateSortOrder()
                .joinToString(separator = "\n") { cert ->
                    "<li>${template(cert).inject(cert)}</li>"
                }
        )
    }.asLiveData2()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccExportAllOverviewViewModel>
}
