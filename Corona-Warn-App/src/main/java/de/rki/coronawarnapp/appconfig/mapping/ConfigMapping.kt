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
import de.rki.coronawarnapp.appconfig.SurveyConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid

interface ConfigMapping :
    CWAConfig,
    KeyDownloadConfig,
    ExposureDetectionConfig,
    ExposureWindowRiskCalculationConfig {

    val survey: SurveyConfig
    val analytics: AnalyticsConfig
    val logUpload: LogUploadConfig
    val presenceTracing: PresenceTracingConfig
    val coronaTestParameters: CoronaTestConfig
    val covidCertificateParameters: CovidCertificateConfig
}
