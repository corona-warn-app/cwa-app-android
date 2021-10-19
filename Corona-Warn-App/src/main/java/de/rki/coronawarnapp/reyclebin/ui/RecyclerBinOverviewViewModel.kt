package de.rki.coronawarnapp.reyclebin.ui

import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.reyclebin.RecycledItemsProvider
import de.rki.coronawarnapp.reyclebin.ui.adapter.OverviewSubHeaderItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecoveryCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecyclerBinItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.TestCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.VaccinationCertificateCard
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class RecyclerBinOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val recycledItemsProvider: RecycledItemsProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val currentEvent = SingleLiveEvent<RecyclerBinEvent>()
    val events: LiveData<RecyclerBinEvent> = currentEvent

    private val recycledCertificates = recycledItemsProvider.recycledCertificates

    val listItems: LiveData<List<RecyclerBinItem>> = recycledCertificates
        .map { it.toRecyclerBinItems() }
        .asLiveData2()

    private fun Collection<CwaCovidCertificate>.toRecyclerBinItems(): List<RecyclerBinItem> {
        val certificateItems = mapNotNull { mapCertToCertItem(it) }

        return when (certificateItems.isNotEmpty()) {
            true -> listOf(OverviewSubHeaderItem).plus(certificateItems)
            false -> emptyList()
        }.also { Timber.d("Created recycler bin items=%s from certs=%s", it, this) }
    }

    private fun mapCertToCertItem(cert: CwaCovidCertificate): RecyclerBinItem? = when (cert) {
        is TestCertificate -> TestCertificateCard.Item(
            certificate = cert,
            onRemove = { certificate, position ->
                currentEvent.postValue(RecyclerBinEvent.RemoveItem(certificate, position))
            },
            onRestore = { certificate ->
                currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreItem(certificate))
            }
        )

        is VaccinationCertificate -> VaccinationCertificateCard.Item(
            certificate = cert,
            onRemove = { certificate, position ->
                currentEvent.postValue(RecyclerBinEvent.RemoveItem(certificate, position))
            },
            onRestore = { certificate ->
                currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreItem(certificate))
            }
        )

        is RecoveryCertificate -> RecoveryCertificateCard.Item(
            certificate = cert,
            onRemove = { certificate, position ->
                currentEvent.postValue(RecyclerBinEvent.RemoveItem(certificate, position))
            },
            onRestore = { certificate ->
                currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreItem(certificate))
            }
        )
        else -> null
    }.also { Timber.v("Mapped cert=%s to recycler bin item=%s", cert, it) }

    fun onRemoveAllItemsClicked() {
        Timber.d("onRemoveAllItemsClicked()")
        currentEvent.postValue(RecyclerBinEvent.ConfirmRemoveAll)
    }

    fun onRemoveAllItemsConfirmation() = launch {
        Timber.d("onRemoveAllItemsConfirmation()")
        val itemToDelete = recycledCertificates.first().map { it.containerId }
        recycledItemsProvider.deleteAllCertificate(itemToDelete)
    }

    fun onRemoveItem(item: CwaCovidCertificate) = launch {
        Timber.d("onRemoveSingleItemConfirmation(item=%s)", item)
        recycledItemsProvider.deleteCertificate(item.containerId)
    }

    fun onRestoreConfirmation(item: CwaCovidCertificate) = launch {
        Timber.d("onRestoreConfirmation(item=%s)", item)
        recycledItemsProvider.restoreCertificate(item.containerId)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RecyclerBinOverviewViewModel>
}
