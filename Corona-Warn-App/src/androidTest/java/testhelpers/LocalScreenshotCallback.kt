package testhelpers

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.util.Log
import tools.fastlane.screengrab.ScreenshotCallback
import tools.fastlane.screengrab.file.Chmod
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

object LocalScreenshotCallback : ScreenshotCallback {
    private const val ROOT_DIRECTORY = "/data/local/tmp"
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
                screenshot.compress(CompressFormat.PNG, IMAGE_QUALITY, it)
                Chmod.chmodPlusR(screenshotFile)
                screenshot.recycle()
            }
            Log.d("Screengrab", "Captured screenshot \"${screenshotFile.name}\"")
        } catch (e: Exception) {
            throw RuntimeException("Unable to capture screenshot.", e)
        }
    }
}
