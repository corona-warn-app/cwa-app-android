package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import org.joda.time.Duration
import org.joda.time.Instant

data class DefaultConfigData(
    val serverTime: Instant,
    val mappedConfig: ConfigMapping,
    override val localOffset: Duration,
    override val configType: ConfigData.Type
) : ConfigData, ConfigMapping by mappedConfig {
    override val updatedAt: Instant = serverTime.plus(localOffset)
}
