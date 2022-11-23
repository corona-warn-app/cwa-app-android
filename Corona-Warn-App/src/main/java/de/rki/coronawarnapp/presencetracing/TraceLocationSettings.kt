package de.rki.coronawarnapp.presencetracing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import de.rki.coronawarnapp.util.datastore.clear
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceLocationSettings @Inject constructor(
    @LocationSettingsDataStore private val dataStore: DataStore<Preferences>
) : Resettable {

    val onboardingStatus = dataStore.dataRecovering.distinctUntilChanged(
        key = PKEY_ONBOARDING_STATUS, defaultValue = OnboardingStatus.NOT_ONBOARDED.order
    ).map { value ->
        OnboardingStatus.values().find { it.order == value } ?: OnboardingStatus.NOT_ONBOARDED
    }

    suspend fun updateOnboardingStatus(value: OnboardingStatus) =
        dataStore.trySetValue(preferencesKey = PKEY_ONBOARDING_STATUS, value = value.order)

    inline val isOnboardingDoneFlow: Flow<Boolean>
        get() = onboardingStatus.map { it == OnboardingStatus.ONBOARDED_2_0 }

    suspend fun isOnboardingDone() = onboardingStatus.first() == OnboardingStatus.ONBOARDED_2_0

    override suspend fun reset() {
        Timber.d("reset()")
        dataStore.clear()
    }

    enum class OnboardingStatus(val order: Int) {
        NOT_ONBOARDED(0),
        ONBOARDED_2_0(1)
    }

    companion object {
        private val PKEY_ONBOARDING_STATUS = intPreferencesKey("trace_location_onboardingstatus")
    }
}
