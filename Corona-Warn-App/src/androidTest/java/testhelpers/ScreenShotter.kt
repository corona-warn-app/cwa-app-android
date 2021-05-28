package testhelpers

import android.graphics.Bitmap
import android.util.Log
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
    Screengrab.screenshot(
        name,
        UiAutomatorScreenshotStrategy(),
        SDCardCallback
    )
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
