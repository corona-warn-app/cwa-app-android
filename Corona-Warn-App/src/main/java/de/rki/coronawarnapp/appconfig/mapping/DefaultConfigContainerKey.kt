package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.ConfigContainerKey
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.appconfig.RiskCalculationConfig
import de.rki.coronawarnapp.server.protocols.internal.AppConfig

data class DefaultConfigContainerKey(
    override val rawConfig: AppConfig.ApplicationConfiguration,
    val cwaConfig: CWAConfig,
    val keyDownloadConfig: KeyDownloadConfig,
    val exposureDetectionConfig: ExposureDetectionConfig,
    val riskCalculationConfig: RiskCalculationConfig
) : ConfigContainerKey,
    CWAConfig by cwaConfig,
    KeyDownloadConfig by keyDownloadConfig,
    ExposureDetectionConfig by exposureDetectionConfig,
    RiskCalculationConfig by riskCalculationConfig
