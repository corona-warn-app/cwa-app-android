package de.rki.coronawarnapp.appconfig.sources.fallback

import android.content.Context
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@Reusable
class DefaultAppConfigSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configParser: ConfigParser,
    private val srsDevSettings: SrsDevSettings,
) {

    fun getRawDefaultConfig(): ByteArray {
        return context.assets.open("default_app_config_android.bin").readBytes()
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
