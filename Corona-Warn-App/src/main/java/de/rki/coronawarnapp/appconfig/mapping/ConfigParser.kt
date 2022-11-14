package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.appconfig.LogUploadConfig
import de.rki.coronawarnapp.appconfig.PresenceTracingConfig
import de.rki.coronawarnapp.appconfig.SelfReportSubmissionConfig
import de.rki.coronawarnapp.appconfig.SurveyConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import timber.log.Timber
import javax.inject.Inject

@Suppress("LongParameterList")
@Reusable
class ConfigParser @Inject constructor(
    private val cwaConfigMapper: CWAConfig.Mapper,
    private val keyDownloadConfigMapper: KeyDownloadConfig.Mapper,
    private val exposureDetectionConfigMapper: ExposureDetectionConfig.Mapper,
    private val exposureWindowRiskCalculationConfigMapper: ExposureWindowRiskCalculationConfig.Mapper,
    private val surveyConfigMapper: SurveyConfig.Mapper,
    private val analyticsConfigMapper: AnalyticsConfig.Mapper,
    private val logUploadConfigMapper: LogUploadConfig.Mapper,
    private val presenceTracingConfigMapper: PresenceTracingConfig.Mapper,
    private val coronaTestConfigMapper: CoronaTestConfig.Mapper,
    private val covidCertificateConfigMapper: CovidCertificateConfig.Mapper,
    private val selfReportSubmissionConfigMapper: SelfReportSubmissionConfig.Mapper
) {

    fun parse(configBytes: ByteArray): ConfigMapping = try {
        parseRawArray(configBytes).let {
            DefaultConfigMapping(
                cwaConfig = cwaConfigMapper.map(it),
                keyDownloadConfig = keyDownloadConfigMapper.map(it),
                exposureDetectionConfig = exposureDetectionConfigMapper.map(it),
                exposureWindowRiskCalculationConfig = exposureWindowRiskCalculationConfigMapper.map(it),
                survey = surveyConfigMapper.map(it),
                analytics = analyticsConfigMapper.map(it),
                logUpload = logUploadConfigMapper.map(it),
                presenceTracing = presenceTracingConfigMapper.map(it),
                coronaTestParameters = coronaTestConfigMapper.map(it),
                covidCertificateParameters = covidCertificateConfigMapper.map(it),
                selfReportSubmission = selfReportSubmissionConfigMapper.map(it),
            )
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to parse AppConfig: %s", configBytes)
        throw e
    }

    private fun parseRawArray(configBytes: ByteArray): AppConfigAndroid.ApplicationConfigurationAndroid {
        Timber.v("Parsing config (size=%dB)", configBytes.size)
        return AppConfigAndroid.ApplicationConfigurationAndroid.parseFrom(configBytes)
    }
}
