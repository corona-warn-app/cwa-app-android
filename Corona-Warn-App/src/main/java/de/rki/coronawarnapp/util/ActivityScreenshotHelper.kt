package de.rki.coronawarnapp.util

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import de.rki.coronawarnapp.util.screenshots.ScreenshotCaptureBlockable

/**
 * Before the fragment is attached, any Fragment with a marker of [ScreenshotCaptureBlockable]
 * will set the FLAG_SECURE flag.
 */
class ActivityScreenshotHelper(private val flavor: String) :
    FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        if (f is ScreenshotCaptureBlockable) {
            f.disableScreenshots()
        }
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentDestroyed(fm, f)
        if (f is ScreenshotCaptureBlockable) {
            f.requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
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
