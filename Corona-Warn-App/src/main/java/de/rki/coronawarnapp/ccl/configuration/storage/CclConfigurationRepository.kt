package de.rki.coronawarnapp.ccl.configuration.storage

import de.rki.coronawarnapp.ccl.configuration.common.CclConfigurationParser
import de.rki.coronawarnapp.ccl.configuration.model.CclConfiguration
import de.rki.coronawarnapp.ccl.configuration.server.CclConfigurationServer
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
class CclConfigurationRepository @Inject constructor(
    @AppScope appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    private val downloadedCclConfigurationStorage: DownloadedCclConfigurationStorage,
    private val defaultCclConfigurationProvider: DefaultCclConfigurationProvider,
    private val cclConfigurationParser: CclConfigurationParser,
    private val cclConfigurationServer: CclConfigurationServer,
    private val cclConfigurationMerger: CclConfigurationMerger
) {
    private val internalData: HotDataFlow<List<CclConfiguration>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Eagerly,
        startValueProvider = { loadInitialConfigs() }
    )

    val cclConfigurations: Flow<List<CclConfiguration>> = internalData.data

    suspend fun getCclConfigurations(): List<CclConfiguration> = cclConfigurations.first()

    /**
     * @return UpdateResult.UPDATE if new data was fetched from the server, UpdateResult.NO_UPDATE if
     * we didn't get new data from the server and UpdaterResult.FAIL if something went wrong
     **/
    suspend fun updateCclConfiguration(): UpdateResult = try {
        var updateResult = UpdateResult.NO_UPDATE

        internalData.updateBlocking {
            Timber.tag(TAG).d("Updating ccl configuration")

            val downloadedConfigListRaw = cclConfigurationServer.getCclConfiguration() ?: run {
                // no new config was downloaded
                Timber.tag(TAG).d("Nothing to update. Keeping old ccl config list")
                return@updateBlocking this
            }

            val downloadedConfigList = downloadedConfigListRaw.tryParseCclConfigurations() ?: run {
                updateResult = UpdateResult.FAIL
                return@updateBlocking this
            }

            Timber.tag(TAG).d("Saving new config data")
            downloadedCclConfigurationStorage.save(rawData = downloadedConfigListRaw)

            updateResult = UpdateResult.UPDATE

            return@updateBlocking cclConfigurationMerger.merge(
                defaultConfigList = loadDefaultCclConfiguration(),
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

    private val defaultCclConfigurationsRawData: ByteArray
        get() = defaultCclConfigurationProvider.loadDefaultCclConfigurationsRawData()

    private fun ByteArray.tryParseCclConfigurations(): List<CclConfiguration>? = try {
        Timber.tag(TAG).d("tryParseCclConfiguration()")
        cclConfigurationParser.parseCClConfigurations(rawData = this)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to parse CclConfiguration")
        null
    }.also { logConfigVersionAndIdentifier(it, logPrefix = "tryParseCclConfiguration() - Returning") }

    private suspend fun loadInitialConfigs(): List<CclConfiguration> {
        Timber.tag(TAG).d("loadInitialConfig()")
        val downloadedConfigList = downloadedCclConfigurationStorage.load()?.tryParseCclConfigurations()
        return when (downloadedConfigList != null) {
            true -> {
                cclConfigurationMerger.merge(
                    defaultConfigList = loadDefaultCclConfiguration(),
                    downloadedConfigList = downloadedConfigList
                )
            }
            false -> loadDefaultCclConfiguration()
        }.also { logConfigVersionAndIdentifier(it, logPrefix = "loadInitialConfig() - Returning") }
    }

    private fun loadDefaultCclConfiguration() =
        cclConfigurationParser.parseCClConfigurations(rawData = defaultCclConfigurationsRawData)

    private fun logConfigVersionAndIdentifier(cclConfigurationList: List<CclConfiguration>?, logPrefix: String) {
        cclConfigurationList?.forEach { config ->
            Timber.tag(TAG)
                .d("%s Configuration with Version=%s and identifier=%s", logPrefix, config.version, config.identifier)
        }
    }
}

private val TAG = tag<CclConfigurationRepository>()
