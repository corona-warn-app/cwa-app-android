package de.rki.coronawarnapp.util

import android.content.Context
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import de.rki.coronawarnapp.util.screenshots.ScreenshotCaptureBlockable

/**
 * Before the fragment is attached, any Fragment with a marker of [ScreenshotCaptureBlockable]
 * will set the FLAG_SECURE flag. Please note that once this flag is set, you won't be able to
 * revert unless the activity is recreated.
 */
class ActivityScreenshotHelper(private val flavor: String) :
    FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
        if (f is ScreenshotCaptureBlockable) {
            f.disableScreenshots()
        }
    }

    private fun Fragment.disableScreenshots() {
        // prevents screenshot except for deviceForTesters build flavor, which is used for testing
        if (flavor != "deviceForTesters") {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }
}
