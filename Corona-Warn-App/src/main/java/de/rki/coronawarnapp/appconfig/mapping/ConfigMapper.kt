package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid

interface ConfigMapper<T> {
    fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): T
}
