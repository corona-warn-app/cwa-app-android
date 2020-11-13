package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ConfigParser @Inject constructor(
    private val cwaConfigMapper: CWAConfig.Mapper,
    private val keyDownloadConfigMapper: KeyDownloadConfig.Mapper,
    private val exposureDetectionConfigMapper: ExposureDetectionConfig.Mapper,
    private val exposureWindowRiskCalculationConfigMapper: ExposureWindowRiskCalculationConfig.Mapper
) {

    fun parse(configBytes: ByteArray): ConfigMapping = try {
        parseRawArray(configBytes).let {
            DefaultConfigMapping(
                rawConfig = it,
                cwaConfig = cwaConfigMapper.map(it),
                keyDownloadConfig = keyDownloadConfigMapper.map(it),
                exposureDetectionConfig = exposureDetectionConfigMapper.map(it),
                exposureWindowRiskCalculationConfig = exposureWindowRiskCalculationConfigMapper.map(it)
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
