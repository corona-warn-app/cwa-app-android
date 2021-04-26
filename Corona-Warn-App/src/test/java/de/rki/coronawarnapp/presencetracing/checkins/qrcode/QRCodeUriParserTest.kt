package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.CWALocationData
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.QRCodePayload
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor.PayloadEncoding
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest

@Suppress("BlockingMethodInNonBlockingContext")
class QRCodeUriParserTest : BaseTest() {

    @MockK lateinit var configProvider: AppConfigProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { configProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer(
                qrCodeDescriptors = listOf(
                    PresenceTracingQRCodeDescriptor.newBuilder()
                        .setVersionGroupIndex(0)
                        .setEncodedPayloadGroupIndex(1)
                        .setPayloadEncoding(PayloadEncoding.BASE64)
                        .setRegexPattern("https://e\\.coronawarn\\.app\\?v=(\\d+)\\#(.+)")
                        .build()
                )
            )
        }
    }

    fun createInstance() = QRCodeUriParser(configProvider)

    @ParameterizedTest
    @ArgumentsSource(Base64UrlProvider::class)
    fun `Base64 Valid URLs`(
        input: String,
        expectedPayload: QRCodePayload,
        expectedVendorData: CWALocationData
    ) = runBlockingTest {
        val qrCodePayload = createInstance().getQrCodePayload(input)
        qrCodePayload shouldBe expectedPayload
        CWALocationData.parseFrom(qrCodePayload.vendorData) shouldBe expectedVendorData
    }

    @ParameterizedTest
    @ArgumentsSource(Base32UrlProvider::class)
    fun `Base32 Valid URLs`(
        input: String,
        expectedPayload: QRCodePayload,
        expectedVendorData: CWALocationData
    ) = runBlockingTest {
        coEvery { configProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer(
                qrCodeDescriptors = listOf(
                    PresenceTracingQRCodeDescriptor.newBuilder()
                        .setVersionGroupIndex(0)
                        .setEncodedPayloadGroupIndex(1)
                        .setPayloadEncoding(PayloadEncoding.BASE32)
                        .setRegexPattern("https://e\\.coronawarn\\.app\\?v=(\\d+)\\#(.+)")
                        .build()
                )
            )
        }

        val qrCodePayload = createInstance().getQrCodePayload(input)
        qrCodePayload shouldBe expectedPayload
        CWALocationData.parseFrom(qrCodePayload.vendorData) shouldBe expectedVendorData
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidUrlProvider::class)
    fun `Invalid URLs`(input: String) = runBlockingTest {
        shouldThrow<InvalidQrCodeUriException> {
            createInstance().getQrCodePayload(input)
        }
    }
}
