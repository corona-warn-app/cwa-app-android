package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.appconfig.RiskCalculationConfig
import de.rki.coronawarnapp.server.protocols.internal.AppConfig

interface ConfigMapping :
    CWAConfig,
    KeyDownloadConfig,
    ExposureDetectionConfig,
    RiskCalculationConfig {

    @Deprecated("Try to access a more specific config type, avoid the RAW variant.")
    val rawConfig: AppConfig.ApplicationConfiguration
}
