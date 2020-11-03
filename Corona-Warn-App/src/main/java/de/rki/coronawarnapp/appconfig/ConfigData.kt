package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import org.joda.time.Duration
import org.joda.time.Instant

interface ConfigData : ConfigMapping {

    val updatedAt: Instant

    /**
     * serverTime + localOffset = updatedAt
     * If **[isFallback]** returns true,
     * you should probably ignore the time offset.
     */
    val localOffset: Duration

    /**
     * Returns true if this is not a fresh config, e.g.
     * server could not be reached.
     */
    val isFallback: Boolean
}
