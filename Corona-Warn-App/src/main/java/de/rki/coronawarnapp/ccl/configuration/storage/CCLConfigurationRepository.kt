package de.rki.coronawarnapp.ccl.configuration.storage

import de.rki.coronawarnapp.ccl.configuration.common.CCLConfigurationParser
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.configuration.server.CCLConfigurationServer
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.repositories.UpdateResult
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
    private val downloadedCclConfigurationStorage: DownloadedCCLConfigurationStorage,
    private val defaultCCLConfigurationProvider: DefaultCCLConfigurationProvider,
    private val cclConfigurationParser: CCLConfigurationParser,
    private val cclConfigurationServer: CCLConfigurationServer,
    private val cclConfigurationMerger: CCLConfigurationMerger
) {
    private val internalData: HotDataFlow<List<CCLConfiguration>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Eagerly,
        startValueProvider = { loadInitialConfigs() }
    )

    val cclConfigurations: Flow<List<CCLConfiguration>> = internalData.data

    suspend fun getCCLConfigurations(): List<CCLConfiguration> = cclConfigurations.first()

    /**
     * @return UpdateResult.UPDATE if new data was fetched from the server, UpdateResult.NO_UPDATE if
     * we didn't get new data from the server and UpdaterResult.FAIL if something went wrong
     **/
    suspend fun updateCCLConfiguration(): UpdateResult = try {
        var updateResult = UpdateResult.NO_UPDATE

        internalData.updateBlocking {
            Timber.tag(TAG).d("Updating ccl configuration")

            val downloadedConfigListRaw = cclConfigurationServer.getCCLConfiguration() ?: run {
                // no new config was downloaded
                Timber.tag(TAG).d("Nothing to update. Keeping old ccl config list")
                return@updateBlocking this
            }

            val downloadedConfigList = downloadedConfigListRaw.tryParseCCLConfigurations() ?: run {
                updateResult = UpdateResult.FAIL
                return@updateBlocking this
            }

            Timber.tag(TAG).d("Saving new config data")
            downloadedCclConfigurationStorage.save(rawData = downloadedConfigListRaw)

            updateResult = UpdateResult.UPDATE

            return@updateBlocking cclConfigurationMerger.merge(
                defaultConfigList = loadDefaultCCLConfiguration(),
                downloadedConfigList = downloadedConfigList
            )
        }

        updateResult
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Error while updating ccl config list")
        UpdateResult.FAIL
    }

    suspend fun clear() {
        Timber.tag(TAG).d("Clearing")
        downloadedCclConfigurationStorage.clear()
        internalData.updateBlocking { loadInitialConfigs() }
    }

    private val defaultCCLConfigurationsRawData: ByteArray
        get() = defaultCCLConfigurationProvider.loadDefaultCCLConfigurationsRawData()

    private fun ByteArray.tryParseCCLConfigurations(): List<CCLConfiguration>? = try {
        Timber.tag(TAG).d("tryParseCCLConfiguration()")
        cclConfigurationParser.parseCClConfigurations(rawData = this)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to parse CCLConfiguration")
        null
    }.also { logConfigVersionAndIdentifier(it, logPrefix = "tryParseCCLConfiguration() - Returning") }

    private suspend fun loadInitialConfigs(): List<CCLConfiguration> {
        Timber.tag(TAG).d("loadInitialConfig()")
        val downloadedConfigList = downloadedCclConfigurationStorage.load()?.tryParseCCLConfigurations()
        return when (downloadedConfigList != null) {
            true -> {
                cclConfigurationMerger.merge(
                    defaultConfigList = loadDefaultCCLConfiguration(),
                    downloadedConfigList = downloadedConfigList
                )
            }
            false -> loadDefaultCCLConfiguration()
        }.also { logConfigVersionAndIdentifier(it, logPrefix = "loadInitialConfig() - Returning") }
    }

    private fun loadDefaultCCLConfiguration() =
        cclConfigurationParser.parseCClConfigurations(rawData = defaultCCLConfigurationsRawData)

    private fun logConfigVersionAndIdentifier(cclConfigurationList: List<CCLConfiguration>?, logPrefix: String) {
        cclConfigurationList?.forEach { config ->
            Timber.tag(TAG)
                .d("%s Configuration with Version=%s and identifier=%s", logPrefix, config.version, config.identifier)
        }
    }
}

private val TAG = tag<CCLConfigurationRepository>()
