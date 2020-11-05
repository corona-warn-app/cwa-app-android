package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.appconfig.RiskCalculationConfig
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ConfigParser @Inject constructor(
    private val cwaConfigMapper: CWAConfig.Mapper,
    private val keyDownloadConfigMapper: KeyDownloadConfig.Mapper,
    private val exposureDetectionConfigMapper: ExposureDetectionConfig.Mapper,
    private val riskCalculationConfigMapper: RiskCalculationConfig.Mapper,
    private val exposureWindowRiskCalculationConfigMapper: ExposureWindowRiskCalculationConfig.Mapper
) {

    fun parse(configBytes: ByteArray): ConfigMapping = try {
        // TODO replace with actual v2 config
        val dummyConfig = AppConfigAndroid
            .ApplicationConfigurationAndroid
            .newBuilder()
            .build()
        parseRawArray(configBytes).let {
            DefaultConfigMapping(
                rawConfig = it,
                cwaConfig = cwaConfigMapper.map(it),
                keyDownloadConfig = keyDownloadConfigMapper.map(it),
                exposureDetectionConfig = exposureDetectionConfigMapper.map(it),
                riskCalculationConfig = riskCalculationConfigMapper.map(it),
                exposureWindowRiskCalculationConfig = exposureWindowRiskCalculationConfigMapper.map(dummyConfig)
            )
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to parse AppConfig: %s", configBytes)
        throw e
    }

    private fun parseRawArray(configBytes: ByteArray): AppConfig.ApplicationConfiguration {
        Timber.v("Parsing config (size=%dB)", configBytes.size)
        return AppConfig.ApplicationConfiguration.parseFrom(configBytes)
    }
}
