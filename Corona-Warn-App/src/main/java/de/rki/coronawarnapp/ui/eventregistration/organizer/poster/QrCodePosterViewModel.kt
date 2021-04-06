package de.rki.coronawarnapp.ui.eventregistration.organizer.poster

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.PosterTemplateProvider
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.Template
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
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
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    fun createPDF(view: View, title: String) = launch(context = dispatcher.IO) {
        try {
            val directory = File(view.context.cacheDir, "poster").apply { if (!exists()) mkdirs() }
            val file = File(directory, "cwa-qr-code.pdf")

            val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()

            PdfDocument().apply {
                startPage(pageInfo).apply {
                    view.draw(canvas)
                    finishPage(this)
                }

                FileOutputStream(file).use {
                    writeTo(it)
                    close()
                }
            }

            sharingIntent.postValue(fileSharing.getFileIntentProvider(file, title))
        } catch (e: Exception) {
            Timber.d(e, "Creating pdf failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun generatePoster() = launch(context = dispatcher.IO) {
        try {
            val traceLocation = traceLocationRepository.traceLocationForId(traceLocationId)
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
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodePosterViewModel> {
        fun create(
            traceLocationId: Long
        ): QrCodePosterViewModel
    }
}

data class Poster(
    val qrCode: Bitmap? = null,
    val template: Template? = null,
    val infoText: String = ""
) {
    fun hasImages(): Boolean = qrCode != null && template?.bitmap != null
}
