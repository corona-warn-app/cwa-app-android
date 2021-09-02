package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.zxing.qrcode.encoder.ByteMatrix
import java.nio.IntBuffer
import java.util.stream.IntStream

fun Canvas.drawTextIntoRectangle(text: String, paint: Paint, area: TextArea) {
    val textList = getMultilineText(text, paint, area.width.toInt())
    drawMultilineText(textList, paint, area.x, area.y + paint.textSize)
}

/*
 Method split text int multiple lines that are able to fit maximal available width
 */
fun getMultilineText(text: String, paint: Paint, maxSize: Int): List<String> {
    return if (paint.measureText(text) > maxSize) {
        var longestTextSize = text.length
        for (i in 1..text.length) {
            if (paint.measureText(text.substring(0, i)) > maxSize) {
                longestTextSize = i - 1
                break
            }
        }
        val lastSpace = text.lastIndexOf(' ', longestTextSize)
        val textLine = text.substring(0, if (lastSpace > 0) lastSpace else longestTextSize)
        val rest = text.substring(textLine.length)
        listOf(textLine.trim()) + getMultilineText(rest.trim(), paint, maxSize)
    } else {
        listOf(text.trim())
    }
}

fun Canvas.drawMultilineText(text: List<String>, paint: Paint, x: Float, y: Float) {
    text.forEachIndexed { index, line ->
        drawText(line, x, y + paint.textSize * index * 1.2f, paint)
    }
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
