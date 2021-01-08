package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid

interface ConfigMapping :
    CWAConfig,
    KeyDownloadConfig,
    ExposureDetectionConfig,
    ExposureWindowRiskCalculationConfig {

    @Deprecated("Try to access a more specific config type, avoid the RAW variant.")
    val rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid
}
