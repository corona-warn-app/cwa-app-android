package de.rki.coronawarnapp.ui.launcher

import android.content.Intent

sealed class LauncherEvent {
    object GoToOnboarding : LauncherEvent()
    object GoToAppShortcutOrMainActivity : LauncherEvent()
    data class ShowUpdateDialog(
        val updateIntent: Intent
    ) : LauncherEvent()
}
