package de.rki.coronawarnapp.appconfig.mapping

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

data class DefaultConfigMapping(
    val cwaConfig: CWAConfig,
    val keyDownloadConfig: KeyDownloadConfig,
    val exposureDetectionConfig: ExposureDetectionConfig,
    val exposureWindowRiskCalculationConfig: ExposureWindowRiskCalculationConfig,
    override val survey: SurveyConfig,
    override val analytics: AnalyticsConfig,
    override val logUpload: LogUploadConfig,
    override val presenceTracing: PresenceTracingConfig,
    override val coronaTestParameters: CoronaTestConfig,
    override val covidCertificateParameters: CovidCertificateConfig,
    override val selfReportSubmission: SelfReportSubmissionConfig,
) : ConfigMapping,
    CWAConfig by cwaConfig,
    KeyDownloadConfig by keyDownloadConfig,
    ExposureDetectionConfig by exposureDetectionConfig,
    ExposureWindowRiskCalculationConfig by exposureWindowRiskCalculationConfig
