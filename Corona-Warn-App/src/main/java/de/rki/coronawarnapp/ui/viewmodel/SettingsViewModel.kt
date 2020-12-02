package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.LiveData
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

/**
 * ViewModel for everything settings related.
 *
 * @see SettingsRepository
 */
class SettingsViewModel @Inject constructor() : CWAViewModel() {

    private val settingsRepository by lazy {
        AppInjector.component.settingsRepository
    }

    val isBackgroundPriorityEnabled: LiveData<Boolean> = settingsRepository.isBackgroundPriorityEnabled

    /**
     * Update connection enabled
     *
     * @param value
     */
    fun updateConnectionEnabled(value: Boolean) {
        settingsRepository.updateConnectionEnabled(value)
    }

    fun refreshBackgroundPriorityEnabled() {
        settingsRepository.refreshBackgroundPriorityEnabled()
    }
}
