package de.rki.coronawarnapp.reyclebin.ui

import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.reyclebin.ui.adapter.OverviewSubHeaderItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecoveryCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecyclerBinItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.TestCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.VaccinationCertificateCard
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import timber.log.Timber

class RecyclerBinOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val recycledCertificatesProvider: RecycledCertificatesProvider,
    recycledCoronaTestsProvider: RecycledCoronaTestsProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val currentEvent = SingleLiveEvent<RecyclerBinEvent>()
    val events: LiveData<RecyclerBinEvent> = currentEvent

    private val recycledCertificates = recycledCertificatesProvider.recycledCertificates

    val listItems: LiveData<List<RecyclerBinItem>> = combine(
        recycledCoronaTestsProvider.tests,
        recycledCertificates
    ) { recycledTests, recycledCertificates ->
        recycledTests.toRecycledItems() +
            recycledCertificates.toRecyclerBinItems()
    }.asLiveData2()

    private fun Collection<CwaCovidCertificate>.toRecyclerBinItems(): List<RecyclerBinItem> {
        val certificateItems = mapNotNull { mapCertToCertItem(it) }

        return when (certificateItems.isNotEmpty()) {
            true -> listOf(OverviewSubHeaderItem).plus(certificateItems)
            false -> emptyList()
        }.also { Timber.d("Created recycler bin items=%s from certs=%s", it, this) }
    }

    private fun Collection<CoronaTest>.toRecycledItems(): List<RecyclerBinItem> {
        // TODO
        return emptyList()
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
        recycledCertificatesProvider.deleteAllCertificate(itemToDelete)
    }

    fun onRemoveItem(item: CwaCovidCertificate) = launch {
        Timber.d("onRemoveSingleItemConfirmation(item=%s)", item)
        recycledCertificatesProvider.deleteCertificate(item.containerId)
    }

    fun onRestoreConfirmation(item: CwaCovidCertificate) = launch {
        Timber.d("onRestoreConfirmation(item=%s)", item)
        recycledCertificatesProvider.restoreCertificate(item.containerId)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RecyclerBinOverviewViewModel>
}
