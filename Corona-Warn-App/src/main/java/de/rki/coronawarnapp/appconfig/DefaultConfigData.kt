package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import org.joda.time.Instant

data class DefaultConfigData(
    override val updatedAt: Instant = Instant.EPOCH,
    val mappedConfig: ConfigMapping
) : ConfigData, ConfigMapping by mappedConfig
