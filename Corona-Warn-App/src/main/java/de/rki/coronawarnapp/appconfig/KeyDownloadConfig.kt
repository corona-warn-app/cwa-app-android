package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.server.protocols.internal.AppConfig.ApplicationConfiguration

interface KeyDownloadConfig {

    interface Mapper {
        fun map(rawConfig: ApplicationConfiguration): KeyDownloadConfig
    }
}
