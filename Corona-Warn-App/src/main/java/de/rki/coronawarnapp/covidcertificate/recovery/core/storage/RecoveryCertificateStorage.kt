package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.covidcertificate.RecoveryCertificateDataStore
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryCertificateStorage @Inject constructor(
    @RecoveryCertificateDataStore private val dataStore: DataStore<Preferences>,
    @BaseJackson private val mapper: ObjectMapper,
) {

    private val mutex = Mutex()

    suspend fun load(): Set<StoredRecoveryCertificateData> = mutex.withLock {
        Timber.tag(TAG).d("recoveryCertificates - load()")
        return mapper.readValue<Set<StoredRecoveryCertificateData>>(
            dataStore.dataRecovering.distinctUntilChanged(key = PKEY_RECOVERY_CERT).first()
                ?: return emptySet()
        )
            .onEach { Timber.tag(TAG).v("StoredRecoveryCertificateData loaded: %s", it) }
    }

    suspend fun save(certificates: Set<StoredRecoveryCertificateData>) = mutex.withLock {
        Timber.tag(TAG).d("recoveryCertificates - save(%s)", certificates.size)

        if (certificates.isEmpty()) {
            dataStore.edit { it.remove(PKEY_RECOVERY_CERT) }
            Unit
        } else {
            val rawJson = mapper.writeValueAsString(certificates)
            dataStore.edit { it[PKEY_RECOVERY_CERT] = rawJson }
            Unit
        }
    }

    companion object {
        private const val TAG = "RecoveryCertStorage"

        @VisibleForTesting
        val PKEY_RECOVERY_CERT = stringPreferencesKey("recovery.certificate")
    }
}
