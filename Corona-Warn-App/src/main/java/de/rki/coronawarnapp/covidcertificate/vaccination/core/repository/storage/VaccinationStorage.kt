package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationStorage @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val baseGson: Gson,
) {
    private val mutex = Mutex()
    private val prefs by lazy {
        context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
    }

    private val gson by lazy {
        // Allow for custom type adapter.
        baseGson.newBuilder().apply {
            registerTypeAdapterFactory(CwaCovidCertificate.State.typeAdapter)
        }.create()
    }

    suspend fun load2(): Set<StoredVaccinationCertificateData> = mutex.withLock {
        Timber.tag(TAG).d("load()")
        return gson
            .fromJson<Set<StoredVaccinationCertificateData>>(
                prefs.getString(PKEY_VACCINATION_CERT, null) ?: return emptySet(),
                TYPE_TOKEN
            )
    }

    suspend fun save2(certificates: Set<StoredVaccinationCertificateData>) = mutex.withLock {
        Timber.tag(TAG).d("save(%s)", certificates.size)
        prefs.edit(commit = true) {
            if (certificates.isEmpty()) {
                remove(PKEY_VACCINATION_CERT)
            } else {
                val rawJson = gson.toJson(certificates, TYPE_TOKEN)
                putString(PKEY_VACCINATION_CERT, rawJson)
            }
        }
    }

    // Legacy implementation
    suspend fun load(): Set<VaccinatedPersonData> = mutex.withLock {
        Timber.tag(TAG).d("load()")
        val persons = prefs.all.mapNotNull { (key, value) ->
            if (!key.startsWith(PKEY_PERSON_PREFIX)) {
                return@mapNotNull null
            }
            value as String
            gson.fromJson<VaccinatedPersonData>(value).also { _ ->
                Timber.tag(TAG).v("Person loaded: %s", key)
            }
        }
        return persons.toSet()
    }

    suspend fun clearLegacyData() = mutex.withLock {
        Timber.tag(TAG).d("saveLegacyData()")
        prefs.edit(commit = true) {
            prefs.all.keys.filter { it.startsWith(PKEY_PERSON_PREFIX) }.forEach {
                Timber.tag(TAG).v("Removing data for %s", it)
                remove(it)
            }
        }
    }

    companion object {
        private const val TAG = "VaccinationStorage"
        private const val PKEY_PERSON_PREFIX = "vaccination.person."
        private const val PKEY_VACCINATION_CERT = "vaccination.certificate"
        private val TYPE_TOKEN = object : TypeToken<Set<StoredVaccinationCertificateData>>() {}.type
    }
}
