package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import android.content.Context
import androidx.core.content.edit
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
    @BaseGson val baseGson: Gson,
) {

    private val prefs by lazy {
        context.getSharedPreferences("recovery_localdata", Context.MODE_PRIVATE)
    }

    private val gson by lazy {
        baseGson.newBuilder().create()
    }

    var recoveryCertificates: Set<StoredRecoveryCertificateData>
        get() {
            Timber.tag(TAG).d("recoveryCertificates - load()")
            return prefs.all.mapNotNull { (key, value) ->
                if (!key.startsWith(PKEY_RECOVERY_CERT_PREFIX)) {
                    return@mapNotNull null
                }
                value as String
                gson.fromJson<StoredRecoveryCertificateData>(value).also { data ->
                    Timber.tag(TAG).v("recovery certificate loaded: %s", data)
                    requireNotNull(data.identifier)
                    requireNotNull(data.registeredAt)
                }
            }.toSet()
        }
        set(value) {
            Timber.tag(TAG).d("recoveryCertificates - save(%s)", value)

            prefs.edit {
                prefs.all.keys.filter { it.startsWith(PKEY_RECOVERY_CERT_PREFIX) }.forEach {
                    Timber.tag(TAG).v("Removing data for %s", it)
                    remove(it)
                }
                value.forEach {
                    val raw = gson.toJson(it)
                    val identifier = it.identifier
                    Timber.tag(TAG).v("Storing recovery certificate %s -> %s", identifier, raw)
                    putString("$PKEY_RECOVERY_CERT_PREFIX$identifier", raw)
                }
            }
        }

    companion object {
        private const val TAG = "RecoveryCertStorage"
        private const val PKEY_RECOVERY_CERT_PREFIX = "recovery.certificate."
    }
}
