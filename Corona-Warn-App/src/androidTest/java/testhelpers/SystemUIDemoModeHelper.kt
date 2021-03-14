package testhelpers

import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * System UI Demo Mode helper that uses instrumentation commands to clean device status bar.
 * @see <a href="https://android.googlesource.com/platform/frameworks/base/+/master/packages/SystemUI/docs/demo_mode.md" >Demo mode</a>
 * @see <a href="https://developer.android.com/studio/debug/dev-options#general" >Dev options</a>
 */
class SystemUIDemoModeHelper {

    /**
     * Allows System UI demo mode and configure how the status bar should look like
     */
    fun enter() {
        executeShellCommand("settings put global sysui_demo_allowed 1")
        sendCommand("exit")
        sendCommand("enter")
        sendCommand("notifications", "visible" to "false")
        sendCommand("network", "wifi" to "show", "level" to "4", "fully" to "true")
        sendCommand("battery", "level" to "100", "plugged" to "false")
        sendCommand("clock", "hhmm" to "1000")
    }

    /**
     * Exist system UI demo mode
     */
    fun exit() {
        sendCommand("exit")
    }

    private fun sendCommand(command: String, vararg extras: Pair<String, Any>) {
        val exec = StringBuilder("am broadcast -a com.android.systemui.demo -e command $command")
        for ((key, value) in extras) {
            exec.append(" -e $key $value")
        }
        executeShellCommand(exec.toString())
    }

    private fun executeShellCommand(command: String) {
        waitForCompletion(InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command))
    }

    private fun waitForCompletion(descriptor: ParcelFileDescriptor) {
        BufferedReader(
            InputStreamReader(
                ParcelFileDescriptor.AutoCloseInputStream(descriptor)
            )
        ).use {
            it.readText()
        }
    }
}
