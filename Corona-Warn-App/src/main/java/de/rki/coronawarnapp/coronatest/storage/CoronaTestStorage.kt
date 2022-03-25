package de.rki.coronawarnapp.coronatest.storage

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoronaTestStorage @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val baseGson: Gson
) {

    private val prefs by lazy {
        context.getSharedPreferences("coronatest_localdata", Context.MODE_PRIVATE)
    }

    private val gson by lazy {
        baseGson.newBuilder().apply {
            registerTypeAdapter(CoronaTestResult::class.java, CoronaTestResult.GsonAdapter())
        }.create()
    }

    private val typeTokenPCR by lazy {
        object : TypeToken<Set<PCRCoronaTest>>() {}.type
    }

    private val typeTokenRA by lazy {
        object : TypeToken<Set<RACoronaTest>>() {}.type
    }

    var coronaTests: Collection<PersonalCoronaTest>
        get() {
            Timber.tag(TAG).d("load()")

            val pcrTests: Set<PCRCoronaTest> = run {
                val raw = prefs.getString(PKEY_DATA_PCR, null) ?: return@run emptySet()
                gson.fromJson<Set<PCRCoronaTest>>(raw, typeTokenPCR).onEach {
                    Timber.tag(TAG).v("PCR loaded: %s", it)
                    requireNotNull(it.identifier)
                    requireNotNull(it.type) { "PCR type should not be null, GSON footgun." }
                }
            }

            val raTests: Set<RACoronaTest> = run {
                val raw = prefs.getString(PKEY_DATA_RA, null) ?: return@run emptySet()
                gson.fromJson<Set<RACoronaTest>>(raw, typeTokenRA).onEach {
                    Timber.tag(TAG).v("RA loaded: %s", it)
                    requireNotNull(it.identifier)
                    requireNotNull(it.type) { "RA type should not be null, GSON footgun." }
                }
            }

            val tests = pcrTests + raTests
            Timber.tag(TAG).v("Loaded %d tests.", tests.size)
            return tests
        }
        set(value) {
            Timber.tag(TAG).d("save(tests=%s)", value)
            prefs.edit {
                value.filter { it.type == BaseCoronaTest.Type.PCR }.run {
                    if (isNotEmpty()) {
                        val raw = gson.toJson(this, typeTokenPCR)
                        Timber.tag(TAG).v("PCR storing: %s", raw)
                        putString(PKEY_DATA_PCR, raw)
                    } else {
                        Timber.tag(TAG).v("No PCR tests available, clearing.")
                        remove(PKEY_DATA_PCR)
                    }
                }
                value.filter { it.type == BaseCoronaTest.Type.RAPID_ANTIGEN }.run {
                    if (isNotEmpty()) {
                        val raw = gson.toJson(this, typeTokenRA)
                        Timber.tag(TAG).v("RA storing: %s", raw)
                        putString(PKEY_DATA_RA, raw)
                    } else {
                        Timber.tag(TAG).v("No RA tests available, clearing.")
                        remove(PKEY_DATA_RA)
                    }
                }
            }
        }

    companion object {
        private const val TAG = "CoronaTestStorage"
        private const val PKEY_DATA_RA = "coronatest.data.ra"
        private const val PKEY_DATA_PCR = "coronatest.data.pcr"
    }
}
