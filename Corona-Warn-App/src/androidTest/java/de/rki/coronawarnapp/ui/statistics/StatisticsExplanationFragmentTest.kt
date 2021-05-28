package de.rki.coronawarnapp.ui.statistics

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.statistics.ui.StatisticsExplanationFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchInEmptyActivity
import testhelpers.takeScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class StatisticsExplanationFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchInEmptyActivity<StatisticsExplanationFragment>()
        takeScreenshot<StatisticsExplanationFragment>()
    }
}

@Module
abstract class StatisticsExplanationFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun statisticsExplanationFragmentTest(): StatisticsExplanationFragmentTest
}
