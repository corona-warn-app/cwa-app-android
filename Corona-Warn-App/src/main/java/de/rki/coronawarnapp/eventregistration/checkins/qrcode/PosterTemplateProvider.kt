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

class PosterTemplateProvider @Inject constructor(
    private val posterTemplateServer: QrCodePosterTemplateServer,
    @AppContext private val context: Context
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun template(): Template {
        val poster = posterTemplateServer.downloadQrCodePosterTemplate()
        val file = File(context.cacheDir, "template.pdf")
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
        val scale = (context.resources.displayMetrics.density / page.width * page.height).toInt()
        Timber.d("scale=$scale")
        val bitmap = Bitmap.createBitmap(
            context.resources.displayMetrics,
            page.width * scale,
            page.height * scale,
            Bitmap.Config.ARGB_8888
        )

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        file.delete()

        return Template(
            bitmap = bitmap,
            width = page.width,
            height = page.height,
            offsetX = 0.160f, /* TODO poster.offsetX*/
            offsetY = 0.095f, /* TODO poster.offsetY*/
            qrCodeLength = 1000/* TODO poster.qrCodeSideLength*/,
            textBox = poster.descriptionTextBox
        )
    }
}

data class Template(
    val bitmap: Bitmap?,
    val width: Int,
    val height: Int,
    val offsetX: Float,
    val offsetY: Float,
    val qrCodeLength: Int,
    val textBox: QRCodeTextBoxAndroid
)
