package testhelpers

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.FakeMainActivity
import de.rki.coronawarnapp.ui.main.MainActivity

/** Delay time before taking screenshot
 */
const val SCREENSHOT_DELAY_TIME = 2000L

/**
 * Launches Fragment in Activity root container.
 * Same as [androidx.fragment.app.testing.launchFragmentInContainer] except that it defaults
 * the theme to [R.style.AppTheme].
 *
 * This helps to avoid showing:
 * 1- Default ActionBar from [R.style.FragmentScenarioEmptyFragmentActivityTheme]
 * 2- Some UI elements squeezed than it should be.
 * in cases where a screenshot is needed to match the exact theme in the App.
 */
inline fun <reified F : Fragment> launchFragmentInContainer2(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.AppTheme,
    factory: FragmentFactory? = null
) = FragmentScenario.launchInContainer(F::class.java, fragmentArgs, themeResId, factory)

/**
 * Launches Fragment in Activity.
 * Same as [androidx.fragment.app.testing.launchFragment] except that it defaults
 * the theme to [R.style.AppTheme].
 */
inline fun <reified F : Fragment> launchFragment2(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.AppTheme,
    factory: FragmentFactory? = null
) = FragmentScenario.launch(F::class.java, fragmentArgs, themeResId, factory)

/**
 * Helper to launch [MainActivity]'s Fragments in [FakeMainActivity].
 * this helps to test each fragment in isolation from other fragments specially
 * in [Screenshot] tests.
 *
 * Note: In cases where screenshots are not required, it is better to use testing framework launcher.
 * This helper is convenient for screenshots when [BottomNavigationView] should be visible in the screenshot.
 *
 * Example:
 * ```
 * launchInMainActivity<HomeFragment>()
 * // Do espresso actions
 * // takeScreenshot<HomeFragment>()
 * ```
 */
inline fun <reified F : Fragment> launchInMainActivity() {
    val intent = Intent(
        ApplicationProvider.getApplicationContext(),
        FakeMainActivity::class.java
    ).apply {
        putExtra(FakeMainActivity.FRAGMENT_CLASS, F::class.qualifiedName)
    }
    launchActivity<FakeMainActivity>(intent)
}
