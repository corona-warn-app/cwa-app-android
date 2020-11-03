package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.KeyDownloadParameters
import javax.inject.Inject

@Reusable
class DownloadConfigMapper @Inject constructor() : ConfigMapper<KeyDownloadConfig> {
    override fun map(rawConfig: AppConfig.ApplicationConfiguration): KeyDownloadConfig {

        return KeyDownloadConfigContainer(
            keyDownloadParameters = rawConfig.androidKeyDownloadParameters
        )
    }

    data class KeyDownloadConfigContainer(
        override val keyDownloadParameters: KeyDownloadParameters.KeyDownloadParametersAndroid
    ) : KeyDownloadConfig
}
