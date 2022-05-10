package de.rki.coronawarnapp.util.qrcode.coil

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import coil.bitmap.BitmapPool
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.Options
import coil.size.OriginalSize
import coil.size.PixelSize
import coil.size.Size
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import okio.BufferedSource
import timber.log.Timber
import java.io.ObjectInputStream
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.nextUp

@Reusable
class BitMatrixDecoder @Inject constructor(
    @AppContext private val context: Context,
) : Decoder {
    private val resources: Resources = context.resources

    override fun handles(source: BufferedSource, mimeType: String?): Boolean = mimeType == MIME_TYPE

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun decode(pool: BitmapPool, source: BufferedSource, size: Size, options: Options): DecodeResult {
        val bitmapStart = System.currentTimeMillis()

        val matrix = ObjectInputStream(source.inputStream()).use {
            @Suppress("UNCHECKED_CAST")
            it.readObject() as Array<ByteArray>
        }

        //  The matrix is array[y][x], e.g. a list of columns
        val matrixHeight = matrix.size
        val matrixWidth = matrix.first().size

        val (targetWidth, targetHeight) = when (size) {
            OriginalSize -> matrixWidth to matrixWidth
            is PixelSize -> size.width to size.height
        }

        val marginFactor = 1
        val squareSize = min(
            targetWidth.toFloat() / (matrixWidth + marginFactor * 2),
            targetHeight.toFloat() / (matrixHeight + marginFactor * 2)
        ).nextUp()

        // The rest of the space if the target size is not a multiple of the matrix size
        val paddingHeight = (targetHeight - squareSize * matrixHeight) / 2f
        val paddingWidth = (targetWidth - squareSize * matrixWidth) / 2f

        val bitmap = Bitmap.createBitmap(
            resources.displayMetrics,
            targetWidth,
            targetHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paintBlack = Paint().apply { color = Color.BLACK }

        for (y in 0 until matrixHeight) {
            for (x in 0 until matrixWidth) {
                if (matrix[y][x] != BLACK_SQUARE) continue

                val left = (x) * squareSize + paddingWidth
                val top = (y) * squareSize + paddingHeight
                canvas.drawRect(
                    left,
                    top,
                    left + squareSize,
                    top + squareSize,
                    paintBlack
                )
            }
        }

        Timber.v("Bitmap generation took %dms", System.currentTimeMillis() - bitmapStart)

        return DecodeResult(
            drawable = BitmapDrawable(resources, bitmap),
            isSampled = false,
        )
    }

    companion object {
        private val BLACK_SQUARE = 1.toByte()
        const val MIME_TYPE = "image/qrcode-bitmatrix"
    }
}
