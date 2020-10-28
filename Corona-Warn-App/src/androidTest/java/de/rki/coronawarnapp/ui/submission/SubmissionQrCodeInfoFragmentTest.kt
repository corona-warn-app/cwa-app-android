package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.qrcode.info.SubmissionQRCodeInfoFragment
import de.rki.coronawarnapp.ui.submission.qrcode.info.SubmissionQRCodeInfoFragmentViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class SubmissionQrCodeInfoFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionQRCodeInfoFragmentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(object : SubmissionQRCodeInfoFragmentViewModel.Factory {
            override fun create(): SubmissionQRCodeInfoFragmentViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionQRCodeInfoFragment>()
    }

    @Test fun testQRInfoNextClicked() {
        val scenario = launchFragmentInContainer<SubmissionQRCodeInfoFragment>()
        onView(withId(R.id.submission_qr_info_button_next))
            .perform(click())

        // TODO verify result
    }
}

@Module
abstract class SubmissionQRInfoFragmentModule {
    @ContributesAndroidInjector
    abstract fun submissionQRInfoScreen(): SubmissionQRCodeInfoFragment
}
