package de.rki.coronawarnapp.test.ccl

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.ccl.configuration.update.CclConfigurationUpdater
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class CclTestViewModel @Inject constructor(
    private val dccWalletInfoRepository: DccWalletInfoRepository,
    private val dccWalletInfoCalculationManager: DccWalletInfoCalculationManager,
    private val cclConfigurationUpdater: CclConfigurationUpdater,
    private val personCertificatesSettings: PersonCertificatesSettings,
) : CWAViewModel() {

    val dccWalletInfoList = dccWalletInfoRepository.personWallets.asLiveData2()

    val forceUpdateUiState = MutableLiveData<ForceUpdateUiState>()

    fun clearDccWallet() = launch {
        dccWalletInfoRepository.reset()
        personCertificatesSettings.reset()
    }

    fun triggerCalculation() = launch {
        dccWalletInfoCalculationManager.triggerAfterConfigChange("")
    }

    fun forceUpdateCclConfiguration() = launch {
        forceUpdateUiState.postValue(ForceUpdateUiState.Loading)
        cclConfigurationUpdater.forceUpdate()
        forceUpdateUiState.postValue(ForceUpdateUiState.Success)
    }

    sealed class ForceUpdateUiState {
        object Loading : ForceUpdateUiState()
        object Success : ForceUpdateUiState()
    }
}
