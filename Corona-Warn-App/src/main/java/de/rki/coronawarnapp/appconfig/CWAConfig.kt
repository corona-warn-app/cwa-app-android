package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import de.rki.coronawarnapp.server.protocols.internal.AppFeaturesOuterClass
import de.rki.coronawarnapp.server.protocols.internal.AppVersionConfig

interface CWAConfig {

    val appVersion: AppVersionConfig.ApplicationVersionConfiguration

    val supportedCountries: List<String>

    val appFeatureus: AppFeaturesOuterClass.AppFeatures

    interface Mapper : ConfigMapper<CWAConfig>
}
