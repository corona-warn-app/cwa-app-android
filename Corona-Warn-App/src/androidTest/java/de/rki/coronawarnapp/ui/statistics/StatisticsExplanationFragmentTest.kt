package de.rki.coronawarnapp.ui.statistics

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.statistics.ui.StatisticsExplanationFragment
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class StatisticsExplanationFragmentTest : BaseUITest() {

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<StatisticsExplanationFragment>()
        takeScreenshot<StatisticsExplanationFragment>()
    }
}

@Module
abstract class StatisticsExplanationFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun statisticsExplanationFragmentTest(): StatisticsExplanationFragmentTest
}
