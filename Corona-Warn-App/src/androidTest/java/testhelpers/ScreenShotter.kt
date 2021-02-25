package testhelpers

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.espresso.ViewAction
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import de.rki.coronawarnapp.R
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.ScreenshotCallback
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.file.Chmod
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Waits for 2 sec and captures a screenshot
 * @param suffix [String] Screenshots file name suffix, default name:[T::class.simpleName]
 * @param delay [Long] delay time before capturing, default 2 sec
 */
inline fun <reified T> takeScreenshot(suffix: String = "", delay: Long = SCREENSHOT_DELAY_TIME) {
    Thread.sleep(delay)
    val simpleName = T::class.simpleName
    val name = if (suffix.isEmpty()) simpleName else simpleName.plus("_$suffix")

    val contentResolver = getInstrumentation().targetContext.contentResolver
    val testLabSetting = Settings.System.getString(contentResolver, "firebase.test.lab")
    val androidStudioMode = InstrumentationRegistry.getArguments().getString("androidStudioMode")
    if ("true" in listOf(testLabSetting, androidStudioMode)) {
        Screengrab.screenshot(
            name,
            UiAutomatorScreenshotStrategy(),
            SDCardCallback
        )
    } else {
        Screengrab.screenshot(name)
    }
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

/**
 * Saves screenshots on the device's sdcard
 */
object SDCardCallback : ScreenshotCallback {
    private const val ROOT_DIRECTORY = "/sdcard"
    private const val SCREENSHOTS_DIRECTORY = "screenshots"
    private const val SCREENSHOT_FORMAT = ".png"
    private const val IMAGE_QUALITY = 100

    override fun screenshotCaptured(screenshotName: String, screenshot: Bitmap) {
        try {
            val directory = File(ROOT_DIRECTORY, SCREENSHOTS_DIRECTORY)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val screenshotFile = File(directory, screenshotName + SCREENSHOT_FORMAT)
            if (!screenshotFile.exists()) {
                screenshotFile.createNewFile()
            }

            BufferedOutputStream(FileOutputStream(screenshotFile)).use {
                screenshot.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, it)
                Chmod.chmodPlusR(screenshotFile)
                screenshot.recycle()
            }
            Log.d("Screengrab", "Captured screenshot \"${screenshotFile.name}\"")
        } catch (e: Exception) {
            throw RuntimeException("Unable to capture screenshot.", e)
        }
    }
}
