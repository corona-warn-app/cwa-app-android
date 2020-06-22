package de.rki.coronawarnapp.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import de.rki.coronawarnapp.test.TestActivity
import de.rki.coronawarnapp.util.screenshots.ScreenshotCaptureBlockable
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActivityScreenshotHelperTest {

    @Test
    fun `flag secure is set when fragment is attached`() {
        val activityScreenshotHelper = ActivityScreenshotHelper("device")

        var flagSecureExists = false
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.setupFragment(activityScreenshotHelper)

            scenario.onActivity { activity ->
                flagSecureExists = activity.window.flagSecureExists()
            }
        }

        Assert.assertEquals(true, flagSecureExists)
    }

    @Test
    fun `flag secure is not set when flavor is deviceForTesters`() {
        val activityScreenshotHelper = ActivityScreenshotHelper("deviceForTesters")

        var flagSecureExists = false
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.setupFragment(activityScreenshotHelper)

            scenario.onActivity { activity ->
                flagSecureExists = activity.window.flagSecureExists()
            }
        }

        Assert.assertEquals(false, flagSecureExists)
    }

    @Test
    fun `flag secure is cleared when fragment is destroyed`() {
        val activityScreenshotHelper = ActivityScreenshotHelper("device")

        var flagSecureExists = false
        ActivityScenario.launch(TestActivity::class.java).use { scenario ->
            scenario.setupFragment(activityScreenshotHelper)

            scenario.onActivity { activity ->
                flagSecureExists = activity.window.flagSecureExists()
            }

            // Assert that flag is set after the fragment got added.
            Assert.assertEquals(true, flagSecureExists)

            // Now remove the fragment to call onDestroy
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                activity.supportFragmentManager.beginTransaction()
                    .remove(fragment!!)
                    .commitNow()
                flagSecureExists = activity.window.flagSecureExists()
            }

            // The flag should not be set any more.
            Assert.assertEquals(false, flagSecureExists)
        }
    }

    private fun Window.flagSecureExists(): Boolean =
        (attributes.flags and WindowManager.LayoutParams.FLAG_SECURE) == WindowManager.LayoutParams.FLAG_SECURE

    private fun ActivityScenario<TestActivity>.setupFragment(
        activityScreenshotHelper: ActivityScreenshotHelper
    ) {
        onActivity { activity ->
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                activityScreenshotHelper,
                true
            )
            val fragment = TestScreenshotFragment()
            activity.addFragment(fragment, tag = FRAGMENT_TAG)
            Assert.assertEquals(true, fragment.isAdded)
        }
    }

    internal class TestScreenshotFragment : Fragment(), ScreenshotCaptureBlockable {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? = View(requireContext())
    }

    private companion object {
        private const val FRAGMENT_TAG = "fragment_tag"
    }
}
