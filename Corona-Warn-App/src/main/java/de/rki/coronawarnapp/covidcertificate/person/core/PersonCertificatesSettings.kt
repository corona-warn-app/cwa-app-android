package de.rki.coronawarnapp.covidcertificate.person.core

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.serialization.BaseGson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonCertificatesSettings @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson val gson: Gson,
) {
    private val prefs by lazy {
        context.getSharedPreferences("certificate_person_localdata", Context.MODE_PRIVATE)
    }

    val currentCwaUser: FlowPreference<CertificatePersonIdentifier?> = prefs.createFlowPreference(
        key = "certificate.person.current",
        reader = FlowPreference.gsonReader<CertificatePersonIdentifier?>(gson, null),
        writer = FlowPreference.gsonWriter(gson)
    )

    fun clear() {
        Timber.d("clear()")
        prefs.clearAndNotify()
    }
}
