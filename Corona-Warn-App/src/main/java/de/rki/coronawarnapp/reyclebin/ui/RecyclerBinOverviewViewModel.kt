package de.rki.coronawarnapp.reyclebin.ui

import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.reyclebin.RecycledItemsProvider
import de.rki.coronawarnapp.reyclebin.ui.adapter.OverviewSubHeaderItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecoveryCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecyclerBinItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.TestCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.VaccinationCertificateCard
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class RecyclerBinOverviewViewModel @AssistedInject constructor(
    recycledItemsProvider: RecycledItemsProvider
) : CWAViewModel() {

    val events = SingleLiveEvent<RecyclerBinEvent>()

    val listItems: LiveData<List<RecyclerBinItem>> = recycledItemsProvider.recycledCertificates.map { certificates ->
        val certificateItems = mutableListOf<RecyclerBinItem>().apply {
            certificates.forEach {
                when (it) {
                    is TestCertificate -> add(
                        TestCertificateCard.Item(
                            certificate = it,
                            onRemove = { certificate, position ->
                                events.postValue(RecyclerBinEvent.ConfirmRemoveItem(certificate, position))
                            },
                            onRestore = { certificate ->
                                events.postValue(RecyclerBinEvent.ConfirmRestoreItem(certificate))
                            }
                        )
                    )
                    is VaccinationCertificate -> add(
                        VaccinationCertificateCard.Item(
                            certificate = it,
                            onRemove = { certificate, position ->
                                events.postValue(RecyclerBinEvent.ConfirmRemoveItem(certificate, position))
                            },
                            onRestore = { certificate ->
                                events.postValue(RecyclerBinEvent.ConfirmRestoreItem(certificate))
                            }
                        )
                    )
                    is RecoveryCertificate -> add(
                        RecoveryCertificateCard.Item(
                            certificate = it,
                            onRemove = { certificate, position ->
                                events.postValue(RecyclerBinEvent.ConfirmRemoveItem(certificate, position))
                            },
                            onRestore = { certificate ->
                                events.postValue(RecyclerBinEvent.ConfirmRestoreItem(certificate))
                            }
                        )
                    )
                }
            }
        }
        if (certificateItems.isNotEmpty()) {
            listOf(OverviewSubHeaderItem).plus(certificateItems)
        } else {
            emptyList()
        }
    }.asLiveData2()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RecyclerBinOverviewViewModel>
}
