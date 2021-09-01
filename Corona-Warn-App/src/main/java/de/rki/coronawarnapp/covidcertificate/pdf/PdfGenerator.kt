package de.rki.coronawarnapp.covidcertificate.pdf

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.annotation.ColorInt
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.environment.presencetracing.qrcodeposter.QrCodePosterTemplate
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.nio.IntBuffer
import java.util.stream.IntStream
import javax.inject.Inject

class PdfGenerator @Inject constructor(
    @QrCodePosterTemplate private val cacheDir: File,
    private val font: Typeface,
    private val assetManager: AssetManager
) {

    // TODO: don't forget about try catch

    suspend fun creteVaccinationCertificatePdf(certificate: VaccinationCertificate): File {
        val templateFile = getTemplateFile()
        val outputFile = File(cacheDir, "test.pdf")
        return createPdf(templateFile, outputFile, certificate)
    }

    private fun getQrCode(certificate: VaccinationCertificate, size: Int): Bitmap {
        val hints = mapOf(
            /**
             * We cannot use Charsets.UTF_8 as zxing calls toString internally
             * and some android version return the class name and not the charset name
             */
            EncodeHintType.CHARACTER_SET to certificate.qrCodeToDisplay.options.characterSet.name()
        )

        val qrCode = Encoder.encode(
            certificate.qrCodeToDisplay.content,
            certificate.qrCodeToDisplay.options.correctionLevel,
            hints,
        )

        return Bitmap.createScaledBitmap(bitMatrixToBitmap(qrCode.matrix), size, size, false)
    }

    private fun bitMatrixToBitmap(qrCodeBitMatrix: ByteMatrix): Bitmap {
        val height = qrCodeBitMatrix.height
        val width = qrCodeBitMatrix.width
        return Bitmap.createBitmap(
            IntStream.range(0, height)
                .flatMap { h ->
                    IntStream.range(0, width).map { w ->
                        if (qrCodeBitMatrix[w, h] > 0) Color.BLACK else Color.WHITE
                    }
                }
                .collect({ IntBuffer.allocate(width * height) }, IntBuffer::put, IntBuffer::put)
                .array(),
            width, height, Bitmap.Config.ARGB_8888
        )
    }

    private fun createPdf(templateFile: File, outputFile: File, certificate: VaccinationCertificate): File {
        val fileDescriptor = ParcelFileDescriptor.open(templateFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val firstPage = PdfRenderer(fileDescriptor).openPage(0)

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val bitmap = Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        firstPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

        val paint = Paint().apply {
            typeface = typeface
            textSize = 35f
            color = FONT_COLOR
        }

        val imagePaint = Paint().apply {
            flags = 0x00
            isAntiAlias = false
            isFilterBitmap = false
            isDither = false
        }

        PdfDocument().apply {
            startPage(pageInfo).apply {
                canvas.apply {
                    drawBitmap(bitmap, 0f, 0f, Paint())
                    drawTextIntoRectangle(
                        "\$nam = ${certificate.fullNameFormatted}",
                        paint,
                        TextArea(115f, 2890f, 1115f)
                    )
                    drawTextIntoRectangle(
                        "\$dob = ${certificate.dateOfBirthFormatted}",
                        paint,
                        TextArea(115f, 3080f, 1115f)
                    )
                    drawTextIntoRectangle("\$ci = ${certificate.certificateId}", paint, TextArea(115f, 3265f, 1115f))
                    // 1175 - 550 = 625
                    val qrCode = getQrCode(certificate, 625)
                    Timber.d("Bitmap size ${qrCode.width}x${qrCode.height}")
                    drawBitmap(qrCode, 550f, 1815f, imagePaint)
//                    drawBitmap(
//                        qrCode,
//                        null,
//                        Rect(550, 1815, 550 + qrCode.width * 8, 1815 + qrCode.height * 8),
//                        imagePaint
//                    )

                    save()
                    rotate(180f, PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f)
                    drawTextIntoRectangle("\$tg = ${certificate.targetDisease}", paint, TextArea(1895f, 2040f, 525f))
                    drawTextIntoRectangle("\$vp = ${certificate.vaccineTypeName}", paint, TextArea(1895f, 2155f, 525f))
                    drawTextIntoRectangle(
                        "\$mp = ${certificate.medicalProductName}",
                        paint,
                        TextArea(1300f, 2450f, 525f)
                    )
                    drawTextIntoRectangle(
                        "\$ma = ${certificate.vaccineManufacturer}",
                        paint,
                        TextArea(1300f, 2690f, 525f)
                    )
                    drawTextIntoRectangle(
                        "\$dn/\$sd = ${certificate.doseNumber}/${certificate.totalSeriesOfDoses}",
                        paint,
                        TextArea(1895f, 2830f, 525f)
                    )
                    drawTextIntoRectangle(
                        "\$dt = ${certificate.vaccinatedOnFormatted}",
                        paint,
                        TextArea(1895f, 3030f, 525f)
                    )
                    drawTextIntoRectangle(
                        "\$co = ${certificate.certificateCountry}",
                        paint,
                        TextArea(1895f, 3160f, 525f)
                    )
                    drawTextIntoRectangle(
                        "\$is = ${certificate.certificateIssuer}",
                        paint,
                        TextArea(1895f, 3295f, 525f)
                    )
                    restore()
                }

//                canvas.drawBitmap(bitmap, 0f, 0f, Paint())
//                //y:2890 - 2980, x:115 - 1230
//
//                canvas.drawText("Name, measure = ${paint.measureText("Name")}", 115f, 2930f, paint)
//                canvas.drawText("Date of Birth", 115f, 3130f, paint)
//                canvas.drawText("Certificate Id", 115f, 3320f, paint)
//                canvas.drawText(
//                    "HELLO WORLD CURRENT TIME: ${SimpleDateFormat("HH:mm:ss").format(Date())}",
//                    500f,
//                    500f,
//                    Paint()
//                )
//                canvas.save()
//                canvas.rotate(180f, PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f)
//                canvas.drawText("SECOND LINE", 500f, 525f, Paint())
//                canvas.drawCircle(1840f, 1754f, 50f, Paint().apply { color = Color.RED })
//                canvas.restore()
//                canvas.drawText("THIRD LINE", 500f, 550f, Paint())
                finishPage(this)
            }

            FileOutputStream(outputFile).use {
                writeTo(it)
                close()
            }
        }
        return outputFile
    }

    private fun Canvas.drawTextIntoRectangle(text: String, paint: Paint, area: TextArea) {
        val textWidth = paint.measureText(text)
        drawText(text, area.x, area.y + paint.textSize, paint)
    }

    private fun getTemplateFile(): File {
        val directory = File(cacheDir, "template").apply { if (!exists()) mkdirs() }
        val file = File(directory, TEMPLATE_NAME)
        if (file.exists()) return file

        val assetStream = assetManager.open(ASSET_NAME)
        return file.apply {
            outputStream().use {
                assetStream.copyTo(it)
            }
        }
    }

    companion object {
        const val ASSET_NAME = "vaccination_certificate_template.pdf"
        const val TEMPLATE_NAME = "vaccination_certificate.pdf"
        const val PAGE_WIDTH = 1240 * 2 //A4 - 300ppi
        const val PAGE_HEIGHT = 1754 * 2
        @ColorInt val FONT_COLOR: Int = 0xFF0067A0.toInt()
    }
}

data class TextArea(val x: Float, val y: Float, val width: Float)
