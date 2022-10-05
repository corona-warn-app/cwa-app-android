package de.rki.coronawarnapp.test.dsc.ui

import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.expiration.DccValidityStateNotificationService
import de.rki.coronawarnapp.covidcertificate.revocation.storage.DccRevocationRepository
import de.rki.coronawarnapp.covidcertificate.revocation.update.DccRevocationListUpdater
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.time.Instant

class DccStateValidationTestViewModel @AssistedInject constructor(
    private val dscRepository: DscRepository,
    private val covidCertificateSettings: CovidCertificateSettings,
    private val dccValidityStateNotificationService: DccValidityStateNotificationService,
    private val dccRevocationRepository: DccRevocationRepository,
    private val dccRevocationListUpdater: DccRevocationListUpdater
) : CWAViewModel() {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccStateValidationTestViewModel>

    val errorEvent = SingleLiveEvent<Unit>()

    private val searchTerm = MutableStateFlow("")

    val dscData: LiveData<DscDataInfo> = searchTerm.combine(dscRepository.dscSignatureList) { searchTerm, dscList ->
        DscDataInfo(
            lastUpdate = if (dscList.updatedAt == Instant.EPOCH)
                "NEVER (using default_dsc_list)"
            else
                dscList.updatedAt.toString(),
            listSize = dscList.dscList.size,
            searchResults = dscList.dscList
                .filter { it.kid.startsWith(searchTerm, true) }
                .map { it.kid }
        )
    }.asLiveData2()

    fun updateSearchTerm(value: String) {
        searchTerm.value = value
    }

    fun clearDscList() {
        launch {
            dscRepository.reset()
        }
    }

    fun refresh() {
        launch {
            try {
                dscRepository.refresh()
            } catch (e: Exception) {
                errorEvent.postValue(Unit)
            }
        }
    }

    fun resetLastCheckTime() = launch {
        covidCertificateSettings.updateLastDccStateBackgroundCheck(Instant.EPOCH)
    }

    fun checkValidityNotifications() = launch {
        covidCertificateSettings.updateLastDccStateBackgroundCheck(Instant.EPOCH)
        dccValidityStateNotificationService.showNotificationIfStateChanged()
    }

    fun refreshRevocationList() = launch {
        dccRevocationListUpdater.updateRevocationList(true)
    }

    fun clearRevocationList() = launch {
        dccRevocationRepository.clear()
    }

    data class DscDataInfo(
        val lastUpdate: String,
        val listSize: Int,
        val searchResults: List<String>
    )
}
