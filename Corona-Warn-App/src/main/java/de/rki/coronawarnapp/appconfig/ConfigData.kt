package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import java.time.Duration
import java.time.Instant

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

    val deviceTimeState: DeviceTimeState

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

    enum class DeviceTimeState(val key: String) {
        /**
         * Device time was compared against server time and deemed correct
         */
        CORRECT("CORRECT"),

        /**
         * Device time was not compared against server time for various reasons
         */
        ASSUMED_CORRECT("ASSUMED_CORRECT"),

        /**
         * Device time was compared against server time and deemed incorrect
         */
        INCORRECT("INCORRECT")
    }

    companion object {
        val DEVICE_TIME_GRACE_RANGE: Duration = Duration.ofHours(2)
    }
}
