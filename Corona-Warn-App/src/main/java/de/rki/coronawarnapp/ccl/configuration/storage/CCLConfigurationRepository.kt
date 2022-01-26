package de.rki.coronawarnapp.ccl.configuration.storage

import de.rki.coronawarnapp.ccl.configuration.common.CCLConfigurationParser
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CCLConfigurationRepository @Inject constructor(
    @AppScope appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    private val cclConfigurationStorage: CCLConfigurationStorage,
    private val defaultCCLConfigurationProvider: DefaultCCLConfigurationProvider,
    private val cclConfigurationParser: CCLConfigurationParser
) {
    private val internalData: HotDataFlow<CCLConfiguration> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Eagerly,
        startValueProvider = { loadInitialConfig() }
    )

    val cclConfiguration: Flow<CCLConfiguration> = internalData.data

    suspend fun getCCLConfiguration(): CCLConfiguration = internalData.data.first()

    suspend fun updateCCLConfiguration(rawData: ByteArray) = internalData.updateBlocking {
        Timber.tag(TAG).d("Updating ccl configuration")
        val newConfig = rawData.tryParseCCLConfiguration()
        when (newConfig != null) {
            true -> {
                Timber.tag(TAG).d("Saving new config json")
                cclConfigurationStorage.save(rawData = rawData)
                newConfig
            }
            false -> this
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).d("Clearing")
        cclConfigurationStorage.clear()
        updateCCLConfiguration(rawData = defaultCCLConfiguration)
    }

    private val defaultCCLConfiguration: ByteArray
        get() = defaultCCLConfigurationProvider.loadDefaultCCLConfiguration()

    private fun ByteArray.tryParseCCLConfiguration(): CCLConfiguration? = try {
        Timber.tag(TAG).d("Trying to parse %s", this)
        cclConfigurationParser.parseCClConfiguration(rawData = this)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to parse %s", this)
        null
    }.also { Timber.d("Returning %s", it) }

    private suspend fun loadInitialConfig(): CCLConfiguration {
        Timber.tag(TAG).d("loadInitialConfig()")
        val config = cclConfigurationStorage.load()?.tryParseCCLConfiguration()
        return when (config != null) {
            true -> config
            false -> cclConfigurationParser.parseCClConfiguration(rawData = defaultCCLConfiguration)
        }.also { Timber.tag(TAG).d("Returning %s", it) }
    }
}

private val TAG = tag<CCLConfigurationRepository>()
