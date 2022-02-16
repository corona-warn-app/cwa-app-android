package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper

interface CWAConfig {

    val latestVersionCode: Long

    val minVersionCode: Long

    val supportedCountries: List<String>

    val isDeviceTimeCheckEnabled: Boolean

    val isUnencryptedCheckInsEnabled: Boolean

    val validationServiceMinVersion: Int

    val dccPersonWarnThreshold: Int

    val dccPersonCountMax: Int

    val admissionScenariosDisabled: Boolean

    interface Mapper : ConfigMapper<CWAConfig>
}
