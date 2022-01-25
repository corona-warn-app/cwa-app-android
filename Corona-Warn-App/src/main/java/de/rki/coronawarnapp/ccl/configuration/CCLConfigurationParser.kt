package de.rki.coronawarnapp.ccl.configuration

import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import javax.inject.Inject

class CCLConfigurationParser @Inject constructor() {

    fun parseCClConfiguration(rawData: String): CCLConfiguration {
        throw NotImplementedError()
    }
}
