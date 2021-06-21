package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryCertificateStorage @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val gson: Gson,
) {

    private val prefs by lazy {
        context.getSharedPreferences("recovery_localdata", Context.MODE_PRIVATE)
    }

    var recoveryCertificates: Set<StoredRecoveryCertificateData>
        get() {
            Timber.tag(TAG).d("recoveryCertificates - load()")
            return prefs.getStringSet(PKEY_RECOVERY_CERT, emptySet())?.map {
                gson.fromJson<StoredRecoveryCertificateData>(it).also { data ->
                    Timber.tag(TAG).v("recovery certificate loaded: %s", data)
                    requireNotNull(data.identifier)
                    requireNotNull(data.registeredAt)
                }
            }?.toSet() ?: emptySet()
        }
        set(value) {
            Timber.tag(TAG).d("recoveryCertificates - save(%s)", value)
            prefs.edit()
                .putStringSet(
                    PKEY_RECOVERY_CERT,
                    value.map { data ->
                        gson.toJson(data).also {
                            Timber.tag(TAG).v("Storing recovery certificate %s -> %s", data.identifier, it)
                        }
                    }.toSet()
                )
                .apply()
        }

    companion object {
        private const val TAG = "RecoveryCertStorage"
        private const val PKEY_RECOVERY_CERT = "recovery.certificate"
    }
}
