package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import de.rki.coronawarnapp.server.protocols.internal.v2.AppFeaturesOuterClass

interface CWAConfig {

    val latestVersionCode: Long

    val minVersionCode: Long

    val supportedCountries: List<String>

    val appFeatures: List<AppFeaturesOuterClass.AppFeature>

    interface Mapper : ConfigMapper<CWAConfig>
}
