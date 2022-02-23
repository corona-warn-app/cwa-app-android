package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryCertificateStorage @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val baseGson: Gson,
) {

    private val gson by lazy {
        baseGson.newBuilder()
            .registerTypeAdapterFactory(CwaCovidCertificate.State.typeAdapter)
            .create()
    }

    private val mutex = Mutex()
    private val prefs by lazy {
        context.getSharedPreferences("recovery_localdata", Context.MODE_PRIVATE)
    }

    suspend fun load(): Set<StoredRecoveryCertificateData> = mutex.withLock {
        Timber.tag(TAG).d("recoveryCertificates - load()")
        return gson
            .fromJson<Set<StoredRecoveryCertificateData>>(
                prefs.getString(PKEY_RECOVERY_CERT, null) ?: return emptySet(), TYPE_TOKEN
            )
            .onEach { Timber.tag(TAG).v("StoredRecoveryCertificateData loaded: %s", it) }
    }

    suspend fun save(certificates: Set<StoredRecoveryCertificateData>) = mutex.withLock {
        Timber.tag(TAG).d("recoveryCertificates - save(%s)", certificates.size)
        prefs.edit(commit = true) {
            if (certificates.isEmpty()) {
                remove(PKEY_RECOVERY_CERT)
            } else {
                val rawJson = gson.toJson(certificates, TYPE_TOKEN)
                putString(PKEY_RECOVERY_CERT, rawJson)
            }
        }
    }

    companion object {
        private const val TAG = "RecoveryCertStorage"
        private const val PKEY_RECOVERY_CERT = "recovery.certificate"
        private val TYPE_TOKEN = object : TypeToken<Set<StoredRecoveryCertificateData>>() {}.type
    }
}
