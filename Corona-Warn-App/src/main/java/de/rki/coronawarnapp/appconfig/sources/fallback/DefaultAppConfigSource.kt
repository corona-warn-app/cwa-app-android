package de.rki.coronawarnapp.appconfig.sources.fallback

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.util.di.AppContext
import org.joda.time.Duration
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class DefaultAppConfigSource @Inject constructor(
    @AppContext private val context: Context,
    private val configParser: ConfigParser
) {

    fun getRawDefaultConfig(): ByteArray {
        return context.assets.open("default_app_config.bin").readBytes()
    }

    fun getConfigData(): ConfigData = ConfigDataContainer(
        mappedConfig = configParser.parse(getRawDefaultConfig()),
        serverTime = Instant.EPOCH,
        localOffset = Duration.ZERO,
        identifier = "fallback.local",
        configType = ConfigData.Type.LOCAL_DEFAULT,
        cacheValidity = Duration.ZERO
    )
}
