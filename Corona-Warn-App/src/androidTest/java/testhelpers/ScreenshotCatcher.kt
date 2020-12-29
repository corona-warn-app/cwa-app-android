package testhelpers

import android.graphics.Bitmap
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.runner.screenshot.Screenshot
import de.rki.coronawarnapp.R

import timber.log.Timber
import java.io.IOException
import java.util.Locale

/**
 * Captures a screenshot for the required fragment.
 * it launches Fragment in container using `launchFragmentInContainer`
 * then captures the screenshot in `FragmentScenario.onFragment`
 * to avoid capturing a blank screenshot
 */
inline fun <reified F : Fragment> captureScreenshot(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.AppTheme,
    factory: FragmentFactory? = null
): FragmentScenario<F> {
    val launchFragmentInContainer = launchFragmentInContainer<F>(fragmentArgs, themeResId, factory)
    launchFragmentInContainer.onFragment {
        val language = Locale.getDefault().language
        capture("${F::class.simpleName}-$language")
    }
    return launchFragmentInContainer
}

/**
 * Captures a screenshot.
 * @param screenshotName [String]
 */
fun capture(screenshotName: String) {

    try {
        Screenshot.capture().apply {
            format = Bitmap.CompressFormat.PNG
            name = screenshotName
            process()
        }
    } catch (e: IOException) {
        Timber.e(e)
    }
}
