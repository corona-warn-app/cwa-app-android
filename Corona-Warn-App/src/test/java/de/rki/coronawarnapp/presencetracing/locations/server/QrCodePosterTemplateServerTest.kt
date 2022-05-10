package de.rki.coronawarnapp.presencetracing.locations.server

import de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate.DefaultQrCodePosterTemplateSource
import de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate.QrCodePosterTemplateApiV1
import de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate.QrCodePosterTemplateInvalidResponseException
import de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate.QrCodePosterTemplateServer
import de.rki.coronawarnapp.util.security.SignatureValidation
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest

internal class QrCodePosterTemplateServerTest : BaseTest() {

    @MockK lateinit var api: QrCodePosterTemplateApiV1
    @MockK lateinit var signatureValidation: SignatureValidation
    @MockK lateinit var defaultTemplateSource: DefaultQrCodePosterTemplateSource

    /**
     * Info: [QrCodePosterTemplateApiV1Test] is testing if okhttp caching is working correctly
     */

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { signatureValidation.hasValidSignature(any(), any()) } returns true
        every { defaultTemplateSource.getDefaultQrCodePosterTemplate() } returns "DEFAULT TEMPLATE".toByteArray()
    }

    private fun createInstance() = QrCodePosterTemplateServer(
        api = api,
        signatureValidation = signatureValidation,
        defaultTemplateSource = defaultTemplateSource
    )

    @Test
    fun `should return poster template when response is successful`() = runTest {
        coEvery {
            api.getQrCodePosterTemplate()
        } returns Response.success(POSTER_BUNDLE.toResponseBody())

        createInstance().downloadQrCodePosterTemplate().apply {

            // check if template contains the pdf by checking the first characters
            template.toStringUtf8().substring(0, 8) shouldBe "%PDF-1.1"
            offsetX shouldBe 0.16f
            offsetY shouldBe 0.095f
            qrCodeSideLength shouldBe 1000
            with(descriptionTextBox) {
                offsetX shouldBe 0.132f
                offsetY shouldBe 0.61f
                width shouldBe 100
                height shouldBe 20
                fontSize shouldBe 10
                fontColor shouldBe "#000000"
            }
        }

        verify(exactly = 1) { signatureValidation.hasValidSignature(any(), any()) }
    }

    @Test
    fun `should fallback to default template if signature is invalid`() = runTest {
        every { signatureValidation.hasValidSignature(any(), any()) } returns false

        coEvery {
            api.getQrCodePosterTemplate()
        } returns Response.success(POSTER_BUNDLE.toResponseBody())

        createInstance().getTemplateFromApiOrCache() shouldBe "DEFAULT TEMPLATE".toByteArray()
    }

    @Test
    fun `should throw exception if response contains invalid data`() = runTest {
        coEvery {
            api.getQrCodePosterTemplate()
        } returns Response.success("ABC123".decodeHex().toResponseBody())

        shouldThrow<QrCodePosterTemplateInvalidResponseException> {
            createInstance().downloadQrCodePosterTemplate()
        }
    }

    @Test
    fun `should fallback to default template when response is not successful`() = runTest {
        coEvery {
            api.getQrCodePosterTemplate()
        } returns Response.error(404, "ERROR".toResponseBody())

        createInstance().getTemplateFromApiOrCache() shouldBe "DEFAULT TEMPLATE".toByteArray()
    }

    companion object {
        /*
        POSTER_BUNDLE below encodes the following protobuf objects:

        private val descriptionTextBox =
            QrCodePosterTemplate.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid.newBuilder()
                .setOffsetX(0.132f)
                .setOffsetY(0.61f)
                .setWidth(100)
                .setHeight(20)
                .setFontSize(10)
                .setFontColor("#000000")
                .build()

        private val qrCodePosterTemplate = QrCodePosterTemplate.QRCodePosterTemplateAndroid.newBuilder()
            .setOffsetX(0.16f)
            .setOffsetY(0.095f)
            .setQrCodeSideLength(1000)
            .setDescriptionTextBox(descriptionTextBox)
            .setTemplate(BINARY PDF)
            .build()*/

        private val POSTER_BUNDLE = (
            "504b03040a0000000800685588525c24ae70900100000e0300000a0000006578706f72742e62696e6d52cd4ac3401006410" +
                "a0bde2c28280c9442556a9226db7aa8155a5b04154b1af0507bd89a6d8da45949b6507d099fc293275fa13e840fe0c5" +
                "27f0ec6e7e6ada744f33df7edf7c33b38bbe378bddf34e593bd65071fe36fff87c47480315d8f01101d4eba058cf4f1" +
                "49416e1c465638189a374c9980650113c53208d06a29e2d15a8b2461a9263e1a56307d0d7a57010432d36f538687176" +
                "4d6d8734d90cfaaa20e9aa0a9a610c964df4b449da25aa21639f8a9a497f2166d2804dfdfba493a8c10ef378922f302" +
                "d8d246864b2ca8f8fd29b0e794890b48c5e329a24a0522d28ce840665934d88b74a1433aecd16915896c7c5680118d9" +
                "dd1b4bbbbfa2de983f00c69212709f9289b8695a612531246827608dc24c6edab2c3b074415d97c12df35dfb002c59a" +
                "c6d4987b842e235f3e908a98091ba3850c558c7308205261ca21bef1fabd5329856cbf20c9ce2719f382ef593f73619" +
                "13ff25fdb63de7858a5e201a95f83c6c0f57312a16db371d94475f85c6feddebfc147e7287bb5b1b47b946feb7b477b" +
                "663c37609557285c8f50f504b03040a00000008006855885218d81ad88f0000008a0000000a0000006578706f72742e" +
                "736967018a0075ff0a87010a380a1864652e726b692e636f726f6e617761726e6170702d6465761a027631220332363" +
                "22a13312e322e3834302e31303034352e342e332e3210011801224730450220148d01176d9be98fa78ca1b0cad0b8b1" +
                "2033f35a43cb7d10369536c233701fff022100ed7c748e7f8e82c6a98ead3a19f2041b1ec090268e3ae15aa146bd0d5" +
                "67617c7504b01020a000a0000000800685588525c24ae70900100000e0300000a0000000000000000000000a4010000" +
                "00006578706f72742e62696e504b01020a000a00000008006855885218d81ad88f0000008a0000000a0000000000000" +
                "000000000a401b80100006578706f72742e736967504b05060000000002000200700000006f0200000000"
            ).decodeHex()
    }
}
