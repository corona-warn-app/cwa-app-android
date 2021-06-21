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
    @BaseGson val gson: Gson,
) {

    private val prefs by lazy {
        context.getSharedPreferences("recovery_localdata", Context.MODE_PRIVATE)
    }

    var recoveryCertificates: Set<StoredRecoveryCertificateData>
        get() {
            Timber.tag(TAG).d("recoveryCertificates - load()")
            return gson.fromJson<RecoveryCertificateDTO>(
                prefs.getString(PKEY_RECOVERY_CERT, null) ?: return emptySet()
            ).data.onEach {
                Timber.tag(TAG).v("recovery certificate loaded: %s", it)
                requireNotNull(it.identifier)
                requireNotNull(it.registeredAt)
            }.toSet()
        }
        set(value) {
            Timber.tag(TAG).d("recoveryCertificates - save(%s)", value)
            prefs.edit {
                if (value.isEmpty()) {
                    remove(PKEY_RECOVERY_CERT)
                } else {
                    putString(
                        PKEY_RECOVERY_CERT, gson.toJson(
                            RecoveryCertificateDTO(
                                value.onEach { data ->
                                    Timber.tag(TAG).v("Storing recovery certificate %s", data.identifier)
                                }.toList()
                            )
                        )
                    )
                }
            }
        }

    companion object {
        private const val TAG = "RecoveryCertStorage"
        private const val PKEY_RECOVERY_CERT = "recovery.certificate"
    }
}
