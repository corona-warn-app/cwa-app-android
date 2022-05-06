package de.rki.coronawarnapp.ccl.reset

import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.ccl.configuration.storage.DownloadedCclConfigurationStorage
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.util.reset.Resettable
import timber.log.Timber
import javax.inject.Inject

class CclReset @Inject constructor(
    private val cclSettings: CclSettings,
    private val downloadedCclConfigurationStorage: DownloadedCclConfigurationStorage,
    private val cclConfigurationRepository: CclConfigurationRepository,
    private val cclWalletInfoRepository: DccWalletInfoRepository
) : Resettable {

    override suspend fun reset() {
        Timber.d("reset()")
        cclSettings.reset()
        downloadedCclConfigurationStorage.reset()
        cclConfigurationRepository.reset()
        cclWalletInfoRepository.reset()
    }
}
