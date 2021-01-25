package de.rki.coronawarnapp.contactdiary.storage

import android.content.Context
import de.rki.coronawarnapp.contactdiary.ui.ContactDiarySettings
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactDiaryPreferences @Inject constructor(
    @AppContext val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("contact_diary_localdata", Context.MODE_PRIVATE)
    }

    val onboardingStatusOrder = prefs.createFlowPreference(
        key = "contact_diary_onboardingstatus",
        defaultValue = ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED.ordinal
    )

    fun clear() {
        prefs.clearAndNotify()
    }
}
