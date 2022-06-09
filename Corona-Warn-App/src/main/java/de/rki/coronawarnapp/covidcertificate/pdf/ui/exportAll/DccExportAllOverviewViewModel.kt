package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import android.app.Activity
import android.print.FilePrinter
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.core.content.getSystemService
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateExportCache
import de.rki.coronawarnapp.covidcertificate.pdf.core.filterAndSortForExport
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateTemplate
import de.rki.coronawarnapp.covidcertificate.pdf.core.appendPage
import de.rki.coronawarnapp.covidcertificate.pdf.core.buildHtml
import de.rki.coronawarnapp.covidcertificate.pdf.core.inject
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
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
    timeStamper: TimeStamper,
    private val dispatcher: DispatcherProvider,
    private val fileSharing: FileSharing,
    private val filePrinter: FilePrinter,
    @CertificateExportCache private val path: File,
) : CWAViewModel(dispatcher) {

    val error = SingleLiveEvent<Throwable>()
    val exportResult = SingleLiveEvent<ExportResult>()

    val pdfString = personCertificatesProvider.certificateContainer.map { container ->
        buildHtml {
            container.allCwaCertificates
                .filterAndSortForExport(timeStamper.nowJavaUTC)
                .forEach { cert ->
                    appendPage(template(cert).inject(cert))
                }
        }
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
                    filePrinter.attributes
                )
            }.onFailure {
                Timber.tag(TAG).e(it, "print() failed")
                error.postValue(it)
            }
        }.also {
            exportResult.postValue(it)
        }

    /**
     * Should be on main dispatcher ,otherwise it throws [IllegalStateException]
     */
    fun createPDF(adapter: PrintDocumentAdapter) = launch(
        context = dispatcher.Main
    ) {
        runCatching {
            filePrinter.print(
                adapter,
                path,
                FILE_NAME
            )
            exportResult.postValue(PDFResult)
        }.onFailure {
            Timber.tag(TAG).e(it, "sharePDF() failed")
            error.postValue(it)
        }
    }

    fun sharePDF() = runCatching {
        exportResult.postValue(
            ShareResult(
                fileSharing.getFileIntentProvider(File(path, FILE_NAME), FILE_NAME, true)
            )
        )
    }.onFailure {
        Timber.tag(TAG).e(it, "sharePDF() failed")
        error.postValue(it)
    }

    override fun onCleared() {
        super.onCleared()
        runCatching {
            File(path, FILE_NAME).delete()
        }.onFailure {
            Timber.tag(TAG).e(it, "delete() failed")
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccExportAllOverviewViewModel>

    sealed interface ExportResult
    data class PrintResult(val print: (activity: Activity) -> Unit) : ExportResult
    data class ShareResult(val provider: FileSharing.FileIntentProvider) : ExportResult
    object PDFResult : ExportResult

    companion object {
        private const val FILE_NAME = "certificates.pdf"
        private val TAG = tag<DccExportAllOverviewViewModel>()
    }
}
