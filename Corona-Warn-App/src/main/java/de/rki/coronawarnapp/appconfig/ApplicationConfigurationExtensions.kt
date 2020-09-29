package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration

fun ApplicationConfiguration.toNewConfig(
    action: ApplicationConfiguration.Builder.() -> Unit
): ApplicationConfiguration {
    val builder = this.toBuilder()
    action(builder)
    return builder.build()
}
