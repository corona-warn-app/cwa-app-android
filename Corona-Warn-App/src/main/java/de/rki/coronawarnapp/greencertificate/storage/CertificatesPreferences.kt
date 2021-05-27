package de.rki.coronawarnapp.greencertificate.storage

import android.content.Context
import de.rki.coronawarnapp.greencertificate.ui.CertificatesSettings
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificatesPreferences @Inject constructor(
    @AppContext val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("certificates_localdata", Context.MODE_PRIVATE)
    }

    val onboardingStatusOrder = prefs.createFlowPreference(
        key = "certificates_onboarding_status",
        defaultValue = CertificatesSettings.OnboardingStatus.NOT_ONBOARDED.ordinal
    )

    fun clear() {
        prefs.clearAndNotify()
    }
}
