package de.rki.coronawarnapp.util.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

/**
 * [DataStore] throws an [IOException] when an exception is encountered when reading data. This
 * catches any [IOException] and recovers with [emptyPreferences].
 * Be aware that this can still throw other Exceptions.
 */
val DataStore<Preferences>.dataRecovering
    get() = data.catch {
        Timber.e(it, "Failed to read DataStore")
        if (it is IOException) emit(emptyPreferences()) else throw it
    }

/**
 * Returns a [Flow] containing the value set for the specified [key]. If no value is set for the
 * key, the flow contains null. Uses [Preferences.get] to get the value.
 * Be aware that this can emit the value multiple times, e.g. when another preferences gets changed.
 */
fun <T> Flow<Preferences>.map(key: Preferences.Key<T>) = map { prefs -> prefs[key] }

/**
 * Combines [map] to get the value for the specified [Preferences.Key] and
 * [Flow.distinctUntilChanged] to filter out repetitions.
 */
fun <T> Flow<Preferences>.distinctUntilChanged(key: Preferences.Key<T>) = map(key)
    .distinctUntilChanged()

/**
 * Returns the value set for the specified [Preferences.Key] or null, if the key is
 * not set.
 */
suspend fun <T> DataStore<Preferences>.getValueOrNull(
    preferencesKey: Preferences.Key<T>
): T? = dataRecovering.map(preferencesKey).firstOrNull()

/**
 * Returns the value set for the specified [Preferences.Key] or the [defaultValue], if the key is
 * not set.
 */
suspend fun <T> DataStore<Preferences>.getValueOrDefault(
    preferencesKey: Preferences.Key<T>,
    defaultValue: T
): T = getValueOrNull(preferencesKey) ?: defaultValue

/**
 * Set the [value] for the specified [Preferences.Key]
 */
suspend fun <T> DataStore<Preferences>.setValue(
    preferencesKey: Preferences.Key<T>,
    value: T
) {
    edit { prefs -> prefs[preferencesKey] = value }
}

/**
 * Deletes all data of the [DataStore]
 */
suspend fun DataStore<Preferences>.clear() {
    edit { prefs -> prefs.clear() }
}
