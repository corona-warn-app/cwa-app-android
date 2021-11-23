package de.rki.coronawarnapp.covidcertificate.pdf.ui.poster

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.pdf.core.PdfGenerator
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
    private val pdfGenerator: PdfGenerator,
) : CWAViewModel(dispatcher) {

    val sharingIntent = SingleLiveEvent<FileSharing.FileIntentProvider>()
    val error = SingleLiveEvent<CertificateExportException>()

    private val mutableUiState = MutableLiveData<UiState>(UiState.InProgress)
    val uiState: LiveData<UiState>
        get() = mutableUiState

    init {
        generatePoster()
    }

    private fun getPDFFileName(certificateData: CwaCovidCertificate): String {
        val pdfNameSuffix = with(certificateData) {
            val lastNameUnderscored = lastName
                .replace(" ", "_")
                .replace("-", "_")

            if (firstName.isNullOrBlank()) {
                lastNameUnderscored
            } else {
                val firstNameUnderscored = firstName
                    ?.replace(" ", "_")
                    ?.replace("-", "_")
                "${firstNameUnderscored}_$lastNameUnderscored"
            }
        }

        return "health_certificate_$pdfNameSuffix.pdf"
    }

    private fun generatePoster() = launch(context = dispatcher.IO) {
        try {
            val certificateData = certificateProvider.findCertificate(containerId)
            val fileName = getPDFFileName(certificateData)
            val certificateFile = pdfGenerator.createDgcPdf(certificateData, fileName)
            mutableUiState.postValue(
                UiState.Done(
                    certificateFile,
                    pdfGenerator.renderPdfFileToBitmap(certificateFile, PdfGenerator.BitmapQuality.PREVIEW)
                )
            )
            sharingIntent.postValue(fileSharing.getFileIntentProvider(certificateFile, fileName))
        } catch (e: Exception) {
            Timber.e(e, "Generating poster failed")
            error.postValue(CertificateExportException(e.cause, e.message))
        }
    }

    private fun deleteFile() = launch(scope = appScope, context = dispatcher.IO) {
        try {
            sharingIntent.value?.file?.delete()
        } catch (e: Exception) {
            Timber.d(e, "deleteFile failed")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Delete exported pdf file
        Timber.i("CertificatePosterFragment closed. Deleting pdf export now.")
        deleteFile()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CertificatePosterViewModel> {
        fun create(containerId: CertificateContainerId): CertificatePosterViewModel
    }

    sealed class UiState {
        object InProgress : UiState()
        data class Done(val file: File?, val bitmap: Bitmap?) : UiState()
    }
}
