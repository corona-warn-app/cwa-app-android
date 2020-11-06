package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import org.joda.time.Duration
import org.joda.time.Instant

interface ConfigData : ConfigMapping {

    /**
     * serverTime + localOffset = updatedAt
     */
    val updatedAt: Instant

    /**
     * If **[configType]** is not **[Type.FROM_SERVER]**,
     * you should probably ignore the time offset.
     */
    val localOffset: Duration

    /**
     * Returns the type config this is.
     */
    val configType: Type

    enum class Type {
        /**
         * Fresh one from a server.
         */
        FROM_SERVER,

        /**
         * Server config locally stored.
         */
        FALLBACK_LAST_RETRIEVED,

        /**
         * Last resort, default config shipped with the app.
         */
        FALLBACK_LOCAL_DEFAULT
    }
}
