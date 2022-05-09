package de.rki.coronawarnapp.covidcertificate.validation.core

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccValidationCache @Inject constructor(
    @CertificateValidation private val cacheDir: File,
) : Resettable {
    private val mutex = Mutex()
    private val countryCacheFile = File(cacheDir, "dcc_validation_cache_countries_raw")
    private val acceptanceRulesCacheFile = File(cacheDir, "dcc_validation_cache_acc_rules_raw")
    private val invalidationRulesCacheFile = File(cacheDir, "dcc_validation_cache_inv_rules_raw")
    private val boosterNotificationRulesCacheFile = File(cacheDir, "dcc_validation_cache_bn_rules_raw")

    suspend fun loadCountryJson(): String? = countryCacheFile.load()

    suspend fun loadAcceptanceRuleJson(): String? = acceptanceRulesCacheFile.load()

    suspend fun loadInvalidationRuleJson(): String? = invalidationRulesCacheFile.load()

    suspend fun loadBoosterNotificationRulesJson(): String? = boosterNotificationRulesCacheFile.load()

    suspend fun saveCountryJson(data: String?) = countryCacheFile.save(data)

    suspend fun saveAcceptanceRulesJson(data: String?) = acceptanceRulesCacheFile.save(data)

    suspend fun saveInvalidationRulesJson(data: String?) = invalidationRulesCacheFile.save(data)

    suspend fun saveBoosterNotificationRulesJson(data: String?) = boosterNotificationRulesCacheFile.save(data)

    override suspend fun reset() {
        cacheDir.deleteRecursively()
            .also { Timber.d("Successfully deleted %s: %b", cacheDir.name, it) }
    }

    @VisibleForTesting
    internal suspend fun File.load(): String? = mutex.withLock {
        try {
            if (exists()) readText() else null
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to load $name from cache.")
            null
        }
    }

    @VisibleForTesting
    internal suspend fun File.save(data: String?) = mutex.withLock {
        if (data == null) {
            if (exists() && delete()) {
                Timber.tag(TAG).d("Cache file for $name was deleted.")
            }
            return
        }
        if (exists()) {
            Timber.tag(TAG).d("Overwriting with new data for $name (size=%d)", data.length)
        }
        parentFile?.mkdirs()
        writeText(data)
    }

    companion object {
        const val TAG = "DccValidationLocalCache"
    }
}
