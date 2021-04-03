package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor.PayloadEncoding
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest
import timber.log.Timber

class QRCodeUriParserTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer(
                qrCodeDescriptors = listOf(
                    PresenceTracingQRCodeDescriptor.newBuilder()
                        .setEncodedPayloadGroupIndex(1)
                        .setPayloadEncoding(PayloadEncoding.BASE64)
                        .setRegexPattern("https://e\\.coronawarn\\.app\\?v=(\\d+)\\#(.+)")
                        .setVersionGroupIndex(0)
                        .build()
                )
            )
        }
    }

    fun createInstance() = QRCodeUriParser(appConfigProvider)

    @ParameterizedTest
    @ArgumentsSource(ValidUrlProvider::class)
    fun `Valid URLs`(input: String) = runBlockingTest {
        val qrCodePayload = createInstance().getQrCodePayload(input)
        qrCodePayload shouldNotBe null
        Timber.d("qrCodePayload=$qrCodePayload")
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidUrlProvider::class)
    fun `Invalid URLs`(input: String) = runBlockingTest {
        createInstance().getQrCodePayload(input) shouldBe null
    }
}
