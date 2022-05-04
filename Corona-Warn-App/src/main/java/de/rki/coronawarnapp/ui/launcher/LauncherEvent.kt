package de.rki.coronawarnapp.ui.launcher

import android.app.Activity

sealed class LauncherEvent {
    object GoToOnboarding : LauncherEvent()
    object GoToMainActivity : LauncherEvent()
    data class ForceUpdate(
        val forceUpdate: (Activity) -> Unit
    ) : LauncherEvent()

    object ShowUpdateDialog : LauncherEvent()

    object ShowRootedDialog : LauncherEvent()

    object RestartApp : LauncherEvent()
}
