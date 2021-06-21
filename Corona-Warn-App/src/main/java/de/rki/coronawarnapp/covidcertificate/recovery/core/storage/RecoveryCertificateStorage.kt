package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
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
            return gson.fromJson<Set<StoredRecoveryCertificateData>>(
                prefs.getString(PKEY_RECOVERY_CERT, null) ?: return emptySet(), TYPE_TOKEN
            ).onEach {
                Timber.tag(TAG).v("recovery certificate loaded: %s", it)
                requireNotNull(it.identifier)
                requireNotNull(it.registeredAt)
            }
        }
        set(value) {
            Timber.tag(TAG).d("recoveryCertificates - save(%s)", value)
            prefs.edit {
                if (value.isEmpty()) {
                    remove(PKEY_RECOVERY_CERT)
                } else {
                    putString(
                        PKEY_RECOVERY_CERT,
                        gson.toJson(
                            value.onEach { data ->
                                Timber.tag(TAG).v("Storing recovery certificate %s", data.identifier)
                            },
                            TYPE_TOKEN
                        )
                    )
                }
            }
        }

    companion object {
        private const val TAG = "RecoveryCertStorage"
        private const val PKEY_RECOVERY_CERT = "recovery.certificate"
        private val TYPE_TOKEN = object : TypeToken<Set<StoredRecoveryCertificateData>>() {}.type
    }
}
