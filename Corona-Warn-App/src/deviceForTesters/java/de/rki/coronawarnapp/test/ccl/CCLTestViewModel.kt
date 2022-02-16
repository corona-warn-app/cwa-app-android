package de.rki.coronawarnapp.test.ccl

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.configuration.update.CCLConfigurationUpdater
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class CCLTestViewModel @AssistedInject constructor(
    private val dccWalletInfoRepository: DccWalletInfoRepository,
    private val dccWalletInfoCalculationManager: DccWalletInfoCalculationManager,
    private val cclConfigurationUpdater: CCLConfigurationUpdater
) : CWAViewModel() {

    val dccWalletInfoList = dccWalletInfoRepository.personWallets.asLiveData2()

    val forceUpdateUiState = MutableLiveData<ForceUpdateUiState>()

    fun clearDccWallet() = launch {
        dccWalletInfoRepository.clear()
    }

    fun triggerCalculation() = launch {
        dccWalletInfoCalculationManager.triggerCalculationAfterConfigChange("")
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

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CCLTestViewModel>
}
