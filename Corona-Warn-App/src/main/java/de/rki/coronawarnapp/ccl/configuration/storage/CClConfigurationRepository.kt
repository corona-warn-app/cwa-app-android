package de.rki.coronawarnapp.ccl.configuration.storage

import de.rki.coronawarnapp.ccl.configuration.common.CCLConfigurationParser
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.configuration.server.CCLConfigurationServer
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
    private val cclConfigurationParser: CCLConfigurationParser,
    private val cclConfigurationServer: CCLConfigurationServer
) {
    private val internalData: HotDataFlow<List<CCLConfiguration>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Eagerly,
        startValueProvider = { loadInitialConfigs() }
    )

    val cclConfigurations: Flow<List<CCLConfiguration>> = internalData.data

    suspend fun getCCLConfigurations(): List<CCLConfiguration> = cclConfigurations.first()

    private val defaultCCLConfigurationsRawData: ByteArray
        get() = defaultCCLConfigurationProvider.loadDefaultCCLConfigurationsRawData()

    private fun ByteArray.tryParseCCLConfigurations(): List<CCLConfiguration>? = try {
        Timber.tag(TAG).d("Trying to parse %s", this)
        cclConfigurationParser.parseCClConfigurations(rawData = this)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to parse %s", this)
        null
    }.also { Timber.d("Returning %s", it) }

    private suspend fun loadInitialConfigs(): List<CCLConfiguration> {
        Timber.tag(TAG).d("loadInitialConfig()")
        val config = cclConfigurationStorage.load()?.tryParseCCLConfigurations()
        return when (config != null) {
            true -> config
            false -> cclConfigurationParser.parseCClConfigurations(rawData = defaultCCLConfigurationsRawData)
        }.also { Timber.tag(TAG).d("Returning %s", it) }
    }
}

private val TAG = tag<CCLConfigurationRepository>()
