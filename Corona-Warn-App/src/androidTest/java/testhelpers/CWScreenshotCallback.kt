package testhelpers

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.Environment
import android.util.Log
import tools.fastlane.screengrab.ScreenshotCallback
import tools.fastlane.screengrab.file.Chmod
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class CWScreenshotCallback : ScreenshotCallback {
    override fun screenshotCaptured(screenshotName: String, screenshot: Bitmap) {
        try {
            val directory =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)?.absolutePath.toString() , "screenshots")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val screenshotFile = File(directory, "$screenshotName.png")
            if (screenshotFile.exists().not()) screenshotFile.createNewFile()
            var fos: BufferedOutputStream? = null
            try {
                fos = BufferedOutputStream(FileOutputStream(screenshotFile))
                screenshot.compress(CompressFormat.PNG, 100, fos)
                Chmod.chmodPlusR(screenshotFile)
            } finally {
                screenshot.recycle()
                fos?.close()
            }
            Log.d("Screengrab", "Captured screenshot \"" + screenshotFile.name + "\"")
        } catch (e: Exception) {
            throw RuntimeException("Unable to capture screenshot.", e)
        }
    }
}
