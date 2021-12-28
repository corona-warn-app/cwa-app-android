package de.rki.coronawarnapp.qrcode.parser

import boofcv.struct.image.GrayU8
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.asDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import java.io.File
import java.nio.file.InvalidPathException

@Suppress("MaxLineLength")
class QrCodeCameraImageParserTest : BaseTest() {

    lateinit var parser: QrCodeCameraImageParser

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private val testDispatcherProvider = testScope.asDispatcherProvider()

    private val emptyImage = createEmptyGrayU8(80, 80)

    @BeforeEach
    fun setup() {
        parser = QrCodeCameraImageParser(testDispatcherProvider)
    }

    @Test
    fun `finds nothing`() {
        runBlockingTest2(ignoreActive = true) {
            parser.parseQrCode(emptyImage)
            parser.rawResults.firstOrNull() shouldBe null
        }
    }

    @Test
    fun `finds certificate qr code`() {
        val path = File(TEST_DATA_DIRECTORY)
        if (path.exists()) parseImageFile(path)
        else throw InvalidPathException(TEST_DATA_DIRECTORY, "Directory with test data is missing")
    }

    private fun parseImageFile(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach(::parseImageFile)
        } else if (file.isFile && file.isImage()) {
            val image = Image(file).toGrayU8()
            runBlockingTest2(ignoreActive = true) {
                parser.parseQrCode(emptyImage)
                parser.parseQrCode(image)
                parser.parseQrCode(emptyImage)
                parser.rawResults.first() shouldBe "HC1:6BFOXN*TS0BI\$ZDFRH5TSWK3*93X+0/R8+C1NDC2LE \$C3I9Y1Q20JDJ54-S2UBF/8X*G-O9UVPQRHIY1VS1NQ1 WUQRELS4 CT3SVI\$21\$4Y/K/*K+2T++K+*4KCTO%K4F7TEF+*4U70H:9\$.FE3KZD5CC9T0HM%2CNNK1H--8SGH.+HAMI PQVW5/O16%HAT1Z%PHOP+MMBT16Y5TU1AT1SU9ZIEQKERQ8IY1I\$H3:U8 9QS5/IECN5U.RTOE2QE2K5N%EQJARMA0THWM6J\$7XLH5G6TH9YJA*LA/CJ-LH/CJ6IAXPMHQ1*P14W19UERU9:PIOEG499FQ5VA131A.V56GAM3Q/RQJZI+EBR3E%JTQOL200GTVQWRSAGOD6P+1746.FE%S6Z H V66VEP-1WJHMN1JAVY+M TUBBEQ6C+XCGZEEVU8+N 4FW5B+UVA1M7TUY2V3JH2QPJ2AR8Q14WIUM-J6BYSJ%PVCSPXL9O5+XB6Q7+K8DZHC9JZRKK:3 CD-7J78B%8FS40L%P-3"
            }
        }
    }

    private fun Image.toGrayU8(): GrayU8 {
        // Add a white border around the image because BoofCV otherwise fails finding the QR code
        val pixels = IntArray(width * height)
        val border = 15
        val data = createEmptyGrayU8(width + 2 * border, height + 2 * border)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val argb = pixels[x + y * width].toUInt()
                data[x + border, y + border] =
                    if (argb and 0xff000000U == 0U) 0xffffffffU.toInt() else argb.toInt()
            }
        }

        return data
    }

    private fun createEmptyGrayU8(width: Int, height: Int): GrayU8 {
        val data = GrayU8(width, height)
        for (y in 0 until data.height) {
            for (x in 0 until data.width) {
                data[x, y] = 0xffffffffU.toInt()
            }
        }
        return data
    }

    private fun File.isImage() =
        listOf(".png", ".jpg", ".jpeg").any { name.endsWith(it) }
}

private class Image(file: File) {
    val width: Int
    val height: Int
    val pixels: IntArray

    val imageIO = Class.forName("javax.imageio.ImageIO")
    val readMethod = imageIO.getDeclaredMethod("read", java.net.URL::class.java)

    init {
        val image = readMethod.invoke(imageIO, file.toURI().toURL())
        val getWidth = image::class.java.getDeclaredMethod("getWidth")
        val getHeight = image::class.java.getDeclaredMethod("getHeight")
        val getRGB = image::class.java.getDeclaredMethod("getRGB", Int::class.java, Int::class.java)
        width = getWidth.invoke(image) as Int
        height = getHeight.invoke(image) as Int
        pixels = IntArray(width * height)
        for (x in 0 until width) {
            for (y in 0 until height) {
                pixels[x + width * y] = getRGB.invoke(image, x, y) as Int
            }
        }
    }
}

private const val TEST_DATA_DIRECTORY = "./src/test/dcc-test-data"
