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
    override val updatedAt: Instant = serverTime.plus(localOffset)

    override fun isValid(nowUTC: Instant): Boolean {
        val expiresAt = updatedAt.plus(cacheValidity)
        return nowUTC.isBefore(expiresAt)
    }
}
