package de.rki.coronawarnapp.ui.presencetracing.organizer.poster

import android.graphics.Bitmap
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfig
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.PosterTemplateProvider
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.Template
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.QrCodePosterTemplate.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid
import de.rki.coronawarnapp.util.files.FileSharing
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class QrCodePosterViewModelTest : BaseTest() {

    @MockK lateinit var qrCodeGenerator: QrCodeGenerator
    @MockK lateinit var posterTemplateProvider: PosterTemplateProvider
    @MockK lateinit var traceLocationRepository: TraceLocationRepository
    @MockK lateinit var fileSharing: FileSharing
    @MockK lateinit var qrCodeBitmap: Bitmap
    @MockK lateinit var templateBitmap: Bitmap
    @MockK lateinit var textBox: QRCodeTextBoxAndroid
    @MockK lateinit var traceLocation: TraceLocation
    @MockK lateinit var appConfigProvider: AppConfigProvider
    private lateinit var template: Template

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        template = Template(
            bitmap = templateBitmap,
            width = 500,
            height = 600,
            offsetX = 0.15f,
            offsetY = 0.14f,
            qrCodeLength = 500,
            textBox = textBox
        )

        coEvery { qrCodeGenerator.createQrCode("locationUrl", any(), any(), any(), any()) } returns qrCodeBitmap
        coEvery { posterTemplateProvider.template() } returns template
        coEvery { traceLocationRepository.traceLocationForId(any()) } returns traceLocation.apply {
            every { description } returns "description"
            every { address } returns "address"
            every { locationUrl } returns "locationUrl"
        }

        coEvery { appConfigProvider.currentConfig } returns flowOf(
            mockk<ConfigData>().apply {
                every { presenceTracing } returns mockk<PresenceTracingConfig>().apply {
                    every { qrCodeErrorCorrectionLevel } returns ErrorCorrectionLevel.M
                }
            }
        )
    }

    @Test
    fun `Poster is requested in init`() {
        createInstance().poster.getOrAwaitValue() shouldBe Poster(
            qrCode = qrCodeBitmap,
            template = template,
            infoText = "description\naddress"
        )
    }

    private fun createInstance() = QrCodePosterViewModel(
        traceLocationId = 1,
        dispatcher = TestDispatcherProvider(),
        qrCodeGenerator = qrCodeGenerator,
        posterTemplateProvider = posterTemplateProvider,
        traceLocationRepository = traceLocationRepository,
        fileSharing = fileSharing,
        appConfigProvider = appConfigProvider
    )
}
