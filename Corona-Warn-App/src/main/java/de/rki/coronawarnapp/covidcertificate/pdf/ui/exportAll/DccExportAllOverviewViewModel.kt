package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import android.app.Activity
import android.print.PdfFile
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
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
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.time.Instant

class DccExportAllOverviewViewModel @AssistedInject constructor(
    private val dispatcher: DispatcherProvider,
    personCertificatesProvider: CertificateProvider,
    template: CertificateTemplate,
    @CertificateExportCache private val path: File,
    private val fileSharing: FileSharing
) : CWAViewModel(dispatcher) {

    private val error = SingleLiveEvent<Throwable>()
    val result = SingleLiveEvent<ExportResult>()

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

    fun print(adapter: PrintDocumentAdapter) =
        PrintResult { activity ->
            runCatching {
                val printManager = requireNotNull(activity.getSystemService<PrintManager>())
                printManager.print(
                    activity.getString(R.string.app_name),
                    adapter,
                    printAttributes()
                )
            }.onFailure {
                error.postValue(it)
            }
        }.also {
            result.postValue(it)
        }

    fun sharePDF(adapter: PrintDocumentAdapter) = launch {
        PdfFile(printAttributes()).save(
            adapter,
            path,
            FILE_NAME
        )
        fileSharing.getFileIntentProvider(
            path,
            FILE_NAME,
            true
        )
    }

    private fun printAttributes(): PrintAttributes = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
        .build()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccExportAllOverviewViewModel>

    companion object {
        private const val FILE_NAME = "certificates.pdf"
    }

    sealed interface ExportResult
    data class PrintResult(val print: (activity: Activity) -> Unit) : ExportResult
    data class (val print: (activity: Activity) -> Unit) : ExportResult
}
