package de.rki.coronawarnapp.appconfig

import androidx.annotation.VisibleForTesting
import dagger.Lazy
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.environment.download.DownloadCDNHomeCountry
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.VerificationKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigProvider @Inject constructor(
    private val appConfigAPI: Lazy<AppConfigApiV1>,
    private val verificationKeys: VerificationKeys,
    @DownloadCDNHomeCountry private val homeCountry: LocationCode,
    private val configStorage: AppConfigStorage
) {

    private val configApi: AppConfigApiV1
        get() = appConfigAPI.get()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun downloadAppConfig(): ByteArray? {
        Timber.tag(TAG).d("Fetching app config.")
        var exportBinary: ByteArray? = null
        var exportSignature: ByteArray? = null
        configApi.getApplicationConfiguration(homeCountry.identifier).byteStream()
            .unzip { entry, entryContent ->
                if (entry.name == EXPORT_BINARY_FILE_NAME) exportBinary =
                    entryContent.copyOf()
                if (entry.name == EXPORT_SIGNATURE_FILE_NAME) exportSignature =
                    entryContent.copyOf()
            }
        if (exportBinary == null || exportSignature == null) {
            throw ApplicationConfigurationInvalidException()
        }

        if (verificationKeys.hasInvalidSignature(exportBinary, exportSignature)) {
            throw ApplicationConfigurationCorruptException()
        }

        return exportBinary!!
    }

    private fun tryParseConfig(byteArray: ByteArray?): ApplicationConfiguration? {
        Timber.v("Parsing config (size=%dB)", byteArray?.size)
        if (byteArray == null) return null
        return ApplicationConfiguration.parseFrom(byteArray)
    }

    private suspend fun getNewAppConfig(): ApplicationConfiguration? {
        val newConfigRaw = try {
            downloadAppConfig()
        } catch (e: Exception) {
            Timber.w(e, "Failed to download latest AppConfig.")
            null
        }

        val newConfigParsed = try {
            tryParseConfig(newConfigRaw)
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse latest AppConfig.")
            null
        }

        return newConfigParsed?.also {
            Timber.v("Saving new valid config.")
            configStorage.appConfigRaw = newConfigRaw
        }
    }

    private fun getFallback(): ApplicationConfiguration {
        val lastValidConfig = tryParseConfig(configStorage.appConfigRaw)
        return if (lastValidConfig != null) {
            Timber.d("Using fallback AppConfig.")
            lastValidConfig
        } else {
            Timber.e("No valid fallback AppConfig available.")
            throw ApplicationConfigurationInvalidException()
        }
    }

    suspend fun getAppConfig(): ApplicationConfiguration = withContext(Dispatchers.IO) {
        val newAppConfig = getNewAppConfig()

        return@withContext if (newAppConfig != null) {
            newAppConfig
        } else {
            Timber.w("No new config available, using last valid.")
            getFallback()
        }
    }.performSanityChecks()

    private fun ApplicationConfiguration.performSanityChecks(): ApplicationConfiguration {
        var sanityChecked = this

        if (sanityChecked.supportedCountriesList.isEmpty()) {
            sanityChecked = sanityChecked.toNewConfig {
                addSupportedCountries(FALLBACK_COUNTRY)
            }
            Timber.w("Country list was empty, corrected: %s", sanityChecked.supportedCountriesList)
        }

        return sanityChecked
    }

    companion object {
        private val TAG = AppConfigProvider::class.java.simpleName

        private const val FALLBACK_COUNTRY = "DE"
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
    }
}
