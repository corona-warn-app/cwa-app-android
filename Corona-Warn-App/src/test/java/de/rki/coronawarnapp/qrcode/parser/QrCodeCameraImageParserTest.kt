package de.rki.coronawarnapp.qrcode.parser

import boofcv.struct.image.GrayU8
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.asDispatcherProvider

class QrCodeCameraImageParserTest : BaseTest() {

    lateinit var parser: QrCodeCameraImageParser

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private val testDispatcherProvider = testScope.asDispatcherProvider()

    @BeforeEach
    fun setup() {
        parser = QrCodeCameraImageParser(testDispatcherProvider)
    }

    @Test
    fun `finds nothing`() {
        val image = createEmptyGrayU8(80, 80)

        testDispatcher.runBlockingTest {
            parser.parseQrCode(image)
            parser.rawResults.firstOrNull() shouldBe null
        }
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
}
