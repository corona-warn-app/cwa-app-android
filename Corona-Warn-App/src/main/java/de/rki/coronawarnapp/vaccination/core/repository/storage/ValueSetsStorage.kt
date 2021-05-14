package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ValueSetsStorage @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson
) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var vaccinationValueSet: VaccinationValueSet?
        get() = getValueSet()
        set(value) = setValueSet(value)

    private fun getValueSet(): VaccinationValueSet? {
        Timber.d("Loading value set")
        return prefs.getString(PKEY_VALUE_SETS_PREFIX, null)?.let {
            gson.fromJson<VaccinationValueSet>(it).also { loaded -> Timber.d("Loaded %s", loaded) }
        }.also { Timber.d("Returning %s", it) }
    }

    private fun setValueSet(value: VaccinationValueSet?) {
        Timber.d("Saving %s", value)
        value?.let {
            prefs.edit {
                val json = gson.toJson(it)
                Timber.d("String %s", json)
                putString(PKEY_VALUE_SETS_PREFIX, json)
            }
        }
    }

    fun clear() {
        Timber.d("Clearing local storage")
        prefs.clearAndNotify()
    }
}

private const val PREF_NAME = "valuesets_local"
private const val PKEY_VALUE_SETS_PREFIX = "valuesets"
