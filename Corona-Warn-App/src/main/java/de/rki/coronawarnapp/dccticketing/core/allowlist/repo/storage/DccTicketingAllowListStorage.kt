package de.rki.coronawarnapp.dccticketing.core.allowlist.repo.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingAllowListContainer
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingAllowListStorage @Inject constructor(
    @AppContext context: Context,
    @BaseGson private val gson: Gson
) {

    private val mutex = Mutex()
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    suspend fun load(): DccTicketingAllowListContainer = mutex.withLock {
        Timber.tag(TAG).v("Load()")
        DccTicketingAllowListContainer(
            serviceProviderAllowList = load(PKEY_ALLOW_LIST_SERVICE_PROVIDER),
            validationServiceAllowList = load(PKEY_ALLOW_LIST_VALIDATION_SERVICE)
        ).also { Timber.v("Returning %s", it) }
    }

    suspend fun save(container: DccTicketingAllowListContainer) = mutex.withLock {
        Timber.tag(TAG).v("save(container=%s)", container)
        save(key = PKEY_ALLOW_LIST_SERVICE_PROVIDER, value = container.serviceProviderAllowList)
        save(key = PKEY_ALLOW_LIST_VALIDATION_SERVICE, value = container.validationServiceAllowList)
    }

    private inline fun <reified T> load(key: String): Set<T> = try {
        Timber.tag(TAG).v("load(key=%s)", key)
        val json = prefs.getString(key, null)

        when (json != null) {
            true -> gson.fromJson(json)
            false -> emptySet()
        }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Loading data failed. Fallback to empty set.")
        emptySet()
    }

    private inline fun <reified T> save(key: String, value: Set<T>) = try {
        Timber.tag(TAG).v("save(key=%s, value=%s)", key, value)
        prefs.edit(commit = true) {
            val type = object : TypeToken<Set<T>>() {}.type
            val json = gson.toJson(value, type)
            putString(key, json)
        }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Saving data failed.")
    }

    companion object {
        private val TAG = tag<DccTicketingAllowListStorage>()

        private const val PREF_NAME = "allowlist_localdata"
        private const val PKEY_ALLOW_LIST_VALIDATION_SERVICE = "allow_list_validation_service"
        private const val PKEY_ALLOW_LIST_SERVICE_PROVIDER = "allow_list_service_provider"
    }
}
