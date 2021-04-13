package de.rki.coronawarnapp.presencetracing

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceLocationSettings @Inject constructor(
    @AppContext val context: Context
) {

    private val preferences by lazy {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    var onboardingStatus: OnboardingStatus
        get() {
            val order = preferences.getInt(key_status, OnboardingStatus.NOT_ONBOARDED.order)
            return OnboardingStatus.values().find { it.order == order } ?: OnboardingStatus.NOT_ONBOARDED
        }
        set(value) = preferences.edit().putInt(key_status, value.order).apply()

    inline val isOnboardingDone get() = onboardingStatus == OnboardingStatus.ONBOARDED_2_0

    fun clear() {
        preferences.clearAndNotify()
    }

    enum class OnboardingStatus(val order: Int) {
        NOT_ONBOARDED(0),
        ONBOARDED_2_0(1)
    }

    companion object {
        private const val key_status = "trace_location_onboardingstatus"
        private const val name = "trace_location_localdata"
    }
}
