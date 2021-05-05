package de.rki.coronawarnapp.ui.presencetracing.organizer.poster

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.PosterTemplateProvider
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.Template
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
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
     * @param weakViewRef [WeakReference] of the [View]
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    fun createPDF(weakViewRef: WeakReference<View>) = launch(context = dispatcher.IO) {
        try {
            val view = weakViewRef.get() ?: return@launch // View is not existing anymore
            val directory = File(view.context.cacheDir, "poster").apply { if (!exists()) mkdirs() }
            val file = File(directory, "cwa-qr-code.pdf")
            val pageInfo = PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, 1).create()
            val scaledBitmap = view.toBitmap().resize(WIDTH, HEIGHT)
            PdfDocument().apply {
                startPage(pageInfo).apply {
                    canvas.drawBitmap(scaledBitmap, 0.0f, 0.0f, null)
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

    private fun View.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        draw(canvas)
        return bitmap
    }

    private fun Bitmap.resize(newWidth: Int, newHeight: Int): Bitmap {
        val resizedBitmap = Bitmap.createScaledBitmap(this, newWidth, newHeight, false)
        recycle()
        return resizedBitmap
    }

    private fun generatePoster() = launch(context = dispatcher.IO) {
        try {
            val traceLocation = traceLocation()
            val template = posterTemplateProvider.template()
            Timber.d("template=$template")
            val qrCode = qrCodeGenerator.createQrCode(
                input = traceLocation.locationUrl,
                length = template.qrCodeLength,
                margin = 0
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
         * https://www.cl.cam.ac.uk/~mgk25/iso-paper-ps.txt
         */
        private const val WIDTH = 595 // PostScript
        private const val HEIGHT = 842 // PostScript
    }
}

data class Poster(
    val qrCode: Bitmap? = null,
    val template: Template? = null,
    val infoText: String = ""
) {
    fun hasImages(): Boolean = qrCode != null && template?.bitmap != null
}
