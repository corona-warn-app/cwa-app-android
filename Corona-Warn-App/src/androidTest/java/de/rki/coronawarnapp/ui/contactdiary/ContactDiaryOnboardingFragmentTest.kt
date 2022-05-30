package de.rki.coronawarnapp.ui.contactdiary

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryUiSettings
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragment
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragmentArgs
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragmentViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class ContactDiaryOnboardingFragmentTest : BaseUITest() {

    @RelaxedMockK lateinit var settings: ContactDiaryUiSettings

    private val fragmentArgs = ContactDiaryOnboardingFragmentArgs(
        showBottomNav = false
    ).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : ContactDiaryOnboardingFragmentViewModel.Factory {
                override fun create(): ContactDiaryOnboardingFragmentViewModel =
                    ContactDiaryOnboardingFragmentViewModel(
                        dispatcherProvider = TestDispatcherProvider(),
                        settings = settings
                    )
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<ContactDiaryOnboardingFragment>(fragmentArgs)
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<ContactDiaryOnboardingFragment>(fragmentArgs)
        takeScreenshot<ContactDiaryOnboardingFragment>()
    }
}

@Module
abstract class ContactDiaryOnboardingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun contactDiaryOnboardingFragment(): ContactDiaryOnboardingFragment
}
