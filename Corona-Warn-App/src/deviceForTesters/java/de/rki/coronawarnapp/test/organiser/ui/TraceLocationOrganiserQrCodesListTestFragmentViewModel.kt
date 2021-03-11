package de.rki.coronawarnapp.test.organiser.ui

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TraceLocationOrganiserQrCodesListTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {


    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TraceLocationOrganiserQrCodesListTestFragmentViewModel>
}
