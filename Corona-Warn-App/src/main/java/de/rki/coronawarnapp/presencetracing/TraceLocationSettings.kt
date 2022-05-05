package de.rki.coronawarnapp.presencetracing

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceLocationSettings @Inject constructor(
    @AppContext val context: Context
) : Resettable {

    private val preferences by lazy {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    val onboardingStatus: FlowPreference<OnboardingStatus> = preferences.createFlowPreference(
        key = PKEY_ONBOARDING_STATUS,
        reader = { key ->
            val order = getInt(key, OnboardingStatus.NOT_ONBOARDED.order)
            OnboardingStatus.values().find { it.order == order } ?: OnboardingStatus.NOT_ONBOARDED
        },
        writer = { key, value ->
            putInt(key, value.order)
        }
    )

    inline val isOnboardingDoneFlow: Flow<Boolean>
        get() = onboardingStatus.flow.map { it == OnboardingStatus.ONBOARDED_2_0 }

    inline val isOnboardingDone: Boolean
        get() = onboardingStatus.value == OnboardingStatus.ONBOARDED_2_0

    fun clear() {

    }

    override suspend fun reset() {
        Timber.d("reset()")
        preferences.clearAndNotify()
    }

    enum class OnboardingStatus(val order: Int) {
        NOT_ONBOARDED(0),
        ONBOARDED_2_0(1)
    }

    companion object {
        private const val PKEY_ONBOARDING_STATUS = "trace_location_onboardingstatus"
        private const val name = "trace_location_localdata"
    }
}
