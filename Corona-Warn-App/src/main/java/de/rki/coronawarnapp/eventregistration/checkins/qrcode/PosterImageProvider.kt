package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import de.rki.coronawarnapp.eventregistration.events.server.qrcodepostertemplate.QrCodePosterTemplateServer
import de.rki.coronawarnapp.server.protocols.internal.pt.QrCodePosterTemplate.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PosterImageProvider @Inject constructor(
    private val posterTemplateServer: QrCodePosterTemplateServer,
    @AppContext private val context: Context
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun posterTemplate(): Template {
        val poster = posterTemplateServer.downloadQrCodePosterTemplate()
        val file = File(context.cacheDir, "poster.pdf")
        FileOutputStream(file).use { it.write(poster.template.toByteArray()) }

        Timber.d(
            "posterTemplate=[x=%s, y=%s, side=%s, descriptionTextBox=%s]",
            poster.offsetX,
            poster.offsetY,
            poster.qrCodeSideLength,
            poster.descriptionTextBox
        )

        val input = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(input)

        val page = renderer.openPage(0)
        val density = 4
        val bitmap = Bitmap.createBitmap(
            context.resources.displayMetrics,
            page.width * density,
            page.height * density,
            Bitmap.Config.ARGB_8888
        )

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        page.close()
        renderer.close()
        file.delete()

        return Template(
            image = bitmap,
            offsetX = poster.offsetX,
            offsetY = poster.offsetY,
            qrCodeLength = poster.qrCodeSideLength,
            textBox = poster.descriptionTextBox
        )
    }
}

data class Template(
    val image: Bitmap?,
    val offsetX: Float,
    val offsetY: Float,
    val qrCodeLength: Int,
    val textBox: QRCodeTextBoxAndroid
)
