package de.rki.coronawarnapp.covidcertificate.person.core

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.serialization.BaseJackson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonCertificatesSettings @Inject constructor(
    @AppContext private val context: Context,
    @BaseJackson private val mapper: ObjectMapper,
) {
    private val prefs by lazy {
        context.getSharedPreferences("certificate_person_localdata", Context.MODE_PRIVATE)
    }

    val currentCwaUser: FlowPreference<CertificatePersonIdentifier?> = prefs.createFlowPreference(
        key = "certificate.person.current",
        reader = FlowPreference.jacksonReader<CertificatePersonIdentifier?>(mapper, null),
        writer = FlowPreference.jacksonWriter(mapper)
    )

    fun clear() {
        Timber.d("clear()")
        prefs.clearAndNotify()
    }
}
