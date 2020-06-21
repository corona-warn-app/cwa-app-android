package de.rki.coronawarnapp.util

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import de.rki.coronawarnapp.storage.SettingsRepository

object UiThemeHelper {

    private fun mapOptionToConstant(value: Int) : Int {
        return when (value) {
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
    }

    fun applyUiTheme() {
        SettingsRepository.refreshUiThemeSetting()
        SettingsRepository.uiThemeSetting.value?.let {
                value -> AppCompatDelegate.setDefaultNightMode(mapOptionToConstant(value))
        }
    }
}