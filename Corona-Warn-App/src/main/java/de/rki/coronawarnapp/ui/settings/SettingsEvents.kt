package de.rki.coronawarnapp.ui.settings

sealed class SettingsEvents {
    object ResetApp: SettingsEvents()
    object GoBack: SettingsEvents()
}
