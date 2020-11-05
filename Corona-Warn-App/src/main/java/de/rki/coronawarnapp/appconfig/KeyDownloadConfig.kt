package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import de.rki.coronawarnapp.server.protocols.internal.KeyDownloadParameters

interface KeyDownloadConfig {

    val keyDownloadParameters: KeyDownloadParameters.KeyDownloadParametersAndroid

    interface Mapper : ConfigMapper<KeyDownloadConfig>
}
