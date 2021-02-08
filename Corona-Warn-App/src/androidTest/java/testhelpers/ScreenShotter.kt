package testhelpers

import android.app.Activity
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.espresso.ViewAction
import de.rki.coronawarnapp.R
import tools.fastlane.screengrab.Screengrab

/**
 * Waits for 2 sec and captures a screenshot
 * @param suffix [String] Screenshots file name suffix, default name:[T::class.simpleName]
 * @param delay [Long] delay time before capturing, default 2 sec
 */
inline fun <reified T> takeScreenshot(suffix: String = "", delay: Long = SCREENSHOT_DELAY_TIME) {
    Thread.sleep(delay)
    val simpleName = T::class.simpleName
    val name = if (suffix.isEmpty()) simpleName else simpleName.plus("_$suffix")
    Screengrab.screenshot(name)
}

/**
 * Launches and captures a screen [Fragment].
 * This function is not convenient if Espresso [ViewAction] is required
 * before taking screenshot or the screen is [Activity].
 * Better to use [takeScreenshot]
 */
inline fun <reified F : Fragment> captureScreenshot(
    suffix: String = "",
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.AppTheme,
    factory: FragmentFactory? = null
) {
    launchFragmentInContainer2<F>(fragmentArgs, themeResId, factory)
    takeScreenshot<F>(suffix)
}
