package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.covidcertificate.RecoveryCertificateDataStore
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.serialization.BaseGson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryCertificateStorage @Inject constructor(
    @RecoveryCertificateDataStore private val dataStore: DataStore<Preferences>,
    @BaseGson val baseGson: Gson,
) {

    private val gson by lazy {
        baseGson.newBuilder()
            .registerTypeAdapterFactory(CwaCovidCertificate.State.typeAdapter)
            .create()
    }

    private val mutex = Mutex()

    suspend fun load(): Set<StoredRecoveryCertificateData> = mutex.withLock {
        Timber.tag(TAG).d("recoveryCertificates - load()")
        return gson
            .fromJson<Set<StoredRecoveryCertificateData>>(
                dataStore.dataRecovering.distinctUntilChanged(key = PKEY_RECOVERY_CERT).first()
                    ?: return emptySet(),
                TYPE_TOKEN
            )
            .onEach { Timber.tag(TAG).v("StoredRecoveryCertificateData loaded: %s", it) }
    }

    suspend fun save(certificates: Set<StoredRecoveryCertificateData>) = mutex.withLock {
        Timber.tag(TAG).d("recoveryCertificates - save(%s)", certificates.size)

        if (certificates.isEmpty()) {
            dataStore.edit { it.remove(PKEY_RECOVERY_CERT) }
            Unit
        } else {
            val rawJson = gson.toJson(certificates, TYPE_TOKEN)
            dataStore.edit { it[PKEY_RECOVERY_CERT] = rawJson }
            Unit
        }
    }

    companion object {
        private const val TAG = "RecoveryCertStorage"

        @VisibleForTesting
        val PKEY_RECOVERY_CERT = stringPreferencesKey("recovery.certificate")
        private val TYPE_TOKEN = object : TypeToken<Set<StoredRecoveryCertificateData>>() {}.type
    }
}
