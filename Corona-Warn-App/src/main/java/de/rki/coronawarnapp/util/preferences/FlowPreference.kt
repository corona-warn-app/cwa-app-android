package de.rki.coronawarnapp.util.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class FlowPreference<T> constructor(
    private val preferences: SharedPreferences,
    private val key: String,
    private val reader: SharedPreferences.(key: String) -> T,
    private val writer: SharedPreferences.Editor.(key: String, value: T) -> Unit
) {

    private val flowInternal = MutableStateFlow(internalValue)
    val flow: Flow<T> = flowInternal

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { changedPrefs, changedKey ->
            if (changedKey != key) return@OnSharedPreferenceChangeListener

            val newValue = reader(changedPrefs, changedKey)
            val currentvalue = flowInternal.value
            if (currentvalue != newValue && flowInternal.compareAndSet(currentvalue, newValue)) {
                Timber.v("%s:%s changed to %s", changedPrefs, changedKey, newValue)
            }
        }

    init {
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private var internalValue: T
        get() = reader(preferences, key)
        set(newValue) {
            preferences.edit {
                writer(key, newValue)
            }
            flowInternal.value = internalValue
        }
    val value: T
        get() = internalValue

    fun update(update: (T) -> T) {
        internalValue = update(internalValue)
    }

    companion object {
        inline fun <reified T> gsonReader(
            gson: Gson,
            defaultValue: T
        ): SharedPreferences.(key: String) -> T = { key ->
            getString(key, null)?.let { gson.fromJson<T>(it) } ?: defaultValue
        }

        inline fun <reified T> gsonWriter(
            gson: Gson
        ): SharedPreferences.Editor.(key: String, value: T) -> Unit = { key, value ->
            putString(key, value?.let { gson.toJson(it) })
        }

        inline fun <reified T> basicReader(defaultValue: T): SharedPreferences.(key: String) -> T =
            { key ->
                (this.all[key] ?: defaultValue) as T
            }

        inline fun <reified T> basicWriter(): SharedPreferences.Editor.(key: String, value: T) -> Unit =
            { key, value ->
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    null -> remove(key)
                    else -> throw NotImplementedError()
                }
            }
    }
}

inline fun <reified T : Any?> SharedPreferences.createFlowPreference(
    key: String,
    defaultValue: T = null as T
) = FlowPreference(
    preferences = this,
    key = key,
    reader = FlowPreference.basicReader(defaultValue),
    writer = FlowPreference.basicWriter()
)

inline fun <reified T : Any?> SharedPreferences.createFlowPreference(
    key: String,
    noinline reader: SharedPreferences.(key: String) -> T,
    noinline writer: SharedPreferences.Editor.(key: String, value: T) -> Unit
) = FlowPreference(
    preferences = this,
    key = key,
    reader = reader,
    writer = writer
)
