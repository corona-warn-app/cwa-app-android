package de.rki.coronawarnapp.storage

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import org.joda.time.Instant
import javax.inject.Inject

class OnboardingData @Inject constructor(
    @AppContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("onboarding_localdata", Context.MODE_PRIVATE)
    }

    val isOnboarded = prefs.createFlowPreference(
        key = IS_ONBOARDED,
        defaultValue = false
    )

    val onboardingCompletedTimestamp = prefs.createFlowPreference(
        key = ONBOARDING_COMPLETED_TIMESTAMP,
        reader = { key ->
            getLong(key, 0L).let {
                if (it != 0L) {
                    Instant.ofEpochMilli(it)
                } else null
            }
        },
        writer = { key, value ->
            putLong(key, value?.millis ?: 0L)
        }
    )

    val isBackgroundCheckDone = prefs.createFlowPreference(
        key = BACKGROUND_CHECK_DONE,
        defaultValue = false
    )

    fun clear() = prefs.clearAndNotify()

    companion object {
        private const val IS_ONBOARDED = "onboarding.done"
        private const val ONBOARDING_COMPLETED_TIMESTAMP = "onboarding.done.timestamp"
        private const val BACKGROUND_CHECK_DONE = "onboarding.background.checked"
    }
}
