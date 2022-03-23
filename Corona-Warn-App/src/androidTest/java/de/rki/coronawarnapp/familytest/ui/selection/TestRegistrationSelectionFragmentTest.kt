package de.rki.coronawarnapp.familytest.ui.selection

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.submission.SubmissionRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
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
class TestRegistrationSelectionFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository

    private val request = CoronaTestQRCode.PCR(qrCodeGUID = "qrCodeGUID", rawQrCode = "rawQrCode")
    private lateinit var viewModel:TestRegistrationSelectionViewModel

    private val fragmentArgs = TestRegistrationSelectionFragmentArgs(
        coronaTestQrCode = request
    ).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = TestRegistrationSelectionViewModel(
            TestDispatcherProvider(),
            submissionRepository,
            request
        )
        setupMockViewModel(
            object : TestRegistrationSelectionViewModel.Factory {
                override fun create(
                    coronaTestQRCode: CoronaTestQRCode
                ): TestRegistrationSelectionViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<TestRegistrationSelectionFragment>(fragmentArgs = fragmentArgs)
        takeScreenshot<TestRegistrationSelectionFragment>()
    }
}

@Module
abstract class TestRegistrationSelectionFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun testRegistrationSelectionScreen(): TestRegistrationSelectionFragment
}
