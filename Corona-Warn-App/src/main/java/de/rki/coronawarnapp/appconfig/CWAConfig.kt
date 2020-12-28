package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper

interface CWAConfig {

    val latestVersionCode: Long

    val minVersionCode: Long

    val supportedCountries: List<String>

    val isDeviceTimeCheckEnabled: Boolean

    interface Mapper : ConfigMapper<CWAConfig>
}
