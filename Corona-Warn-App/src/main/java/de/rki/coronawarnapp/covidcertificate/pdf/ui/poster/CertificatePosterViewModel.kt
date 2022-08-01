package de.rki.coronawarnapp.covidcertificate.pdf.ui.poster

import android.app.Activity
import android.print.FilePrinter
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateExportCache
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateTemplate
import de.rki.coronawarnapp.covidcertificate.pdf.core.appendPage
import de.rki.coronawarnapp.covidcertificate.pdf.core.buildHtml
import de.rki.coronawarnapp.covidcertificate.pdf.core.inject
import de.rki.coronawarnapp.covidcertificate.pdf.ui.CertificateExportException
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import java.io.File

class CertificatePosterViewModel @AssistedInject constructor(
    @Assisted private val containerId: CertificateContainerId,
    @AppScope private val appScope: CoroutineScope,
    private val certificateProvider: CertificateProvider,
    private val dispatcher: DispatcherProvider,
    private val fileSharing: FileSharing,
    private val template: CertificateTemplate,
    private val filePrinter: FilePrinter,
    @CertificateExportCache private val path: File

) : CWAViewModel(dispatcher) {

    val sharingIntent = SingleLiveEvent<FileSharing.FileIntentProvider>()
    val error = SingleLiveEvent<CertificateExportException>()

    private val mutableUiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState>
        get() = mutableUiState

    init {
        pdfString()
    }

    fun createPDF(adapter: PrintDocumentAdapter) = launch(context = dispatcher.Main) {
        try {
            filePrinter.print(adapter, path, getPDFFileName())
            mutableUiState.postValue(UiState.Done)
        } catch (e: Exception) {
            Timber.e(e, "createPDF() failed")
            error.postValue(CertificateExportException(e.cause, e.message))
        }
    }

    fun sharePDF() = launch {
        try {
            val fileName = getPDFFileName()
            sharingIntent.postValue(
                fileSharing.getFileIntentProvider(File(path, fileName), fileName, true)
            )
        } catch (e: Exception) {
            Timber.e(e, "sharePDF() failed")
            error.postValue(CertificateExportException(e.cause, e.message))
        }
    }

    fun print(adapter: PrintDocumentAdapter) =
        UiState.PrintResult { activity ->
            runCatching {
                val printManager = requireNotNull(activity.getSystemService<PrintManager>())
                printManager.print(
                    activity.getString(R.string.app_name),
                    adapter,
                    filePrinter.attributes
                )
            }.onFailure { e ->
                Timber.e(e, "print() failed")
                error.postValue(CertificateExportException(e.cause, e.message))
            }
        }.also {
            mutableUiState.postValue(it)
        }

    override fun onCleared() {
        super.onCleared()
        // Delete exported pdf file
        Timber.i("CertificatePosterFragment closed. Deleting pdf export now.")
        deleteFile()
    }

    private fun deleteFile() = launch(
        scope = appScope,
        context = dispatcher.IO
    ) {
        try {
            File(path, getPDFFileName()).delete()
        } catch (e: Exception) {
            Timber.d(e, "deleteFile() failed")
        }
    }

    private suspend fun getPDFFileName(): String {
        val certificateData = certificateProvider.findCertificate(containerId)
        val pdfNameSuffix = with(certificateData) {
            fullName.replace(" ", "_").replace("-", "_")
        }

        return "health_certificate_$pdfNameSuffix.pdf"
    }

    private fun pdfString() = launch(context = dispatcher.IO) {
        try {
            val certificate = certificateProvider.findCertificate(containerId)
            mutableUiState.postValue(
                UiState.PDF(
                    buildHtml {
                        appendPage(template(certificate).inject(certificate))
                    }
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Generating poster failed")
            error.postValue(CertificateExportException(e.cause, e.message))
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CertificatePosterViewModel> {
        fun create(containerId: CertificateContainerId): CertificatePosterViewModel
    }

    sealed class UiState {
        object Done : UiState()
        data class PrintResult(val print: (activity: Activity) -> Unit) : UiState()
        data class PDF(val pdfString: String) : UiState()
    }
}
