package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.zxing.qrcode.encoder.ByteMatrix
import java.nio.IntBuffer
import java.util.stream.IntStream

data class TextArea(val x: Float, val y: Float, val width: Float)

fun Canvas.drawTextIntoRectangle(text: String, paint: Paint, area: TextArea) {
    val textWidth = paint.measureText(text)
    drawText(text, area.x, area.y + paint.textSize, paint)
}

fun ByteMatrix.toBitmap(): Bitmap = Bitmap.createBitmap(
    IntStream.range(0, height)
        .flatMap { h ->
            IntStream.range(0, width).map { w ->
                if (get(w, h) > 0) Color.BLACK else Color.WHITE
            }
        }
        .collect({ IntBuffer.allocate(width * height) }, IntBuffer::put, IntBuffer::put)
        .array(),
    width, height, Bitmap.Config.RGB_565
)

