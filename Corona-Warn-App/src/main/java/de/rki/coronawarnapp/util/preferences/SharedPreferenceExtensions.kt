package de.rki.coronawarnapp.util.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import timber.log.Timber

fun SharedPreferences.clearAndNotify() {
    val currentKeys = this.all.keys.toSet()
    Timber.v("%s clearAndNotify(): %s", this, currentKeys)
    edit {
        currentKeys.forEach { remove(it) }
    }
    // Clear does not notify anyone using registerOnSharedPreferenceChangeListener
    edit(commit = true) {
        clear()
    }
}
