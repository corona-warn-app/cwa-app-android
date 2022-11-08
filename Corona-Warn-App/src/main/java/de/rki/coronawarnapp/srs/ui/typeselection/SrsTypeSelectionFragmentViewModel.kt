package de.rki.coronawarnapp.srs.ui.typeselection

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SrsTypeSelectionFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SrsTypeSelectionFragmentViewModel>
}
