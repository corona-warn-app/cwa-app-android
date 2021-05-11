package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationStorage @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val baseGson: Gson
) {

    private val prefs by lazy {
        context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
    }

    private val gson by lazy {
        // Allow for custom type adapter.
        baseGson.newBuilder().apply {
            registerTypeAdapter(RawCOSEObject::class.java, RawCOSEObject.JsonAdapter())
        }.create()
    }

    var personContainers: Set<PersonData>
        get() {
            Timber.tag(TAG).d("vaccinatedPersons - load()")
            val persons = prefs.all.mapNotNull { (key, value) ->
                if (!key.startsWith(PKEY_PERSON_PREFIX)) {
                    return@mapNotNull null
                }
                value as String
                gson.fromJson<PersonData>(value).also {
                    Timber.tag(TAG).v("Person loaded: %s", it)
                    requireNotNull(it.identifier)
                }
            }
            return persons.toSet()
        }
        set(persons) {
            Timber.tag(TAG).d("vaccinatedPersons - save(%s)", persons)

            prefs.edit {
                prefs.all.keys.filter { it.startsWith(PKEY_PERSON_PREFIX) }.forEach {
                    Timber.tag(TAG).v("Removing data for %s", it)
                    remove(it)
                }
                persons.forEach {
                    val raw = gson.toJson(it)
                    val identifier = it.identifier
                    Timber.tag(TAG).v("Storing vaccinatedPerson %s -> %s", identifier, raw)
                    putString("$PKEY_PERSON_PREFIX${identifier.code}", raw)
                }
            }
        }

    companion object {
        private const val TAG = "VaccinationStorage"
        private const val PKEY_PERSON_PREFIX = "vaccination.person."
    }
}
