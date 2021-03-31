package de.rki.coronawarnapp.test.eventregistration.ui

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.warning.download.TraceTimeWarningPackageSyncTool
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val syncTool: TraceTimeWarningPackageSyncTool
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    fun downloadWarningPackages() {
        launch {
            Timber.d("downloadWarningPackages()")
            syncTool.syncPackages()
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<EventRegistrationTestFragmentViewModel>
}
