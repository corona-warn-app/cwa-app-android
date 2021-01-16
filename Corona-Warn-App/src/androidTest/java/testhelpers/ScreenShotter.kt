package testhelpers

import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy

object ScreenShotter {

    inline fun <reified T> capture(suffix: String = "") {
        val contentResolver =
            InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val testLabSetting = Settings.System.getString(contentResolver, "firebase.test.lab")

        val screenshotName = T::class.simpleName.plus(suffix)
        if ("true" == testLabSetting) {
            Screengrab.screenshot(
                screenshotName,
                UiAutomatorScreenshotStrategy(),
                LocalScreenshotCallback
            )
        } else {
            Screengrab.screenshot(screenshotName)
        }
    }
}
