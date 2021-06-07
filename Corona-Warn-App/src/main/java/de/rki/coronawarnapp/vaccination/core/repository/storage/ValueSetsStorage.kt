package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets.ValueSetsContainer
import de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets.emptyValueSetsContainer
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

    var valueSetsContainer: ValueSetsContainer
        get() = getValueSet()
        set(value) = setValueSet(value)

    private fun getValueSet(): ValueSetsContainer {
        Timber.v("Loading value sets container")
        val valueSetString = prefs.getString(PKEY_VALUE_SETS_CONTAINER_PREFIX, null)
        return when (valueSetString != null) {
            true -> gson.fromJson(valueSetString)
            else -> emptyValueSetsContainer
        }.also { loaded -> Timber.v("Loaded value sets container %s", loaded) }
    }

    private fun setValueSet(value: ValueSetsContainer) {
        Timber.v("Saving value sets container %s", value)
        prefs.edit {
            val json = gson.toJson(value, ValueSetsContainer::class.java)
            Timber.v("Writing %s to prefs", json)
            putString(PKEY_VALUE_SETS_CONTAINER_PREFIX, json)
        }
    }
}

private const val PREF_NAME = "valuesets_localdata"
private const val PKEY_VALUE_SETS_CONTAINER_PREFIX = "valuesets_container"
