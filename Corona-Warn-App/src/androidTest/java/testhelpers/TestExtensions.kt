package testhelpers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.util.Preconditions
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.FakeEmptyActivity
import de.rki.coronawarnapp.ui.main.FakeMainActivity
import de.rki.coronawarnapp.ui.main.MainActivity
import java.util.Locale

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
    testNavHostController: TestNavHostController? = null
) = launchInEmptyActivity<F>(fragmentArgs, testNavHostController)
// TODO FragmentScenario.launchInContainer(F::class.java, fragmentArgs, themeResId, factory)
// when https://issuetracker.google.com/issues/121347222 is fixed

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
 * takeScreenshot<HomeFragment>()
 * ```
 */
inline fun <reified F : Fragment> launchInMainActivity(
    fragmentArgs: Bundle? = null,
    testNavHostController: TestNavHostController? = null
): ActivityScenario<FakeMainActivity> {
    val startActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            FakeMainActivity::class.java
        )
    ).putExtra(EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY, R.style.AppTheme_Main)

    return ActivityScenario.launch<FakeMainActivity>(startActivityIntent).onActivity { activity ->
        val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            Preconditions.checkNotNull(F::class.java.classLoader),
            F::class.java.name
        )
        fragment.arguments = fragmentArgs
        fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
            if (viewLifecycleOwner != null) {
                testNavHostController?.let {
                    Navigation.setViewNavController(fragment.requireView(), it)
                }
            }
        }
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commitNow()
    }
}

/**
 * Helper to launch a Fragment in [FakeEmptyActivity].
 * this helps to test each fragment in isolation from other fragments specially
 * in [Screenshot] tests.
 *
 * Note: In cases where screenshots are not required, it is better to use testing framework launcher.
 * If a [BottomNavigationView] should be visible in the screenshot, you should use [launchInMainActivity].
 *
 * This helper was introduced to be used instead of [launchFragment2] or [launchFragmentInContainer2] in screenshot
 * tests, because we had some issues with [EmptyFragmentActivity] that was used with these function was no AppCompat
 * Activity and therefore some Images in ImageViews didn't show up in the screenshot. This helper now loads the
 * fragments in an [AppCompatActivity]
 *
 * Example:
 * ```
 * launchInEmptyActivity<OnboardingFragment>()
 * // Do espresso actions
 * takeScreenshot<OnboardingFragment>()
 * ```
 */
inline fun <reified F : Fragment> launchInEmptyActivity(
    fragmentArgs: Bundle? = null,
    testNavHostController: TestNavHostController? = null
): ActivityScenario<FakeEmptyActivity> {
    val startActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            FakeEmptyActivity::class.java
        )
    ).putExtra(EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY, R.style.AppTheme_Main)

    return ActivityScenario.launch<FakeEmptyActivity>(startActivityIntent).onActivity { activity ->
        val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            Preconditions.checkNotNull(F::class.java.classLoader),
            F::class.java.name
        )
        fragment.arguments = fragmentArgs
        fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
            if (viewLifecycleOwner != null) {
                testNavHostController?.let {
                    Navigation.setViewNavController(fragment.requireView(), it)
                }
            }
        }
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commitNow()
    }
}

/**
 * Helper to get a string for a specific locale
 */
fun stringForLocale(locale: Locale, @StringRes stringRes: Int): String {
    val ctx: Context = ApplicationProvider.getApplicationContext()
    val conf = ctx.resources.configuration
        .let { Configuration(it) }
        .apply { setLocale(locale) }
    return ctx.createConfigurationContext(conf).getString(stringRes)
}
