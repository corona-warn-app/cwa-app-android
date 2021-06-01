package testhelpers

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.espresso.ViewAction
import androidx.test.platform.app.InstrumentationRegistry
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
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
    val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
    SDCard.screenshotCaptured(name ?: System.currentTimeMillis().toString(), screenshot)
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
object SDCard {
    private const val SDCARD_DIRECTORY = "/sdcard"
    private const val DATA_DIRECTORY = "/data/data/${BuildConfig.APPLICATION_ID}"
    private const val SCREENSHOTS_DIRECTORY = "screenshots"
    private const val SCREENSHOT_FORMAT = ".png"
    private const val IMAGE_QUALITY = 100

    fun screenshotCaptured(screenshotName: String, screenshot: Bitmap) {
        try {
            val directory = File(rootDir, SCREENSHOTS_DIRECTORY)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val screenshotFile = File(directory, screenshotName + SCREENSHOT_FORMAT)
            if (!screenshotFile.exists()) {
                screenshotFile.createNewFile()
            }

            BufferedOutputStream(FileOutputStream(screenshotFile)).use {
                screenshot.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, it)
                screenshot.recycle()
            }
            Log.d("SDCard", "Captured screenshot \"${screenshotFile.name}\"")
        } catch (e: Exception) {
            throw RuntimeException("Unable to capture screenshot.", e)
        }
    }

    private val rootDir: String by lazy {
        // Screenshots are saved in local directory on API 30+ due to scoped storage changes.
        // Developer can explore screenshots taken locally using "Device File Explorer" in Android studio.
        // Firebase TL  runs screenshots on API 29 and pulls them from sdcard.
        if (Build.VERSION.SDK_INT < 30) SDCARD_DIRECTORY else DATA_DIRECTORY
    }
}
