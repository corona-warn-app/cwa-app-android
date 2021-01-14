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
     * How much our device time differs from the servers time (UTC vs UTC comparison)
     */
    val localOffset: Duration

    /**
     * Is this device's time within **[DEVICE_TIME_GRACE_RANGE]** of the server's time?
     */
    val isDeviceTimeCorrect: Boolean

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

    /**
     * Has the config validity expired?
     * Is this configs update date, past the maximum cache age?
     */
    fun isValid(nowUTC: Instant): Boolean

    companion object {
        val DEVICE_TIME_GRACE_RANGE: Duration = Duration.standardHours(2)
    }
}
