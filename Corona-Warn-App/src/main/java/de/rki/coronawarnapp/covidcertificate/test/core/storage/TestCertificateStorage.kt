package de.rki.coronawarnapp.covidcertificate.test.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.google.gson.Gson
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
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.coronawarnapp.util.serialization.jackson.StateJsonSerializerFactory
import de.rki.coronawarnapp.util.serialization.jackson.registerCoronaTestResultSerialization
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
    @BaseJackson private val objectMapper: ObjectMapper,
) {
    private val mutex = Mutex()

    private val gson by lazy {
        baseGson.newBuilder().apply {
            registerTypeAdapterFactory(CwaCovidCertificate.State.typeAdapter)
            registerTypeAdapter(CoronaTestResult::class.java, CoronaTestResult.GsonAdapter())
        }.create()
    }

    private val mapper by lazy {
        objectMapper.registerModule(object : SimpleModule() {
            override fun setupModule(context: SetupContext) {
                super.setupModule(context)
                context.addBeanSerializerModifier(StateJsonSerializerFactory())
                this.registerCoronaTestResultSerialization()
            }
        })
    }

    private val typeReferencePCR: TypeReference<Set<PCRCertificateData>> by lazy {
        object : TypeReference<Set<PCRCertificateData>>() {}
    }
    private val typeReferenceRA: TypeReference<Set<RACertificateData>> by lazy {
        object : TypeReference<Set<RACertificateData>>() {}
    }
    private val typeReferenceGeneric: TypeReference<Set<GenericTestCertificateData>> by lazy {
        object : TypeReference<Set<GenericTestCertificateData>>() {}
    }

    suspend fun load(): Collection<BaseTestCertificateData> = mutex.withLock {
        Timber.tag(TAG).d("load()")

        val pcrCertContainers: Set<PCRCertificateData> = loadCerts(PKEY_DATA_PCR, typeReferencePCR)
        val raCerts: Set<RACertificateData> = loadCerts(PKEY_DATA_RA, typeReferenceRA)
        val scannedCerts: Set<GenericTestCertificateData> = loadCerts(PKEY_DATA_SCANNED, typeReferenceGeneric)

        return (pcrCertContainers + raCerts + scannedCerts).also {
            Timber.tag(TAG).v("Loaded %d certificates.", it.size)
        }
    }

    suspend fun save(certs: Collection<BaseTestCertificateData>) = mutex.withLock {
        Timber.tag(TAG).d("save(testCertificates=%s)", certs.size)

        storeCerts(certs.filterIsInstance<PCRCertificateData>(), PKEY_DATA_PCR)
        storeCerts(certs.filterIsInstance<RACertificateData>(), PKEY_DATA_RA)
        storeCerts(certs.filterIsInstance<GenericTestCertificateData>(), PKEY_DATA_SCANNED)
    }

    private suspend fun storeCerts(
        certs: Collection<BaseTestCertificateData>,
        storageKey: Preferences.Key<String>
    ) {
        if (certs.isNotEmpty()) {
            val raw = mapper.writeValueAsString(certs)
            Timber.tag(TAG).v("Storing scanned certs (${certs.first()::class}): %s", certs.size)
            dataStore.trySetValue(storageKey, raw)
        } else {
            Timber.tag(TAG).v("No stored certificates available, clearing.")
            dataStore.edit { it.remove(storageKey) }
        }
    }

    private suspend fun <T : BaseTestCertificateData> loadCerts(
        storageKey: Preferences.Key<String>,
        typeReference: TypeReference<Set<T>>
    ): Set<T> {
        return dataStore.dataRecovering.distinctUntilChanged(storageKey, "").map { value ->
            if (value.isEmpty()) {
                emptySet()
            } else {
                mapper.readValue(value, typeReference).onEach {
                    Timber.tag(TAG).v("Certificates ($typeReference) loaded: %s", it.identifier)
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
