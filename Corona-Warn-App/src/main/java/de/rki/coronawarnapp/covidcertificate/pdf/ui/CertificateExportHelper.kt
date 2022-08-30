package de.rki.coronawarnapp.covidcertificate.pdf.ui

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.qrcode.encoder.ByteMatrix

fun ByteMatrix.toBitmap(): Bitmap = Bitmap.createBitmap(
    (0 until height).flatMap { h ->
        (0 until width).map { w -> if (get(w, h) > 0) Color.BLACK else Color.WHITE }
    }.toIntArray(),
    width, height, Bitmap.Config.RGB_565
)
