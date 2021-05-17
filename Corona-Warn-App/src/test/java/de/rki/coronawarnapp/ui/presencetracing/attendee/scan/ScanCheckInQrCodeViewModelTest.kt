package de.rki.coronawarnapp.ui.presencetracing.attendee.scan

import com.journeyapps.barcodescanner.BarcodeResult
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QRCodeUriParser
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationVerifier
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.presencetracing.attendee.scan.ScanCheckInQrCodeNavigation.ScanResultNavigation
import de.rki.coronawarnapp.util.permission.CameraSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
class ScanCheckInQrCodeViewModelTest : BaseTest() {

    private lateinit var viewModel: ScanCheckInQrCodeViewModel
    @MockK lateinit var qrCodeUriParser: QRCodeUriParser
    @MockK lateinit var cameraSettings: CameraSettings
    @MockK lateinit var traceLocationVerifier: TraceLocationVerifier

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = ScanCheckInQrCodeViewModel(
            qrCodeUriParser,
            cameraSettings,
            traceLocationVerifier
        )
    }

    @Test
    fun `onNavigateUp goes back`() {
        viewModel.onNavigateUp()
        viewModel.events.getOrAwaitValue() shouldBe ScanCheckInQrCodeNavigation.BackNavigation
    }

    @Test
    fun `onScanResult results in navigation url`() = runBlockingTest {
        val codeContent = "https://coronawarn.app/E1/SOME_PATH_GOES_HERE"
        val expectedOutcome: ScanCheckInQrCodeNavigation = ScanResultNavigation(codeContent)
        val validationPassed = mockk<TraceLocationVerifier.VerificationResult.Valid>()
        val mockedResult = mockk<BarcodeResult> {
            every { result } returns mockk {
                every { text } returns codeContent
            }
        }
        val qrCodePayload = mockk<TraceLocationOuterClass.QRCodePayload>()
        coEvery { qrCodeUriParser.getQrCodePayload(any()) } returns qrCodePayload
        every { traceLocationVerifier.verifyTraceLocation(qrCodePayload) } returns validationPassed

        viewModel.onScanResult(mockedResult)
        viewModel.events.getOrAwaitValue() shouldBe expectedOutcome
    }

    @Test
    fun `Camera settings is saved when user denies it`() {
        every { cameraSettings.isCameraDeniedPermanently } returns mockFlowPreference(false)
        viewModel.setCameraDeniedPermanently(true)

        verify { cameraSettings.isCameraDeniedPermanently }
    }
}
