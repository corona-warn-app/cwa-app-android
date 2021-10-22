package de.rki.coronawarnapp.reyclebin.ui

import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsRepository
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.reyclebin.ui.adapter.OverviewSubHeaderItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecoveryCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecyclerBinItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.TestCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.TestCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.VaccinationCertificateCard
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import timber.log.Timber
import java.lang.IllegalArgumentException

class RecyclerBinOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val recycledCertificatesProvider: RecycledCertificatesProvider,
    recycledCoronaTestsRepository: RecycledCoronaTestsRepository,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val currentEvent = SingleLiveEvent<RecyclerBinEvent>()
    val events: LiveData<RecyclerBinEvent> = currentEvent

    private val recycledCertificates = recycledCertificatesProvider.recycledCertificates
    private val recycledTests = recycledCoronaTestsRepository.tests

    val listItems: LiveData<List<RecyclerBinItem>> = listOf(
        recycledCertificates,
        recycledTests,
    )
        .merge()
        .map { it.toRecyclerBinItems() }
        .asLiveData2()

    private fun Collection<Any>.toRecyclerBinItems(): List<RecyclerBinItem> {
        val recyclerBinItems = mapNotNull {
            when (it) {
                is CwaCovidCertificate -> mapCertToRecyclerBinItem(it)
                is RecycledCoronaTest -> mapTestToRecyclerBinItem(it)
                else -> throw IllegalArgumentException("Can't convert $it to RecyclerBinItem")
            }
        }

        return when (recyclerBinItems.isNotEmpty()) {
            true -> listOf(OverviewSubHeaderItem).plus(recyclerBinItems)
            false -> emptyList()
        }.also { Timber.d("Created recycler bin items=%s from certs=%s", it, this) }
    }

    private fun mapTestToRecyclerBinItem(recycledTest: RecycledCoronaTest): RecyclerBinItem? = TestCard.Item(
        test = recycledTest,
        onRemove = { test, position ->
            currentEvent.postValue(RecyclerBinEvent.RemoveTest(test, position))
        }, onRestore = { test ->
            currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreTest(test))
        }
    )

    private fun mapCertToRecyclerBinItem(cert: CwaCovidCertificate): RecyclerBinItem? = when (cert) {
        is TestCertificate -> TestCertificateCard.Item(
            certificate = cert,
            onRemove = { certificate, position ->
                currentEvent.postValue(RecyclerBinEvent.RemoveCertificate(certificate, position))
            },
            onRestore = { certificate ->
                currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreCertificate(certificate))
            }
        )

        is VaccinationCertificate -> VaccinationCertificateCard.Item(
            certificate = cert,
            onRemove = { certificate, position ->
                currentEvent.postValue(RecyclerBinEvent.RemoveCertificate(certificate, position))
            },
            onRestore = { certificate ->
                currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreCertificate(certificate))
            }
        )

        is RecoveryCertificate -> RecoveryCertificateCard.Item(
            certificate = cert,
            onRemove = { certificate, position ->
                currentEvent.postValue(RecyclerBinEvent.RemoveCertificate(certificate, position))
            },
            onRestore = { certificate ->
                currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreCertificate(certificate))
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
