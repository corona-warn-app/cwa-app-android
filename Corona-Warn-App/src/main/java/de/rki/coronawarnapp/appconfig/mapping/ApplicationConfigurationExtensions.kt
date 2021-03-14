package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.AppConfig.ApplicationConfiguration

fun ApplicationConfiguration.toNewConfig(
    action: ApplicationConfiguration.Builder.() -> Unit
): ApplicationConfiguration {
    val builder = this.toBuilder()
    action(builder)
    return builder.build()
}
