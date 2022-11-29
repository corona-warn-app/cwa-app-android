package de.rki.coronawarnapp.covidcertificate.test.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.BaseTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RACertificateData
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.serialization.BaseGson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCertificateStorage @Inject constructor(
    @TestCertificateStorageDataStore private val dataStore: DataStore<Preferences>,
    @BaseGson val baseGson: Gson,
) {
    private val mutex = Mutex()

    private val gson by lazy {
        baseGson.newBuilder().apply {
            registerTypeAdapterFactory(CwaCovidCertificate.State.typeAdapter)
            registerTypeAdapter(CoronaTestResult::class.java, CoronaTestResult.GsonAdapter())
        }.create()
    }

    private val typeTokenPCR: TypeToken<Set<PCRCertificateData>> by lazy {
        object : TypeToken<Set<PCRCertificateData>>() {}
    }
    private val typeTokenRA: TypeToken<Set<RACertificateData>> by lazy {
        object : TypeToken<Set<RACertificateData>>() {}
    }
    private val typeTokenGeneric: TypeToken<Set<GenericTestCertificateData>> by lazy {
        object : TypeToken<Set<GenericTestCertificateData>>() {}
    }

    suspend fun load(): Collection<BaseTestCertificateData> = mutex.withLock {
        Timber.tag(TAG).d("load()")

        val pcrCertContainers: Set<PCRCertificateData> = loadCerts(typeTokenPCR, PKEY_DATA_PCR)
        val raCerts: Set<RACertificateData> = loadCerts(typeTokenRA, PKEY_DATA_RA)
        val scannedCerts: Set<GenericTestCertificateData> = loadCerts(typeTokenGeneric, PKEY_DATA_SCANNED)

        return (pcrCertContainers + raCerts + scannedCerts).also {
            Timber.tag(TAG).v("Loaded %d certificates.", it.size)
        }
    }

    suspend fun save(certs: Collection<BaseTestCertificateData>) = mutex.withLock {
        Timber.tag(TAG).d("save(testCertificates=%s)", certs.size)

        storeCerts(certs.filterIsInstance<PCRCertificateData>(), typeTokenPCR, PKEY_DATA_PCR)
        storeCerts(certs.filterIsInstance<RACertificateData>(), typeTokenRA, PKEY_DATA_RA)
        storeCerts(certs.filterIsInstance<GenericTestCertificateData>(), typeTokenGeneric, PKEY_DATA_SCANNED)
    }

    private suspend fun <T : BaseTestCertificateData> storeCerts(
        certs: Collection<BaseTestCertificateData>,
        typeToken: TypeToken<Set<T>>,
        storageKey: Preferences.Key<String>
    ) {
        val type = typeToken.type
        if (certs.isNotEmpty()) {
            val raw = gson.toJson(certs, type)
            Timber.tag(TAG).v("Storing scanned certs ($type): %s", certs.size)
            dataStore.trySetValue(storageKey, raw)
        } else {
            Timber.tag(TAG).v("No stored certificates ($type) available, clearing.")
            dataStore.edit { it.remove(storageKey) }
        }
    }

    private suspend fun <T : BaseTestCertificateData> loadCerts(
        typeToken: TypeToken<Set<T>>,
        storageKey: Preferences.Key<String>
    ): Set<T> {
        object : TypeToken<Set<PCRCertificateData>>() {}
        val type = typeToken.type

        return dataStore.dataRecovering.distinctUntilChanged(storageKey, "").map { value ->
            if (value.isEmpty()) {
                emptySet()
            } else {
                gson.fromJson<Set<T>>(value, type).onEach {
                    Timber.tag(TAG).v("Certificates ($type) loaded: %s", it.identifier)
                    requireNotNull(it.identifier)
                }
            }
        }.first()
    }

    companion object {
        private const val TAG = "TestCertificateStorage"
        val PKEY_DATA_RA = stringPreferencesKey("testcertificate.data.ra")
        val PKEY_DATA_PCR = stringPreferencesKey("testcertificate.data.pcr")
        val PKEY_DATA_SCANNED = stringPreferencesKey("testcertificate.data.scanned")
    }
}
