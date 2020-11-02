package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.server.protocols.internal.AppConfig

interface ConfigContainerKey : CWAConfig,
    KeyDownloadConfig,
    ExposureDetectionConfig,
    RiskCalculationConfig {

    @Deprecated("Try to access a more specific config type, avoid the RAW variant.")
    val rawConfig: AppConfig.ApplicationConfiguration
}
