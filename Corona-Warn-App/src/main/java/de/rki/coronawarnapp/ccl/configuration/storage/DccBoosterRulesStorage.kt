package de.rki.coronawarnapp.ccl.configuration.storage

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.ccl.configuration.CclConfiguration
import de.rki.coronawarnapp.covidcertificate.validation.core.CertificateValidation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccBoosterRulesStorage @Inject constructor(
    @CertificateValidation private val cacheDir: File,
    @CclConfiguration private val cclFile: File
) {
    private val mutex = Mutex()

    // legacy
    @VisibleForTesting
    internal val boosterRulesCacheFile = File(cacheDir, "dcc_validation_cache_bn_rules_raw")

    @VisibleForTesting
    internal val boosterRulesFile = File(cclFile, "dcc_booster_rules")

    private suspend fun loadLegacyBoosterRulesJson(): String? = boosterRulesCacheFile.load()

    suspend fun loadBoosterRulesJson(): String? = boosterRulesFile.load() ?: loadLegacyBoosterRulesJson()?.also {
        Timber.tag(TAG).i("Loading legacy booster rules.")
    }

    suspend fun saveBoosterRulesJson(data: String?) = boosterRulesFile.save(data).also {
        Timber.tag(TAG).i("Delete legacy booster rules.")
        boosterRulesCacheFile.save(null)
    }

    private suspend fun File.load(): String? = mutex.withLock {
        try {
            if (exists()) readText() else null
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to load $path")
            null
        }
    }

    private suspend fun File.save(data: String?) = mutex.withLock {
        if (data == null) {
            if (exists() && delete()) {
                Timber.tag(TAG).d("File $path was deleted.")
            }
            return
        }
        if (exists()) {
            Timber.tag(TAG).d("Overwriting with new data for $path (size=%d)", data.length)
        }
        parentFile?.mkdirs()
        writeText(data)
    }
}

private const val TAG = "DccBoosterRuleStorage"
