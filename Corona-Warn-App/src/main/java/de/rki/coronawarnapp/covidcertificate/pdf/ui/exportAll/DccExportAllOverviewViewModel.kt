package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateExportCache
import de.rki.coronawarnapp.covidcertificate.person.core.toCertificateSortOrder
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.File

class DccExportAllOverviewViewModel @AssistedInject constructor(
    private val dispatcher: DispatcherProvider,
    personCertificatesProvider: CertificateProvider,
    template: CertificateTemplate,
    @CertificateExportCache private val path: File,
    private val fileSharing: FileSharing
) : CWAViewModel(dispatcher) {

    private val error = SingleLiveEvent<Throwable>()
    val result = SingleLiveEvent<FileSharing.FileIntentProvider>()


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
    }.catch {
        error.postValue(it)
    }.asLiveData2()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccExportAllOverviewViewModel>

    companion object {
        private const val FILE_NAME = "certificates.pdf"
    }
}
