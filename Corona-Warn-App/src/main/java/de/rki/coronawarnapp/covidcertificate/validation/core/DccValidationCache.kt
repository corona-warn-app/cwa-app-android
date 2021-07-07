package de.rki.coronawarnapp.covidcertificate.validation.core

import androidx.annotation.VisibleForTesting
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

    suspend fun loadCountryJson(): String? = countryCacheFile.load("raw dcc countries")

    suspend fun loadRuleJson(): String? = rulesCacheFile.load("raw rules")

    suspend fun saveCountryJson(data: String?) = countryCacheFile.save(data, "raw dcc countries")

    suspend fun saveRulesJson(data: String?) = rulesCacheFile.save(data, "raw dcc countries")

    @VisibleForTesting
    internal suspend fun File.load(descr: String): String? = mutex.withLock {
        try {
            if (exists()) readText() else null
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to load $descr from cache.")
            null
        }
    }

    @VisibleForTesting
    internal suspend fun File.save(data: String?, descr: String) = mutex.withLock {
        if (data == null) {
            if (exists() && delete()) {
                Timber.tag(TAG).d("Cache file for $descr was deleted.")
            }
            return
        }
        if (exists()) {
            Timber.tag(TAG).d("Overwriting with new data for $descr (size=%d)", data.length)
        }
        parentFile?.mkdirs()
        writeText(data)
    }

    companion object {
        const val TAG = "DccValidationLocalCache"
    }
}
