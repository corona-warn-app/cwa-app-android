package de.rki.coronawarnapp.appconfig.download

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.environment.download.DownloadCDNHomeCountry
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.VerificationKeys
import okhttp3.Cache
import timber.log.Timber
import javax.inject.Inject

@Reusable
class AppConfigServer @Inject constructor(
    private val api: Lazy<AppConfigApiV1>,
    private val verificationKeys: VerificationKeys,
    @DownloadCDNHomeCountry private val homeCountry: LocationCode,
    @AppConfigHttpCache private val cache: Cache
) {

    internal suspend fun downloadAppConfig(): ByteArray {
        Timber.tag(TAG).d("Fetching app config.")
        var exportBinary: ByteArray? = null
        var exportSignature: ByteArray? = null
        api.get().getApplicationConfiguration(homeCountry.identifier).byteStream()
            .unzip { entry, entryContent ->
                if (entry.name == EXPORT_BINARY_FILE_NAME) {
                    exportBinary = entryContent.copyOf()
                }
                if (entry.name == EXPORT_SIGNATURE_FILE_NAME) {
                    exportSignature = entryContent.copyOf()
                }
            }
        if (exportBinary == null || exportSignature == null) {
            throw ApplicationConfigurationInvalidException()
        }

        if (verificationKeys.hasInvalidSignature(exportBinary, exportSignature)) {
            throw ApplicationConfigurationCorruptException()
        }

        return exportBinary!!
    }

    internal fun clearCache() {
        Timber.tag(TAG).v("clearCache()")
        cache.evictAll()
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
        private val TAG = AppConfigServer::class.java.simpleName
    }
}
