package de.rki.coronawarnapp.util

import android.annotation.SuppressLint
import android.content.Context
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.security.SecurityHelper
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * Helper for supplying functionality regarding Data Retention
 */
object DataRetentionHelper {
    private val TAG: String? = DataRetentionHelper::class.simpleName

    /**
     * Deletes all data known to the Application
     *
     */
    @SuppressLint("ApplySharedPref") // We need a commit here to ensure consistency
    fun clearAllLocalData(context: Context) {
        Timber.w("CWA LOCAL DATA DELETION INITIATED.")
        // Database Reset
        AppDatabase.reset(context)
        // Shared Preferences Reset
        SecurityHelper.resetSharedPrefs()
        // Reset the current risk level stored in LiveData
        RiskLevelRepository.reset()
        // Export File Reset
        // TODO runBlocking, but also all of the above is BLOCKING and should be called more nicely
        runBlocking { AppInjector.component.keyCacheRepository.clear() }
        Timber.w("CWA LOCAL DATA DELETION COMPLETED.")
    }
}
