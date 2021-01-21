package de.rki.coronawarnapp.contactdiary.ui

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactDiarySettings @Inject constructor(
    @AppContext val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("contact_diary_localdata", Context.MODE_PRIVATE)
    }

    private val onboardingStatusOrder = prefs.createFlowPreference(
        key = "contact_diary_onboardingstatus",
        defaultValue = -1
    )

    var onboardingStatus: OnboardingStatus
        get() {
            val order = onboardingStatusOrder.value
            return OnboardingStatus.values().find { it.order == order } ?: OnboardingStatus.NOT_ONBOARDED
        }
        set(value) = onboardingStatusOrder.update { value.order }

    fun clear() {
        prefs.clearAndNotify()
    }

    enum class OnboardingStatus(val order: Int) {
        NOT_ONBOARDED(-1),
        RISK_STATUS_1_12(0),
        FUTURE_CHANGE_1_13(1)
    }
}
