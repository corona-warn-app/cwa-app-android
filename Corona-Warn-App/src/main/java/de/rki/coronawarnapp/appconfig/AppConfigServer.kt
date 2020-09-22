package de.rki.coronawarnapp.appconfig

import com.google.protobuf.InvalidProtocolBufferException
import dagger.Lazy
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.environment.download.DownloadCDNHomeCountry
import de.rki.coronawarnapp.exception.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.exception.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.VerificationKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigServer @Inject constructor(
    private val appConfigAPI: Lazy<AppConfigApiV1>,
    private val verificationKeys: VerificationKeys,
    @DownloadCDNHomeCountry private val homeCountry: LocationCode
) {

    private val configApi: AppConfigApiV1
        get() = appConfigAPI.get()

    suspend fun downloadAppConfig(): ApplicationConfigurationOuterClass.ApplicationConfiguration =
        withContext(Dispatchers.IO) {
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

            try {
                return@withContext ApplicationConfigurationOuterClass.ApplicationConfiguration.parseFrom(
                    exportBinary
                )
            } catch (e: InvalidProtocolBufferException) {
                throw ApplicationConfigurationInvalidException()
            }
        }

    companion object {
        private val TAG = AppConfigServer::class.java.simpleName

        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
    }
}
