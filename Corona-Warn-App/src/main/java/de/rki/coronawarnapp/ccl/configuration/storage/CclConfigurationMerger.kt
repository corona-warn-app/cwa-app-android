package de.rki.coronawarnapp.ccl.configuration.storage

import dagger.Reusable
import de.rki.coronawarnapp.ccl.configuration.model.CclConfiguration
import javax.inject.Inject

@Reusable
class CclConfigurationMerger @Inject constructor() {

    fun merge(
        defaultConfigList: List<CclConfiguration>,
        downloadedConfigList: List<CclConfiguration>
    ): List<CclConfiguration> {

        val defaultConfigsNotInDownloadedConfigs = defaultConfigList.filter { defaultConfig ->
            downloadedConfigList.none { downloadedConfig ->
                downloadedConfig.identifier == defaultConfig.identifier
            }
        }

        return downloadedConfigList + defaultConfigsNotInDownloadedConfigs
    }
}
