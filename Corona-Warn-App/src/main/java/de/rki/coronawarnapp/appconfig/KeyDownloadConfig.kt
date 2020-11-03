package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.server.protocols.internal.AppConfig.ApplicationConfiguration
import de.rki.coronawarnapp.server.protocols.internal.KeyDownloadParameters

interface KeyDownloadConfig {

    val keyDownloadParameters: KeyDownloadParameters.KeyDownloadParametersAndroid

    interface Mapper {
        fun map(rawConfig: ApplicationConfiguration): KeyDownloadConfig
    }
}
