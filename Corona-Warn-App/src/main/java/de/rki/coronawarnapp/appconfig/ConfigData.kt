package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import org.joda.time.Duration
import org.joda.time.Instant

interface ConfigData : ConfigMapping {

    val updatedAt: Instant

    /**
     * serverTime + localOffset = updatedAt
     */
    val localOffset: Duration
}
