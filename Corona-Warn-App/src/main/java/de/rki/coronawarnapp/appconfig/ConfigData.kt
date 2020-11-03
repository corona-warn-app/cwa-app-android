package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import org.joda.time.Instant

interface ConfigData : ConfigMapping {

    val updatedAt: Instant
}
