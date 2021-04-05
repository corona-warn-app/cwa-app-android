package de.rki.coronawarnapp.eventregistration.events.server.qrcodepostertemplate

import de.rki.coronawarnapp.util.security.SignatureValidation
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest

internal class QrCodePosterTemplateServerTest : BaseTest() {

    @MockK lateinit var api: QrCodePosterTemplateApiV1
    @MockK lateinit var signatureValidation: SignatureValidation
    @MockK lateinit var templateCache: QrCodePosterTemplateCache

    /**
     * Info: [QrCodePosterTemplateApiV1Test] is testing if the ETag is set correctly
     */

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { signatureValidation.hasValidSignature(any(), any()) } returns true
        every { templateCache.saveTemplate(any()) } just Runs
        every { templateCache.getTemplate() } returns "CACHE".toByteArray()
    }

    private fun createInstance() = QrCodePosterTemplateServer(
        api = api,
        signatureValidation = signatureValidation,
        templateCache = templateCache
    )

    @Test
    fun `should return poster template when response is successful`() = runBlockingTest {
        coEvery {
            api.getQrCodePosterTemplate()
        } returns Response.success(POSTER_BUNDLE.toResponseBody())

        createInstance().downloadQrCodePosterTemplate().apply {
            template.toStringUtf8().substring(0, 22) shouldBe "<vector xmlns:android="
            offsetX shouldBe 10
            offsetY shouldBe 10
            qrCodeSideLength shouldBe 100
            with(descriptionTextBox) {
                offsetX shouldBe 10
                offsetY shouldBe 50
                width shouldBe 100
                height shouldBe 20
                fontSize shouldBe 10
                fontColor shouldBe "#000000"
            }
        }

        verify(exactly = 1) { signatureValidation.hasValidSignature(any(), any()) }
    }

    @Test
    fun `should fallback to cached or default template if signature is invalid`() = runBlockingTest {
        every { signatureValidation.hasValidSignature(any(), any()) } returns false

        coEvery {
            api.getQrCodePosterTemplate()
        } returns Response.success(POSTER_BUNDLE.toResponseBody())

        createInstance().getTemplateFromApiOrCache() shouldBe "CACHE".toByteArray()
    }

    @Test
    fun `should throw exception if response contains invalid data`() = runBlockingTest {
        coEvery {
            api.getQrCodePosterTemplate()
        } returns Response.success("ABC123".decodeHex().toResponseBody())

        shouldThrow<QrCodePosterTemplateInvalidResponseException> {
            createInstance().downloadQrCodePosterTemplate()
        }
    }

    @Test
    fun `should fallback to cached or default template when response is not successful`() = runBlockingTest {
        coEvery {
            api.getQrCodePosterTemplate()
        } returns Response.error(404, "ERROR".toResponseBody())

        createInstance().getTemplateFromApiOrCache() shouldBe "CACHE".toByteArray()
    }

    companion object {
        /*
        POSTER_BUNDLE below encodes the following protobuf objects:

        private val descriptionTextBox =
            QrCodePosterTemplate.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid.newBuilder()
                .setOffsetX(10)
                .setOffsetY(50)
                .setWidth(100)
                .setHeight(20)
                .setFontSize(10)
                .setFontColor("#000000")
                .build()

        private val qrCodePosterTemplate = QrCodePosterTemplate.QRCodePosterTemplateAndroid.newBuilder()
            .setOffsetX(10.0f)
            .setOffsetY(10.0f)
            .setQrCodeSideLength(100)
            .setDescriptionTextBox(descriptionTextBox)
            .setTemplate(
                ByteString.copyFromUtf8("""<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    xmlns:aapt=\"http://schemas.android.com/aapt\"\n" +
                "    android:width=\"595.3dp\"\n" +
                "    android:height=\"841.9dp\"\n" +
                "    android:viewportWidth=\"595.3\"\n" +
                "    android:viewportHeight=\"841.9\">\n" +
                "  <path\n" +
                "      android:pathData=\"M78.1,665.6v-12.8h1.2l3.6,8.5v-8.5h1.5v12.8h-1.1l-3.7,-8.7v8.7H78.1z\"\n" +
                "      android:fillColor=\"#404040\"/>\n" +
                "</vector>\n"""))
            .build()*/

        private val POSTER_BUNDLE = (
            "504b03040a000000080014867d52008c85fefb000000ab0100000a0000006578706f72742e62" +
                "696e7d90cf4bc33014c7071e949c0415bc0825bbc8685f9676edbad216440fbb78f61c9a6a8ae912da9089ff80ffb66957700e" +
                "f185bcc3e7fbe3f0d0d7596eebcaa8cefb68e5aecfd88e77aae10516c6e88c90be1275cb7a983854aa254cbf93aeeec9c430f2" +
                "dc4c71a6cdff59673804269aed1b6e4481e34d0c11d7bf3551376fc215a62b0a9b53d136f55eabcebc1c15fcedd81ed7e0d279" +
                "72cd8c18bd3fee013d31c30afcbc4e81fa49124362031a422a28843282c44f21b6815b0ec47654020a540611ac7dc7d7d6fded" +
                "90fec427edaf8d948f4aaaaec0f3d572789894282787eb97e86636f31eee86e5f1c5d505ba0c6fb9777d8fc2f3f9729c6f504b" +
                "03040a000000080014867d528a1d0eac8f0000008a0000000a0000006578706f72742e736967018a0075ff0a87010a380a1864" +
                "652e726b692e636f726f6e617761726e6170702d6465761a02763122033236322a13312e322e3834302e31303034352e342e33" +
                "2e321001180122473045022100c251eb5e62282e5573fdb915edf61115d61d020354a510bed66b7b8ce482a38d02202a793775" +
                "0958155c82a17acb6dd4b666afc1566285ef532e6e8c11e1d52e5a75504b01020a000a000000080014867d52008c85fefb0000" +
                "00ab0100000a0000000000000000000000a401000000006578706f72742e62696e504b01020a000a000000080014867d528a1d" +
                "0eac8f0000008a0000000a0000000000000000000000a401230100006578706f72742e736967504b0506000000000200020070" +
                "000000da0100000000"
            ).decodeHex()
    }
}
