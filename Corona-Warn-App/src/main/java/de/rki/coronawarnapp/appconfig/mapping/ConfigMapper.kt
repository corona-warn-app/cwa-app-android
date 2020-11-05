package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.AppConfig

interface ConfigMapper<T> {
    fun map(rawConfig: AppConfig.ApplicationConfiguration): T
}
