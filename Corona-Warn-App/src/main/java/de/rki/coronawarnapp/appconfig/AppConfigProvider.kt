package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.download.AppConfigServer
import de.rki.coronawarnapp.appconfig.download.AppConfigStorage
import de.rki.coronawarnapp.appconfig.download.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigProvider @Inject constructor(
    private val server: AppConfigServer,
    private val storage: AppConfigStorage,
    private val parser: ConfigParser,
    private val dispatcherProvider: DispatcherProvider
) {
    private val mutex = Mutex()

    suspend fun getAppConfig(): ConfigContainerKey = mutex.withLock {
        withContext(dispatcherProvider.IO) {

            val (serverBytes, serverError) = try {
                server.downloadAppConfig() to null
            } catch (e: Exception) {
                Timber.w(e, "Failed to download AppConfig from server .")
                null to e
            }

            var parsedConfig: ConfigContainerKey? = serverBytes?.let { bytes ->
                try {
                    parser.parse(bytes).also {
                        Timber.d("Got a valid AppConfig from server, saving.")
                        storage.setAppConfigRaw(bytes)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse AppConfig from server, trying fallback.")
                    null
                }
            }

            if (parsedConfig == null) {
                parsedConfig = storage.getAppConfigRaw()?.let {
                    try {
                        parser.parse(it)
                    } catch (e: Exception) {
                        Timber.e(e, "Fallback config exists but could not be parsed!")
                        throw e
                    }
                }
            }

            if (parsedConfig == null) {
                throw ApplicationConfigurationInvalidException(serverError)
            }

            return@withContext parsedConfig
        }
    }

    suspend fun clear() = mutex.withLock {
        withContext(dispatcherProvider.IO) {
            storage.setAppConfigRaw(null)

            // We are using Dispatchers IO to make it appropriate
            @Suppress("BlockingMethodInNonBlockingContext")
            server.clearCache()
        }
    }
}
