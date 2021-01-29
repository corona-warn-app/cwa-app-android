package testhelpers

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import de.rki.coronawarnapp.R
import tools.fastlane.screengrab.Screengrab

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

inline fun <reified F : Fragment> captureScreenshot(
    suffix: String = "",
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.AppTheme,
    factory: FragmentFactory? = null
) {
    val name = F::class.simpleName.plus(suffix)
    launchFragmentInContainer2<F>(fragmentArgs, themeResId, factory)
    Thread.sleep(SCREENSHOT_DELAY_TIME)
    Screengrab.screenshot(name)
}

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
