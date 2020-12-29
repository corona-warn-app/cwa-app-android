package de.rki.coronawarnapp.ui.onboarding.screenshot

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import de.rki.coronawarnapp.ui.onboarding.OnboardingTestFragment
import de.rki.coronawarnapp.ui.onboarding.OnboardingTestViewModel
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
class OnboardingTestFragmentScreenshot : BaseUITest() {

    @MockK lateinit var viewModel: OnboardingTestViewModel

    @get:Rule val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : OnboardingTestViewModel.Factory {
            override fun create(): OnboardingTestViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun capture_screenshot() {
        captureScreenshot<OnboardingTestFragment>()
    }
}
