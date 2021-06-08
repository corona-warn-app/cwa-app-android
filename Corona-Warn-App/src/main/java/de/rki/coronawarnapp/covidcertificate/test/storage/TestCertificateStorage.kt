package de.rki.coronawarnapp.covidcertificate.test.storage

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.vaccination.core.repository.storage.ContainerPostProcessor
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCertificateStorage @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val baseGson: Gson,
    private val containerPostProcessor: ContainerPostProcessor,
) {

    private val prefs by lazy {
        context.getSharedPreferences("coronatest_certificate_localdata", Context.MODE_PRIVATE)
    }

    private val gson by lazy {
        baseGson.newBuilder().apply {
            registerTypeAdapter(CoronaTestResult::class.java, CoronaTestResult.GsonAdapter())
            registerTypeAdapterFactory(containerPostProcessor)
        }.create()
    }

    private val typeTokenPCR by lazy {
        object : TypeToken<Set<PCRCertificateData>>() {}.type
    }

    private val typeTokenRA by lazy {
        object : TypeToken<Set<RACertificateData>>() {}.type
    }

    var testCertificates: Collection<StoredTestCertificateData>
        get() {
            Timber.tag(TAG).d("load()")

            val pcrCertContainers: Set<PCRCertificateData> = run {
                val raw = prefs.getString(PKEY_DATA_PCR, null) ?: return@run emptySet()
                gson.fromJson<Set<PCRCertificateData>>(raw, typeTokenPCR).onEach {
                    Timber.tag(TAG).v("PCR loaded: %s", it)
                    requireNotNull(it.identifier)
                    requireNotNull(it.type) { "PCR type should not be null, GSON footgun." }
                }
            }

            val raCerts: Set<RACertificateData> = run {
                val raw = prefs.getString(PKEY_DATA_RA, null) ?: return@run emptySet()
                gson.fromJson<Set<RACertificateData>>(raw, typeTokenRA).onEach {
                    Timber.tag(TAG).v("RA loaded: %s", it)
                    requireNotNull(it.identifier)
                    requireNotNull(it.type) { "RA type should not be null, GSON footgun." }
                }
            }

            return (pcrCertContainers + raCerts).also {
                Timber.tag(TAG).v("Loaded %d certificates.", it.size)
            }
        }
        set(value) {
            Timber.tag(TAG).d("save(testCertificates=%s)", value)
            prefs.edit {
                value.filter { it.type == CoronaTest.Type.PCR }.run {
                    if (isNotEmpty()) {
                        val raw = gson.toJson(this, typeTokenPCR)
                        Timber.tag(TAG).v("PCR storing: %s", raw)
                        putString(PKEY_DATA_PCR, raw)
                    } else {
                        Timber.tag(TAG).v("No PCR certificates available, clearing.")
                        remove(PKEY_DATA_PCR)
                    }
                }
                value.filter { it.type == CoronaTest.Type.RAPID_ANTIGEN }.run {
                    if (isNotEmpty()) {
                        val raw = gson.toJson(this, typeTokenRA)
                        Timber.tag(TAG).v("RA storing: %s", raw)
                        putString(PKEY_DATA_RA, raw)
                    } else {
                        Timber.tag(TAG).v("No RA certificates available, clearing.")
                        remove(PKEY_DATA_RA)
                    }
                }
            }
        }

    companion object {
        private const val TAG = "TestCertificateStorage"
        private const val PKEY_DATA_RA = "testcertificate.data.ra"
        private const val PKEY_DATA_PCR = "testcertificate.data.pcr"
    }
}
