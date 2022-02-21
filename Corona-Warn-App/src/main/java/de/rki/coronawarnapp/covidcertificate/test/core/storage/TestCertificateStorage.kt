package de.rki.coronawarnapp.covidcertificate.test.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.BaseTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RACertificateData
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCertificateStorage @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val baseGson: Gson,
) {

    private val mutex = Mutex()
    private val prefs by lazy {
        context.getSharedPreferences("coronatest_certificate_localdata", Context.MODE_PRIVATE)
    }

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

        val pcrCertContainers: Set<PCRCertificateData> = prefs.loadCerts(typeTokenPCR, PKEY_DATA_PCR)
        val raCerts: Set<RACertificateData> = prefs.loadCerts(typeTokenRA, PKEY_DATA_RA)
        val scannedCerts: Set<GenericTestCertificateData> = prefs.loadCerts(typeTokenGeneric, PKEY_DATA_SCANNED)

        return (pcrCertContainers + raCerts + scannedCerts).also {
            Timber.tag(TAG).v("Loaded %d certificates.", it.size)
        }
    }

    suspend fun save(certs: Collection<BaseTestCertificateData>) = mutex.withLock {
        Timber.tag(TAG).d("save(testCertificates=%s)", certs.size)
        prefs.edit(commit = true) {
            storeCerts(certs.filterIsInstance<PCRCertificateData>(), typeTokenPCR, PKEY_DATA_PCR)
            storeCerts(certs.filterIsInstance<RACertificateData>(), typeTokenRA, PKEY_DATA_RA)
            storeCerts(certs.filterIsInstance<GenericTestCertificateData>(), typeTokenGeneric, PKEY_DATA_SCANNED)
        }
    }

    private fun <T : BaseTestCertificateData> SharedPreferences.Editor.storeCerts(
        certs: Collection<BaseTestCertificateData>,
        typeToken: TypeToken<Set<T>>,
        storageKey: String
    ) {
        val type = typeToken.type
        if (certs.isNotEmpty()) {
            val raw = gson.toJson(certs, type)
            Timber.tag(TAG).v("Storing scanned certs ($type): %s", certs.size)
            putString(storageKey, raw)
        } else {
            Timber.tag(TAG).v("No stored certificates ($type) available, clearing.")
            remove(storageKey)
        }
    }

    private fun <T : BaseTestCertificateData> SharedPreferences.loadCerts(
        typeToken: TypeToken<Set<T>>,
        storageKey: String
    ): Set<T> {
        object : TypeToken<Set<PCRCertificateData>>() {}
        val type = typeToken.type
        val raw = prefs.getString(storageKey, null) ?: return emptySet()
        return gson.fromJson<Set<T>>(raw, type).onEach {
            Timber.tag(TAG).v("Certificates ($type) loaded: %s", it.identifier)
            requireNotNull(it.identifier)
        }
    }

    companion object {
        private const val TAG = "TestCertificateStorage"
        private const val PKEY_DATA_RA = "testcertificate.data.ra"
        private const val PKEY_DATA_PCR = "testcertificate.data.pcr"
        private const val PKEY_DATA_SCANNED = "testcertificate.data.scanned"
    }
}
