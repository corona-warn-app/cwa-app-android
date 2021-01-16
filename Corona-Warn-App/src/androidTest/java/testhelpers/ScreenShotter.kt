package testhelpers

import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy

object ScreenShotter {

    inline fun <reified T> takeScreenshot(suffix: String = "") {
        val contentResolver =
            InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val testLabSetting = Settings.System.getString(contentResolver, "firebase.test.lab")

        val screenshotName = T::class.simpleName + suffix
        if ("true" == testLabSetting) {
            Screengrab.screenshot(
                screenshotName,
                UiAutomatorScreenshotStrategy(),
                SDCardScreenshotCallback
            )
        } else {
            Screengrab.screenshot(screenshotName)
        }
    }
}


