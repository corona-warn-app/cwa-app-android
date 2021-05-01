package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate.QrCodePosterTemplateServer
import de.rki.coronawarnapp.server.protocols.internal.pt.QrCodePosterTemplate.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.roundToInt

class PosterTemplateProvider @Inject constructor(
    private val posterTemplateServer: QrCodePosterTemplateServer,
    private val dispatcherProvider: DispatcherProvider,
    @AppContext private val context: Context
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun template(): Template = withContext(dispatcherProvider.IO) {
        val templateData = posterTemplateServer.downloadQrCodePosterTemplate()
        val file = File(context.cacheDir, "template.pdf")
        FileOutputStream(file).use { it.write(templateData.template.toByteArray()) }

        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fileDescriptor)

        val page = renderer.openPage(0)
        val scale = (context.resources.displayMetrics.density / page.width * page.height).roundToInt()
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

        Template(
            bitmap = bitmap,
            width = page.width,
            height = page.height,
            offsetX = templateData.offsetX,
            offsetY = templateData.offsetY,
            qrCodeLength = templateData.qrCodeSideLength,
            textBox = templateData.descriptionTextBox
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
