package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.toCertificateSortOrder
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import java.lang.StringBuilder

class DccExportAllOverviewViewModel @AssistedInject constructor(
    dispatcher: DispatcherProvider,
    personCertificatesProvider: PersonCertificatesProvider,
    template: CertificateTemplate,
) : CWAViewModel(dispatcher) {

    val dccData = personCertificatesProvider.personCertificates.map { persons ->
        val certSvgList = persons.flatMap { it.certificates }
            .sortedBy { cert -> cert.fullNameFormatted }
            .toCertificateSortOrder()
            .map { template.of(it).injectData(it) }

        val certs = StringBuilder().apply {
            certSvgList.forEach { svg -> append("<li>$svg</li>\n") }
        }.toString()

        HTML_TEMPLATE.replace(" ++certificates++", certs)
    }.asLiveData2()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccExportAllOverviewViewModel>
}
