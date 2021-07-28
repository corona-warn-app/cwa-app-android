package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ValueSetsStorage @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson
) {
    private val mutex = Mutex()
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).also {
            migration23To24(it)
        }
    }

    suspend fun load(): ValueSetsContainer = mutex.withLock {
        Timber.tag(TAG).v("load()")
        val valueSetString = prefs.getString(PKEY_VALUE_SETS_CONTAINER_PREFIX, null)
        return when (valueSetString != null) {
            true -> gson.fromJson(valueSetString)
            else -> emptyValueSetsContainer
        }.also { loaded -> Timber.v("Loaded value sets container %s", loaded) }
    }

    suspend fun save(value: ValueSetsContainer) = mutex.withLock {
        Timber.tag(TAG).v("save(value=%s)", value)
        prefs.edit(commit = true) {
            val json = gson.toJson(value, ValueSetsContainer::class.java)
            putString(PKEY_VALUE_SETS_CONTAINER_PREFIX, json)
        }
    }

    // Migration from 2.3.x to 2.4.x: Support for more value sets
    private fun migration23To24(prefs: SharedPreferences) {
        Timber.v("Checking for leftover and removing it")
        if (prefs.contains(PKEY_VALUE_SETS_PREFIX)) {
            prefs.edit(commit = true) {
                remove(PKEY_VALUE_SETS_PREFIX)
            }
        }
    }

    companion object {
        private const val TAG = "ValueSetsStorage"
        private const val PREF_NAME = "valuesets_localdata"
        private const val PKEY_VALUE_SETS_PREFIX = "valueset"
        private const val PKEY_VALUE_SETS_CONTAINER_PREFIX = "valuesets_container"
    }
}
