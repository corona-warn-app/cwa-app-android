package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import javax.inject.Inject

@Reusable
class DownloadConfigMapper @Inject constructor() : KeyDownloadConfig.Mapper {
    override fun map(rawConfig: AppConfig.ApplicationConfiguration): KeyDownloadConfig {
        return KeyDownloadConfigContainer()
    }

    class KeyDownloadConfigContainer : KeyDownloadConfig
}
