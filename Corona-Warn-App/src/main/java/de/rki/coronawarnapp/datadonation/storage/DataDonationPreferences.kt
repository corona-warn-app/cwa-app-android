package de.rki.coronawarnapp.datadonation.storage

import android.content.Context
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataDonationPreferences @Inject constructor(
    @AppContext val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("data_donation_localdata", Context.MODE_PRIVATE)
    }

    val oneTimePassword = prefs.createFlowPreference<OneTimePassword?>(
        key = "one_time_password",
        defaultValue = null
    )

    fun clear() {
        prefs.clearAndNotify()
    }
}
