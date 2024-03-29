package de.rki.coronawarnapp.ui.presencetracing.organizer.poster

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragmentArgs
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class QrCodePosterFragmentTest : BaseUITest() {

    @MockK private lateinit var viewModel: QrCodePosterViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { viewModel.poster } returns MutableLiveData(
            Poster(
                qrCode = null,
                template = null,
                infoText = "Some Info Text"
            )
        )

        setupMockViewModel(
            object : QrCodePosterViewModel.Factory {
                override fun create(traceLocationId: Long): QrCodePosterViewModel = viewModel
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
}

@Module
abstract class QrCodePosterFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun qrCodePosterFragment(): QrCodePosterFragment
}
