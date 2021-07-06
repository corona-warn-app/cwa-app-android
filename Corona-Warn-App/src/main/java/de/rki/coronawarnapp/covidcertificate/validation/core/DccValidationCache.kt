package de.rki.coronawarnapp.covidcertificate.validation.core

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccValidationCache @Inject constructor(
    @CertificateValidation private val cacheDir: File,
) {
    private val mutex = Mutex()
    private val countryCacheFile = File(cacheDir, "dcc_validation_cache_countries_raw")
    private val rulesCacheFile = File(cacheDir, "dcc_validation_cache_rules_raw")

    suspend fun loadJson(): String? = mutex.withLock {
        try {
            if (countryCacheFile.exists()) countryCacheFile.readText() else null
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to load raw dcc countries from cache.")
            null
        }
        // TODO load rules
    }

    suspend fun saveJson(data: String?) = mutex.withLock {
        if (data == null) {
            if (countryCacheFile.exists() && countryCacheFile.delete()) {
                Timber.tag(TAG).d("Cache file was deleted.")
            }
            return
        }
        if (countryCacheFile.exists()) {
            Timber.tag(TAG).d("Overwriting with new data (size=%d)", data.length)
        }
        countryCacheFile.parentFile?.mkdirs()
        countryCacheFile.writeText(data)
        // TODO write rules
    }

    companion object {
        const val TAG = "DccValidationLocalCache"
    }
}
