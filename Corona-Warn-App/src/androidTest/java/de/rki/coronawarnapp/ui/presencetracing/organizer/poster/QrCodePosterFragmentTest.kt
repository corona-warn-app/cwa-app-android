package de.rki.coronawarnapp.ui.presencetracing.organizer.poster

import android.content.Context
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfig
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.PosterTemplateProvider
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.Template
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.QrCodePosterTemplate.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid
import de.rki.coronawarnapp.ui.eventregistration.organizer.TraceLocationData
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragmentArgs
import de.rki.coronawarnapp.util.files.FileSharing
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class QrCodePosterFragmentTest : BaseUITest() {

    @MockK lateinit var posterTemplateProvider: PosterTemplateProvider
    @MockK private lateinit var appConfigProvider: AppConfigProvider
    @MockK private lateinit var traceLocationRepository: TraceLocationRepository
    @MockK lateinit var fileSharing: FileSharing
    @MockK lateinit var context: Context

    @MockK lateinit var templateBitmap: Bitmap
    @MockK lateinit var textBox: QRCodeTextBoxAndroid
    private lateinit var mockTemplate: Template

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        mockTemplate = Template(
            bitmap = templateBitmap,
            width = 500,
            height = 600,
            offsetX = 0.15f,
            offsetY = 0.14f,
            qrCodeLength = 500,
            textBox = textBox
        )

        coEvery { posterTemplateProvider.template() } returns mockTemplate

        every { fileSharing.getFileIntentProvider(any(), any(), any()) } returns mockk()
        coEvery { traceLocationRepository.traceLocationForId(1) } returns TraceLocationData.traceLocationSameDate
        coEvery { traceLocationRepository.traceLocationForId(2) } returns TraceLocationData.traceLocationDifferentDate
        coEvery { appConfigProvider.currentConfig } returns flowOf(
            mockk<ConfigData>().apply {
                every { presenceTracing } returns mockk<PresenceTracingConfig>().apply {
                    every { qrCodeErrorCorrectionLevel } returns ErrorCorrectionLevel.M
                }
            }
        )

        setupMockViewModel(
            object : QrCodePosterViewModel.Factory {
                override fun create(traceLocationId: Long): QrCodePosterViewModel {
                    return createViewModel(traceLocationId)
                }
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Screenshot
    @Test
    fun screenshot() {
        launchFragmentInContainer2<QrCodePosterFragment>(
            fragmentArgs = QrCodeDetailFragmentArgs(
                traceLocationId = 1
            ).toBundle()
        )

        takeScreenshot<QrCodePosterFragment>()
    }

    private fun createViewModel(traceLocationId: Long) =
        QrCodePosterViewModel(
            traceLocationId = traceLocationId,
            dispatcher = TestDispatcherProvider(),
            posterTemplateProvider = posterTemplateProvider,
            traceLocationRepository = traceLocationRepository,
            appConfigProvider = appConfigProvider,
            fileSharing = fileSharing,
            context = context
        )
}

@Module
abstract class QrCodePosterFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun qrCodePosterFragment(): QrCodePosterFragment
}
