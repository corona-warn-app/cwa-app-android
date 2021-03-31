package de.rki.coronawarnapp.ui.eventregistration.organizer.poster

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.PosterImageProvider
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.Template
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
    @Assisted private val locationID: String,
    private val dispatcher: DispatcherProvider,
    private val qrCodeGenerator: QrCodeGenerator,
    private val posterImageProvider: PosterImageProvider,
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
            val file = File(view.context.cacheDir, "CoronaWarnApp-Event.pdf")
            val pageInfo = PdfDocument.PageInfo.Builder(
                view.width,
                view.height,
                1
            ).create()

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

            sharingIntent.postValue(
                fileSharing.getFileIntentProvider(file, "Scan and Help")
            )
        } catch (e: Exception) {
            Timber.d(e, "Creating pdf failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun generatePoster() = launch(context = dispatcher.IO) {
        try {
            val template = posterImageProvider.posterTemplate()
            val qrCode = qrCodeGenerator.createQrCode(locationID, 700)

            posterLiveData.postValue(
                Poster(qrCode = qrCode, template = template)
            )
        } catch (e: Exception) {
            Timber.d(e, "Generating poster failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodePosterViewModel> {
        fun create(
            locationID: String
        ): QrCodePosterViewModel
    }
}

data class Poster(val qrCode: Bitmap?, val template: Template)
