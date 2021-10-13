package de.rki.coronawarnapp.reyclebin.ui

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.reyclebin.RecycledItemsProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class RecyclerBinOverviewViewModel @AssistedInject constructor(
    private val recycledItemsProvider: RecycledItemsProvider
) : CWAViewModel() {

    val recycledCertificates = recycledItemsProvider.recycledCertificates.asLiveData2()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RecyclerBinOverviewViewModel>
}
