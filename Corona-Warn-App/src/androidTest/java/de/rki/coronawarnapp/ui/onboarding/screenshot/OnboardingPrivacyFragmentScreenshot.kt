package de.rki.coronawarnapp.ui.onboarding.screenshot

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import de.rki.coronawarnapp.ui.onboarding.OnboardingPrivacyFragment
import de.rki.coronawarnapp.ui.onboarding.OnboardingPrivacyViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.captureScreenshot

@RunWith(AndroidJUnit4::class)
class OnboardingPrivacyFragmentScreenshot : BaseUITest() {

    @MockK lateinit var viewModel: OnboardingPrivacyViewModel

    @get:Rule val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : OnboardingPrivacyViewModel.Factory {
            override fun create(): OnboardingPrivacyViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun capture_screenshot() {
        captureScreenshot<OnboardingPrivacyFragment>()
    }
}
