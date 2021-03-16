package de.rki.coronawarnapp.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingSettings @Inject constructor(
    @AppContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("onboarding_localdata", Context.MODE_PRIVATE)
    }

    var onboardingCompletedTimestamp: Instant?
        get() = prefs.getLong(ONBOARDING_COMPLETED_TIMESTAMP, 0L).let {
            if (it != 0L) {
                Instant.ofEpochMilli(it)
            } else null
        }
        set(value) = prefs.edit { putLong(ONBOARDING_COMPLETED_TIMESTAMP, value?.millis ?: 0L) }

    val isOnboarded: Boolean
        get() = onboardingCompletedTimestamp != null

    var isBackgroundCheckDone: Boolean
        get() = prefs.getBoolean(BACKGROUND_CHECK_DONE, false)
        set(value) = prefs.edit { putBoolean(BACKGROUND_CHECK_DONE, value) }

    fun clear() = prefs.clearAndNotify()

    companion object {
        private const val ONBOARDING_COMPLETED_TIMESTAMP = "onboarding.done.timestamp"
        private const val BACKGROUND_CHECK_DONE = "onboarding.background.checked"
    }
}
