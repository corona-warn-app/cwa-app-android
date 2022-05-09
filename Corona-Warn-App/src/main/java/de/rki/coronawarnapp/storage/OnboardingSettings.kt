package de.rki.coronawarnapp.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingSettings @Inject constructor(
    @AppContext private val context: Context
) : Resettable {
    private val prefs by lazy {
        context.getSharedPreferences("onboarding_localdata", Context.MODE_PRIVATE)
    }

    val onboardingCompletedTimestamp: FlowPreference<Instant?> = prefs.createFlowPreference(
        key = ONBOARDING_COMPLETED_TIMESTAMP,
        reader = {
            val raw = getLong(it, 0L)
            if (raw != 0L) {
                Instant.ofEpochMilli(raw)
            } else null
        },
        writer = { key, value ->
            putLong(key, value?.millis ?: 0L)
        }
    )

    val isOnboarded: Boolean
        get() = onboardingCompletedTimestamp.value != null

    val isOnboardedFlow: Flow<Boolean>
        get() = onboardingCompletedTimestamp.flow.map { it != null }

    val fabScannerOnboardingDone = prefs.createFlowPreference(
        key = ONBOARDING_FAB_SCANNER_DONE,
        defaultValue = false
    )

    var isBackgroundCheckDone: Boolean
        get() = prefs.getBoolean(BACKGROUND_CHECK_DONE, false)
        set(value) = prefs.edit { putBoolean(BACKGROUND_CHECK_DONE, value) }

    override suspend fun reset() {
        Timber.d("reset()")
        prefs.clearAndNotify()
    }

    companion object {
        private const val ONBOARDING_COMPLETED_TIMESTAMP = "onboarding.done.timestamp"
        private const val BACKGROUND_CHECK_DONE = "onboarding.background.checked"
        private const val ONBOARDING_FAB_SCANNER_DONE = "onboarding.fab.scanner.done"
    }
}
