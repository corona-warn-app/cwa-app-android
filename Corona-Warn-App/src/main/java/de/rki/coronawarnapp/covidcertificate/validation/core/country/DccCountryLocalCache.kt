package de.rki.coronawarnapp.covidcertificate.validation.core.country

import de.rki.coronawarnapp.covidcertificate.validation.core.CertificateValidation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccCountryLocalCache @Inject constructor(
    @CertificateValidation private val cacheDir: File,
) {
    private val mutex = Mutex()
    private val cacheFile = File(cacheDir, "dcc_country_cache_raw")

    suspend fun loadJson(): String? = mutex.withLock {
        try {
            if (cacheFile.exists()) cacheFile.readText() else null
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to load raw dcc countries from cache.")
            null
        }
    }

    suspend fun saveJson(data: String?) = mutex.withLock {
        if (data == null) {
            if (cacheFile.exists() && cacheFile.delete()) {
                Timber.tag(TAG).d("Cache file was deleted.")
            }
            return
        }
        if (cacheFile.exists()) {
            Timber.tag(TAG).d("Overwriting with new data (size=%d)", data.length)
        }
        cacheFile.parentFile?.mkdirs()
        cacheFile.writeText(data)
    }

    companion object {
        const val TAG = "DccCountryLocalCache"
    }
}
