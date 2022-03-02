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
        baseGson.newBuilder().apply {
            registerTypeAdapterFactory(CwaCovidCertificate.State.typeAdapter)
        }.create()
    }

    suspend fun load(): Set<StoredVaccinationCertificateData> = mutex.withLock {
        Timber.tag(TAG).d("load()")
        return gson
            .fromJson<Set<StoredVaccinationCertificateData>?>(
                prefs.getString(PKEY_VACCINATION_CERT, null) ?: return emptySet(),
                TYPE_TOKEN
            )
            .associateBy { it.vaccinationQrCode }
            .map { it.value }
            .toSet()
    }

    suspend fun save(certificates: Set<StoredVaccinationCertificateData>) = mutex.withLock {
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

    suspend fun loadLegacyData(): Set<VaccinatedPersonData> = mutex.withLock {
        Timber.tag(TAG).d("loadLegacyData()")
        val persons = prefs.all.mapNotNull { (key, value) ->
            if (!key.startsWith("vaccination.person.")) {
                return@mapNotNull null
            }
            value as String
            gson.fromJson<VaccinatedPersonData>(value).also {
                Timber.tag(TAG).v("Person loaded: %s", key)
            }
        }
        return persons.toSet()
    }

    suspend fun clearLegacyData() = mutex.withLock {
        Timber.tag(TAG).d("clearLegacyData()")
        prefs.edit(commit = true) {
            prefs.all.keys.filter { it.startsWith("vaccination.person.") }.forEach {
                Timber.tag(TAG).v("Removing data for %s", it)
                remove(it)
            }
        }
    }

    companion object {
        private const val TAG = "VaccinationStorage"
        private const val PKEY_VACCINATION_CERT = "vaccination.certificate"
        private val TYPE_TOKEN = object : TypeToken<Set<StoredVaccinationCertificateData>>() {}.type
    }
}
