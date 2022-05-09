package de.rki.coronawarnapp.covidcertificate.vaccination.core

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.reset.Resettable
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CovidCertificateSettings @Inject constructor(
    @AppContext val context: Context,
) : Resettable {
    private val prefs by lazy {
        context.getSharedPreferences("covid_certificate_localdata", Context.MODE_PRIVATE)
    }

    val isOnboarded = prefs.createFlowPreference(
        key = "covid_certificate_onboarded",
        defaultValue = false
    )

    val lastDccStateBackgroundCheck = prefs.createFlowPreference("dcc.state.lastcheck", Instant.EPOCH)

    override suspend fun reset() {
        Timber.d("reset()")
        prefs.clearAndNotify()
    }
}
