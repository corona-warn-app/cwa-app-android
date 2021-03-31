package de.rki.coronawarnapp.test.eventregistration.ui.qrcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.view.View
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.events.server.qrcodepostertemplate.QrCodePosterTemplateServer
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class QrCodeCreationTestViewModel @AssistedInject constructor(
    private val dispatcher: DispatcherProvider,
    private val fileSharing: FileSharing,
    private val qrCodeGenerator: QrCodeGenerator,
    @AppContext private val context: Context,
    private val posterTemplateServer: QrCodePosterTemplateServer
) : CWAViewModel(dispatcher) {

    val qrCodeBitmap = SingleLiveEvent<Bitmap>()
    val errorMessage = SingleLiveEvent<String>()
    val sharingIntent = SingleLiveEvent<FileSharing.FileIntentProvider>()
    val qrCodePosterTemplate = SingleLiveEvent<File>()

    /**
     * Creates a QR Code [Bitmap] ,result is delivered by [qrCodeBitmap]
     */
    fun createQrCode(input: String) = launch(context = dispatcher.IO) {

        try {
            qrCodeBitmap.postValue(qrCodeGenerator.createQrCode(input))
        } catch (e: Exception) {
            Timber.d(e, "Qr code creation failed")
            errorMessage.postValue(e.localizedMessage ?: "QR code creation failed")
        }
    }

    /**
     * Create a new PDF file and result is delivered by [sharingIntent]
     * as a sharing [FileSharing.ShareIntentProvider]
     */
    fun createPDF(
        view: View
    ) = launch(context = dispatcher.IO) {
        try {
            val file = pdfFile()
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
            errorMessage.postValue(e.localizedMessage ?: "Creating pdf failed")
            Timber.d(e, "Creating pdf failed")
        }
    }

    private fun pdfFile(): File {
        val dir = File(context.filesDir, "events")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "CoronaWarnApp-Event.pdf")
    }

    fun downloadQrCodePosterTemplate() {
        launch {
            try {
                val posterTemplate = posterTemplateServer.downloadQrCodePosterTemplate()

                FileOutputStream(File(context.cacheDir, "poster.pdf")).use {
                    it.write(posterTemplate.template.toByteArray())
                }

                Timber.d(
                    "posterTemplate=[x=%s, y=%s, side=%s, descriptionTextBox=%s]",
                    posterTemplate.offsetX,
                    posterTemplate.offsetY,
                    posterTemplate.qrCodeSideLength,
                    posterTemplate.descriptionTextBox
                )
                qrCodePosterTemplate.postValue(File(context.cacheDir, "poster.pdf"))
            } catch (exception: Exception) {
                Timber.e(exception, "Download failed")
                errorMessage.postValue("Downloading Poster Template failed: ${exception.message}")
            }
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodeCreationTestViewModel>
}
