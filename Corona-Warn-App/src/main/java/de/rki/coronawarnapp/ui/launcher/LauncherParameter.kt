package de.rki.coronawarnapp.ui.launcher

sealed class LauncherParameter {
    data class EnvironmentKey(val value: String) : LauncherParameter()
    data class Base64Environment(val value: String) : LauncherParameter()
}
