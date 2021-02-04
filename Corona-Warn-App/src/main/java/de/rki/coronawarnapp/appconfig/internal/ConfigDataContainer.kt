package de.rki.coronawarnapp.appconfig.internal

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import org.joda.time.Duration
import org.joda.time.Instant

data class ConfigDataContainer(
    val serverTime: Instant,
    val cacheValidity: Duration,
    val mappedConfig: ConfigMapping,
    override val identifier: String,
    override val localOffset: Duration,
    override val configType: ConfigData.Type
) : ConfigData, ConfigMapping by mappedConfig {

    override val isDeviceTimeCorrect: Boolean
        get() = deviceTimeState != ConfigData.DeviceTimeState.INCORRECT

    override val deviceTimeState: ConfigData.DeviceTimeState
        get() = when {
            !isDeviceTimeCheckEnabled -> ConfigData.DeviceTimeState.ASSUMED_CORRECT
            localOffset.abs() < ConfigData.DEVICE_TIME_GRACE_RANGE -> ConfigData.DeviceTimeState.CORRECT
            else -> ConfigData.DeviceTimeState.INCORRECT
        }

    override val updatedAt: Instant = serverTime.plus(localOffset)

    override fun isValid(nowUTC: Instant): Boolean = if (cacheValidity == Duration.ZERO) {
        false
    } else {
        Duration(nowUTC, updatedAt).abs() <= cacheValidity
    }
}
