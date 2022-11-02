package de.rki.coronawarnapp.coronatest.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.coronatest.CoronaTestStorageDataStore
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.datastore.dataRecovering
import de.rki.coronawarnapp.util.datastore.distinctUntilChanged
import de.rki.coronawarnapp.util.datastore.trySetValue
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.serialization.BaseGson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoronaTestStorage @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    @CoronaTestStorageDataStore private val dataStore: DataStore<Preferences>,
    @BaseGson val baseGson: Gson
) {

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

    suspend fun getCoronaTests(): Collection<PersonalCoronaTest> {
        Timber.tag(TAG).d("load()")
        val pcrTestFlow = dataStore.dataRecovering.distinctUntilChanged(
            key = PKEY_DATA_PCR, defaultValue = ""
        ).map { value ->
            if (value.isEmpty()) {
                emptySet()
            } else {
                gson.fromJson<Set<PCRCoronaTest>>(value, typeTokenPCR).onEach {
                    Timber.tag(TAG).v("PCR loaded: %s", it)
                    requireNotNull(it.identifier)
                    requireNotNull(it.type) { "PCR type should not be null, GSON footgun." }
                }
            }
        }

        val raTestFlow = dataStore.dataRecovering.distinctUntilChanged(
            key = PKEY_DATA_RA, defaultValue = ""
        ).map { value ->
            if (value.isEmpty()) {
                emptySet()
            } else {
                gson.fromJson<Set<RACoronaTest>>(value, typeTokenRA).onEach {
                    Timber.tag(TAG).v("RA loaded: %s", it)
                    requireNotNull(it.identifier)
                    requireNotNull(it.type) { "RA type should not be null, GSON footgun." }
                }
            }
        }

        return combine(pcrTestFlow, raTestFlow) { pcrTests, raTests ->
            val tests = pcrTests + raTests
            Timber.tag(TAG).v("Loaded %d tests.", tests.size)
            tests
        }.shareLatest(scope = appScope).first()
    }

    suspend fun updateCoronaTests(tests: Collection<PersonalCoronaTest>) {
        Timber.tag(TAG).d("save(tests=%s)", tests)
        tests.filter { it.type == BaseCoronaTest.Type.PCR }.run {
            if (isNotEmpty()) {
                val raw = gson.toJson(this, typeTokenPCR)
                Timber.tag(TAG).v("PCR storing: %s", raw)
                dataStore.trySetValue(PKEY_DATA_PCR, raw)
            } else {
                Timber.tag(TAG).v("No PCR tests available, clearing.")
                dataStore.edit { it.remove(PKEY_DATA_PCR) }
            }
        }
        tests.filter { it.type == BaseCoronaTest.Type.RAPID_ANTIGEN }.run {
            if (isNotEmpty()) {
                val raw = gson.toJson(this, typeTokenRA)
                Timber.tag(TAG).v("RA storing: %s", raw)
                dataStore.trySetValue(PKEY_DATA_RA, raw)
            } else {
                Timber.tag(TAG).v("No RA tests available, clearing.")
                dataStore.edit { it.remove(PKEY_DATA_RA) }
            }
        }
    }

    companion object {
        private const val TAG = "CoronaTestStorage"

        val PKEY_DATA_RA = stringPreferencesKey("coronatest.data.ra")
        val PKEY_DATA_PCR = stringPreferencesKey("coronatest.data.pcr")
    }
}
