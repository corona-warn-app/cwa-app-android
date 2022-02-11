package de.rki.coronawarnapp.ccl.configuration.storage

import dagger.Reusable
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import javax.inject.Inject

@Reusable
class CCLConfigurationMerger @Inject constructor() {

    fun merge(
        defaultConfigurationList: List<CCLConfiguration>,
        downloadedConfigurationList: List<CCLConfiguration>
    ): List<CCLConfiguration> {
        return defaultConfigurationList.map { defaultConfig ->
            downloadedConfigurationList.firstOrNull { downloadedConfig ->
                downloadedConfig.identifier == defaultConfig.identifier
            } ?: defaultConfig
        }
    }
}
