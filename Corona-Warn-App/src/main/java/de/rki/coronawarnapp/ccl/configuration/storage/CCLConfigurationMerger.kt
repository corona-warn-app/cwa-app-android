package de.rki.coronawarnapp.ccl.configuration.storage

import dagger.Reusable
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import javax.inject.Inject

@Reusable
class CCLConfigurationMerger @Inject constructor() {

    fun merge(
        defaultConfigList: List<CCLConfiguration>,
        downloadedConfigList: List<CCLConfiguration>
    ): List<CCLConfiguration> {

        val defaultConfigsNotInDownloadedConfigs = defaultConfigList.filter { defaultConfig ->
            downloadedConfigList.none { downloadedConfig ->
                downloadedConfig.identifier == defaultConfig.identifier
            }
        }

        return downloadedConfigList + defaultConfigsNotInDownloadedConfigs
    }
}
