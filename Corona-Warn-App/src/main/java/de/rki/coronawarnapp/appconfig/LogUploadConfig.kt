package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper

interface LogUploadConfig {

    val safetyNetRequirements: SafetyNetRequirements

    interface Mapper : ConfigMapper<LogUploadConfig>
}
