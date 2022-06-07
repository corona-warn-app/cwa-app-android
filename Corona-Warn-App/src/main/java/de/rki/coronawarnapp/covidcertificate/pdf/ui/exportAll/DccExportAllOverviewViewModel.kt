package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import android.app.Activity
import android.print.FilePrinter
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.core.content.getSystemService
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateExportCache
import de.rki.coronawarnapp.covidcertificate.person.core.toCertificateSortOrder
import de.rki.coronawarnapp.tag
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
    personCertificatesProvider: CertificateProvider,
    template: CertificateTemplate,
    private val dispatcher: DispatcherProvider,
    private val fileSharing: FileSharing,
    @CertificateExportCache private val path: File
) : CWAViewModel(dispatcher) {

    val error = SingleLiveEvent<Throwable>()
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
        Timber.tag(TAG).e(it, "dccData failed")
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
                Timber.tag(TAG).e(it, "print() failed")
                error.postValue(it)
            }
        }.also {
            result.postValue(it)
        }

    fun sharePDF(adapter: PrintDocumentAdapter) = launch(context = dispatcher.IO) {
        runCatching {
            FilePrinter(printAttributes()).print(
                adapter,
                path,
                FILE_NAME
            )

            result.postValue(
                ShareResult(
                    fileSharing.getFileIntentProvider(File(path, FILE_NAME), FILE_NAME, true)
                )
            )
        }.onFailure {
            Timber.tag(TAG).e(it, "sharePDF() failed")
            error.postValue(it)
        }
    }

    private fun printAttributes(): PrintAttributes = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
        .build()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccExportAllOverviewViewModel>

    sealed interface ExportResult
    data class PrintResult(val print: (activity: Activity) -> Unit) : ExportResult
    data class ShareResult(val provider: FileSharing.FileIntentProvider) : ExportResult

    companion object {
        private const val FILE_NAME = "certificates.pdf"
        private val TAG = tag<DccExportAllOverviewViewModel>()
    }
}
