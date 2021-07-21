package de.rki.coronawarnapp.util.qrcode.coil

import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.Encoder
import dagger.Reusable
import okio.Buffer
import java.io.ObjectOutputStream
import javax.inject.Inject

@Reusable
class QrCodeBitMatrixFetcher @Inject constructor() : Fetcher<CoilQrCode> {

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun fetch(pool: BitmapPool, data: CoilQrCode, size: Size, options: Options): FetchResult {

        val hints = mapOf(
            /**
             * We cannot use Charsets.UTF_8 as zxing calls toString internally
             * and some android version return the class name and not the charset name
             */
            EncodeHintType.CHARACTER_SET to data.options.characterSet.name()
        )

        val qrCode = Encoder.encode(
            data.content,
            data.options.correctionLevel,
            hints,
        )

        val buffer = Buffer()

        ObjectOutputStream(buffer.outputStream()).use {
            val rawMatrix: Array<ByteArray> = qrCode.matrix.array
            it.writeObject(rawMatrix)
        }

        return SourceResult(
            source = buffer,
            mimeType = BitMatrixDecoder.MIME_TYPE,
            dataSource = DataSource.MEMORY,
        )
    }

    override fun key(data: CoilQrCode): String = data.requestKey
}
