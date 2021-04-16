package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.appconfig.LogUploadConfig
import de.rki.coronawarnapp.appconfig.PresenceTracingConfig
import de.rki.coronawarnapp.appconfig.SurveyConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid

data class DefaultConfigMapping(
    override val rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid,
    val cwaConfig: CWAConfig,
    val keyDownloadConfig: KeyDownloadConfig,
    val exposureDetectionConfig: ExposureDetectionConfig,
    val exposureWindowRiskCalculationConfig: ExposureWindowRiskCalculationConfig,
    override val survey: SurveyConfig,
    override val analytics: AnalyticsConfig,
    override val logUpload: LogUploadConfig,
    override val presenceTracing: PresenceTracingConfig,
    override val coronaTestParameters: CoronaTestConfig,
) : ConfigMapping,
    CWAConfig by cwaConfig,
    KeyDownloadConfig by keyDownloadConfig,
    ExposureDetectionConfig by exposureDetectionConfig,
    ExposureWindowRiskCalculationConfig by exposureWindowRiskCalculationConfig
