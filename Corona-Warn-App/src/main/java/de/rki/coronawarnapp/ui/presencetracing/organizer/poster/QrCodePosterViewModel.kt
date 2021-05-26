package de.rki.coronawarnapp.ui.presencetracing.organizer.poster

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.PosterTemplateProvider
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.Template
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class QrCodePosterViewModel @AssistedInject constructor(
    @Assisted private val traceLocationId: Long,
    private val dispatcher: DispatcherProvider,
    private val qrCodeGenerator: QrCodeGenerator,
    private val posterTemplateProvider: PosterTemplateProvider,
    private val traceLocationRepository: TraceLocationRepository,
    private val appConfigProvider: AppConfigProvider,
    private val fileSharing: FileSharing
) : CWAViewModel(dispatcher) {

    private val posterLiveData = MutableLiveData<Poster>()
    val poster: LiveData<Poster> = posterLiveData
    val sharingIntent = SingleLiveEvent<FileSharing.FileIntentProvider>()

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
            val directory = File(view.context.cacheDir, "poster").apply { if (!exists()) mkdirs() }
            val file = File(directory, "cwa-qr-code.pdf")

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

            sharingIntent.postValue(fileSharing.getFileIntentProvider(file, traceLocation().description))
        } catch (e: Exception) {
            Timber.d(e, "Creating pdf failed")
            e.report(ExceptionCategory.UI)
        }
    }

    private fun generatePoster() = launch(context = dispatcher.IO) {
        try {
            val traceLocation = traceLocation()
            val template = posterTemplateProvider.template()
            val correctionLevel = appConfigProvider.currentConfig.first().presenceTracing.qrCodeErrorCorrectionLevel
            Timber.d("template=$template")
            val qrCode = qrCodeGenerator.createQrCode(
                input = traceLocation.locationUrl,
                length = template.qrCodeLength,
                margin = 0,
                correctionLevel = correctionLevel
            )

            val textInfo = buildString {
                append(traceLocation.description)
                appendLine()
                append(traceLocation.address)
            }
            posterLiveData.postValue(
                Poster(qrCode, template, textInfo)
            )
        } catch (e: Exception) {
            Timber.d(e, "Generating poster failed")
            posterLiveData.postValue(Poster())
            e.report(ExceptionCategory.UI)
        }
    }

    private suspend fun traceLocation() = traceLocationRepository.traceLocationForId(traceLocationId)

    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodePosterViewModel> {
        fun create(
            traceLocationId: Long
        ): QrCodePosterViewModel
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

data class Poster(
    val qrCode: Bitmap? = null,
    val template: Template? = null,
    val infoText: String = ""
) {
    fun hasImages(): Boolean = qrCode != null && template?.bitmap != null
}
