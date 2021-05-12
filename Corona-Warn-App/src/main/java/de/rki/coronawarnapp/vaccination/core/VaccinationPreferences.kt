package de.rki.coronawarnapp.vaccination.core

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationPreferences @Inject constructor(
    @AppContext val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
    }

    val registrationAcknowledged = prefs.createFlowPreference(
        key = "registration_acknowledged",
        defaultValue = false
    )

    fun clear() {
        prefs.clearAndNotify()
    }
}
