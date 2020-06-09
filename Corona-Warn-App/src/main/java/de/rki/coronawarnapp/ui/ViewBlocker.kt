package de.rki.coronawarnapp.ui

import android.os.Handler
import android.os.Looper
import android.view.View
import java.util.Timer
import kotlin.concurrent.schedule

object ViewBlocker {
    private const val DEFAULT_INTERVAL: Long = 500

    fun runAndBlockInteraction(
        views: Array<View>,
        delay: Long = DEFAULT_INTERVAL,
        runnable: () -> Unit?
    ) {
        views.forEach { it.isEnabled = false }
        val handler = Handler(Looper.getMainLooper())
        runnable()
        Timer().schedule(delay) {
            handler.post {
                views.forEach { it.isEnabled = true }
            }
        }
    }
}
