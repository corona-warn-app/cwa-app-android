package de.rki.coronawarnapp.service.applicationconfiguration

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.protobuf.InvalidProtocolBufferException
import de.rki.coronawarnapp.exception.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.exception.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.http.service.DistributionService
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.VerificationKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val EXPORT_BINARY_FILE_NAME = "export.bin"
private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"

class ApplicationConfigurationService @Inject constructor(
    val distributionService: DistributionService,
    val verificationKeys: VerificationKeys
) {

    suspend fun asyncRetrieveApplicationConfiguration(): ApplicationConfiguration {
        return withContext(Dispatchers.IO) {
            var exportBinary: ByteArray? = null
            var exportSignature: ByteArray? = null

            distributionService.getApplicationConfiguration(
                DiagnosisKeyConstants.COUNTRY_APPCONFIG_DOWNLOAD_URL
            ).byteStream().unzip { entry, entryContent ->
                if (entry.name == EXPORT_BINARY_FILE_NAME) exportBinary = entryContent.copyOf()
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
                return@withContext ApplicationConfiguration.parseFrom(exportBinary)
            } catch (e: InvalidProtocolBufferException) {
                throw ApplicationConfigurationInvalidException()
            }
        }
    }

    suspend fun asyncRetrieveExposureConfiguration(): ExposureConfiguration =
        asyncRetrieveApplicationConfiguration()
            .mapRiskScoreToExposureConfiguration()

    private fun ApplicationConfiguration.mapRiskScoreToExposureConfiguration(): ExposureConfiguration =
        ExposureConfiguration
            .ExposureConfigurationBuilder()
            .setTransmissionRiskScores(
                this.exposureConfig.transmission.appDefined1Value,
                this.exposureConfig.transmission.appDefined2Value,
                this.exposureConfig.transmission.appDefined3Value,
                this.exposureConfig.transmission.appDefined4Value,
                this.exposureConfig.transmission.appDefined5Value,
                this.exposureConfig.transmission.appDefined6Value,
                this.exposureConfig.transmission.appDefined7Value,
                this.exposureConfig.transmission.appDefined8Value
            )
            .setDurationScores(
                this.exposureConfig.duration.eq0MinValue,
                this.exposureConfig.duration.gt0Le5MinValue,
                this.exposureConfig.duration.gt5Le10MinValue,
                this.exposureConfig.duration.gt10Le15MinValue,
                this.exposureConfig.duration.gt15Le20MinValue,
                this.exposureConfig.duration.gt20Le25MinValue,
                this.exposureConfig.duration.gt25Le30MinValue,
                this.exposureConfig.duration.gt30MinValue
            )
            .setDaysSinceLastExposureScores(
                this.exposureConfig.daysSinceLastExposure.ge14DaysValue,
                this.exposureConfig.daysSinceLastExposure.ge12Lt14DaysValue,
                this.exposureConfig.daysSinceLastExposure.ge10Lt12DaysValue,
                this.exposureConfig.daysSinceLastExposure.ge8Lt10DaysValue,
                this.exposureConfig.daysSinceLastExposure.ge6Lt8DaysValue,
                this.exposureConfig.daysSinceLastExposure.ge4Lt6DaysValue,
                this.exposureConfig.daysSinceLastExposure.ge2Lt4DaysValue,
                this.exposureConfig.daysSinceLastExposure.ge0Lt2DaysValue
            )
            .setAttenuationScores(
                this.exposureConfig.attenuation.gt73DbmValue,
                this.exposureConfig.attenuation.gt63Le73DbmValue,
                this.exposureConfig.attenuation.gt51Le63DbmValue,
                this.exposureConfig.attenuation.gt33Le51DbmValue,
                this.exposureConfig.attenuation.gt27Le33DbmValue,
                this.exposureConfig.attenuation.gt15Le27DbmValue,
                this.exposureConfig.attenuation.gt10Le15DbmValue,
                this.exposureConfig.attenuation.le10DbmValue
            )
            .setMinimumRiskScore(this.minRiskScore)
            .setDurationAtAttenuationThresholds(
                this.attenuationDuration.thresholds.lower,
                this.attenuationDuration.thresholds.upper
            )
            .build()
}
