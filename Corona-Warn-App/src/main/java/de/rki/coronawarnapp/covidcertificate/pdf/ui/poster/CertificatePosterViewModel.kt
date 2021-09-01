package de.rki.coronawarnapp.covidcertificate.pdf.ui.poster

import android.graphics.pdf.PdfDocument
import android.view.View
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class CertificatePosterViewModel @AssistedInject constructor(
    @Assisted private val containerId: CertificateContainerId,
    private val certificateProvider: CertificateProvider,
    private val dispatcher: DispatcherProvider,
    private val fileSharing: FileSharing,
) : CWAViewModel(dispatcher) {

    val sharingIntent = SingleLiveEvent<FileSharing.FileIntentProvider>()

    private lateinit var certificateData: CwaCovidCertificate

    init {
        generatePoster()
    }

    /**
     * Create a new PDF file and result is delivered by [sharingIntent]
     * as a sharing [FileSharing.ShareIntentProvider]
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    fun createPDF(view: View) = launch(context = dispatcher.IO) {
        try {
            val weakViewRef = WeakReference(view) // Accessing view in background thread
            val directory = File(view.context.cacheDir, "certificates").apply { if (!exists()) mkdirs() }
            val file = File(directory, "cwa-certificate.pdf") // TODO: add certificate type here

            val weakView = weakViewRef.get() ?: return@launch // View is not existing anymore
            val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
            PdfDocument().apply {
                startPage(pageInfo).apply {
                    val sx = A4_WIDTH.toFloat() / weakView.width
                    val sy = A4_HEIGHT.toFloat() / weakView.height
                    canvas.scale(sx, sy)
                    weakView.draw(canvas)
                    finishPage(this)
                }

                FileOutputStream(file).use {
                    writeTo(it)
                    close()
                }
            }

            sharingIntent.postValue(fileSharing.getFileIntentProvider(file, getPDFFileName()))
        } catch (e: Exception) {
            Timber.d(e, "Creating pdf failed")
            e.report(ExceptionCategory.UI)
        }
    }

    private fun getPDFFileName() =
        when (certificateData) {
            is RecoveryCertificate -> "recovery_certificate"
            is TestCertificate -> "test_certificate"
            else -> "vaccination_certificate"
        }

    private fun generatePoster() = launch(context = dispatcher.IO) {
        try {
            certificateData = certificateProvider.findCertificate(containerId)
            Timber.d("Certificate found: ${certificateData.fullName}")
            // TODO: generate poster here with provided certificate
        } catch (e: Exception) {
            Timber.d(e, "Generating poster failed")
            // TODO: empty poster here
            e.report(ExceptionCategory.UI)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CertificatePosterViewModel> {
        fun create(containerId: CertificateContainerId): CertificatePosterViewModel
    }

    companion object {
        /**
         * A4 size in PostScript
         * @see <a href="https://www.cl.cam.ac.uk/~mgk25/iso-paper-ps.txt">Iso-paper-ps</a>
         */
        private const val A4_WIDTH = 595 // PostScript
        private const val A4_HEIGHT = 842 // PostScript
    }
}
