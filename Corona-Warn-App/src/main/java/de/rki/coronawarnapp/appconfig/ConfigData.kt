package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import org.joda.time.Duration
import org.joda.time.Instant

interface ConfigData : ConfigMapping {

    /**
     * A unique value to identify this app config by.
     * When this value changes, the app config has changed.
     */
    val identifier: String

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
        LAST_RETRIEVED,

        /**
         * Last resort, default config shipped with the app.
         */
        LOCAL_DEFAULT
    }
}
