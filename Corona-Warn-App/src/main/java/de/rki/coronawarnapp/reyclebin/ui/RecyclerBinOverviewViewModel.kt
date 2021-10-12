package de.rki.coronawarnapp.reyclebin.ui

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class RecyclerBinOverviewViewModel @AssistedInject constructor() : CWAViewModel() {
    // TODO: Implement the ViewModel

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RecyclerBinOverviewViewModel>
}
