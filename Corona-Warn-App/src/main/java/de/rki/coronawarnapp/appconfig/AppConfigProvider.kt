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
            if (configStorage.isAppConfigAvailable) {
                null
            } else {
                Timber.e("No fallback available, rethrowing!")
                throw e
            }
        }

        val newConfigParsed = try {
            tryParseConfig(newConfigRaw)
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse latest AppConfig.")
            null
        }

        return newConfigParsed?.also {
            Timber.d("Saving new valid config.")
            Timber.v("New Config.supportedCountries: %s", it.supportedCountriesList)
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

        if (sanityChecked.supportedCountriesList == null) {
            sanityChecked = sanityChecked.toNewConfig {
                clearSupportedCountries()
                addAllSupportedCountries(emptyList<String>())
            }
        }

        val countryCheck = sanityChecked.supportedCountriesList
        if (countryCheck.size == 1 && !VALID_CC.matches(countryCheck.single())) {
            Timber.w("Invalid country data, clearing. (%s)", this.supportedCountriesList)
            sanityChecked = sanityChecked.toNewConfig {
                clearSupportedCountries()
            }
        }
        return sanityChecked
    }

    companion object {
        private val VALID_CC = "^([A-Z]{2,3})$".toRegex()
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
        private val TAG = AppConfigProvider::class.java.simpleName
    }
}
