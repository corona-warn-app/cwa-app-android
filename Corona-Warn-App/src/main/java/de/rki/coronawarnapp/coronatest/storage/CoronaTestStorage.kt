package de.rki.coronawarnapp.coronatest.storage

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RapidAntigenCoronaTest
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.adapter.RuntimeTypeAdapterFactory
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
            val rta = RuntimeTypeAdapterFactory.of(CoronaTest::class.java)
                .registerSubtype(PCRCoronaTest::class.java)
                .registerSubtype(RapidAntigenCoronaTest::class.java)

            registerTypeAdapterFactory(rta)
        }.create()
    }

    private val typeToken by lazy {
        object : TypeToken<List<CoronaTest>>() {}.type
    }

    var coronaTests: Collection<CoronaTest>
        get() {
            Timber.tag(TAG).d("load()")
            val raw = prefs.getString(PKEY_TESTDATA, null) ?: return emptySet()
            val tests: List<CoronaTest> = gson.fromJson(raw, typeToken)
            tests.forEach {
                Timber.tag(TAG).v("Loaded: %s", it)
                requireNotNull(it.identifier)
            }
            return tests
        }
        set(value) {
            Timber.tag(TAG).d("save(tests=%s)", value)
            prefs.edit {
                val raw = gson.toJson(value, typeToken)
                putString(PKEY_TESTDATA, raw)
            }
        }

    companion object {
        private const val TAG = "CoronaTestStorage"
        private const val PKEY_TESTDATA = "coronatest.jsondata"
    }
}
