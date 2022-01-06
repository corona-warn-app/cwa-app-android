package de.rki.coronawarnapp.qrcode.parser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTestInstrumentation
import java.io.InputStream
import java.nio.file.Paths

@RunWith(AndroidJUnit4::class)
class QrCodeBoofCVParserTest : BaseTestInstrumentation() {

    private val instance: QrCodeBoofCVParser
        get() = QrCodeBoofCVParser()

    private val rawResult =
        "HC1:6BFOXN*TS0BI\$ZDFRH5TSWK3*93X+0/R8+C1NDC2LE \$C3I9Y1Q20JDJ54-S2UBF/8X*G-O9UVPQRHIY1VS1NQ1 WUQRELS4 CT3SVI\$21\$4Y/K/*K+2T++K+*4KCTO%K4F7TEF+*4U70H:9\$.FE3KZD5CC9T0HM%2CNNK1H--8SGH.+HAMI PQVW5/O16%HAT1Z%PHOP+MMBT16Y5TU1AT1SU9ZIEQKERQ8IY1I\$H3:U8 9QS5/IECN5U.RTOE2QE2K5N%EQJARMA0THWM6J\$7XLH5G6TH9YJA*LA/CJ-LH/CJ6IAXPMHQ1*P14W19UERU9:PIOEG499FQ5VA131A.V56GAM3Q/RQJZI+EBR3E%JTQOL200GTVQWRSAGOD6P+1746.FE%S6Z H V66VEP-1WJHMN1JAVY+M TUBBEQ6C+XCGZEEVU8+N 4FW5B+UVA1M7TUY2V3JH2QPJ2AR8Q14WIUM-J6BYSJ%PVCSPXL9O5+XB6Q7+K8DZHC9JZRKK:3 CD-7J78B%8FS40L%P-3"

    @Test
    fun finds_qr() {
        val parseResult = QrCodeBoofCVParser.ParseResult.Success(setOf(rawResult))
        val stream = createInputStream(TEST_WORKING_QR_CODE)
        val bitmap = stream.use { BitmapFactory.decodeStream(it) }

        instance.parseQrCode(bitmap) shouldBe parseResult
    }

    @Test
    fun finds_nothing() {
        val parseResult = QrCodeBoofCVParser.ParseResult.Success(emptySet())
        val stream = createInputStream(TEST_EMPTY_IMAGE)
        val bitmap = stream.use { BitmapFactory.decodeStream(it) }

        instance.parseQrCode(bitmap) shouldBe parseResult
    }

    @Test
    fun returns_failure_result() {
        val imageProxy: ImageProxy = mockk {
            every { height } returns 100
            every { width } returns 100
            every { image } returns null
        }

        instance.parseQrCode(imageProxy = imageProxy) should beInstanceOf<QrCodeBoofCVParser.ParseResult.Failure>()

        val bitmap: Bitmap = mockk {
            every { height } returns 100
            every { width } returns 100
        }

        instance.parseQrCode(bitmap = bitmap) should beInstanceOf<QrCodeBoofCVParser.ParseResult.Failure>()
    }

    private fun createInputStream(filename: String): InputStream {
        val path = Paths.get(TEST_DATA_DIRECTORY, filename).toString()
        return InstrumentationRegistry.getInstrumentation().context.assets.open(path)
    }
}

private const val TEST_DATA_DIRECTORY = "dcc-test-data"
private const val TEST_WORKING_QR_CODE = "working_qr_code.png"
private const val TEST_EMPTY_IMAGE = "empty_image.png"
