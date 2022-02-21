package de.rki.coronawarnapp.test.ccl

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.configuration.update.CCLConfigurationUpdater
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class CCLTestViewModel @AssistedInject constructor(
    certificateProvider: CertificateProvider,
    private val dccWalletInfoRepository: DccWalletInfoRepository,
    private val dccWalletInfoCalculationManager: DccWalletInfoCalculationManager,
    private val cclConfigurationUpdater: CCLConfigurationUpdater
) : CWAViewModel() {

    var selectedPersonIdentifier: PersonIdentifierSelection = PersonIdentifierSelection.All
    val dccWalletInfoList = dccWalletInfoRepository.personWallets.asLiveData2()

    val personIdentifiers = certificateProvider.certificateContainer
        .map { certificateContainer ->
            certificateContainer.allCwaCertificates.map { it.personIdentifier }
                .distinct()
                .map { PersonIdentifierSelection.Selected(it) }
                .toMutableList<PersonIdentifierSelection>()
                .apply { add(0, PersonIdentifierSelection.All) }
                .toList()
        }
        .asLiveData2()

    val forceUpdateUiState = MutableLiveData<ForceUpdateUiState>()

    fun clearDccWallet() = launch {
        dccWalletInfoRepository.clear()
    }

    sealed class PersonIdentifierSelection {
        data class Selected(val personIdentifier: CertificatePersonIdentifier) : PersonIdentifierSelection()
        object All : PersonIdentifierSelection()

        fun getCertificatePersonIdentifier() = when (this) {
            is Selected -> personIdentifier
            else -> null
        }
    }

    fun triggerCalculation() = launch {
        selectedPersonIdentifier.getCertificatePersonIdentifier()?.let {
            dccWalletInfoCalculationManager.triggerCalculationForPerson(it)
        } ?: run {
            dccWalletInfoCalculationManager.triggerCalculationAfterConfigChange()
        }
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
