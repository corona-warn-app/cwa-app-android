package de.rki.coronawarnapp.test.deltaonboarding.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DeltaOnboardingFragmentViewModel @AssistedInject constructor(
    private val settings: CWASettings,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val changelogVersion: LiveData<Long> = settings.lastChangelogVersion.flow.asLiveData(context = dispatcherProvider.Default)

    fun updateChangelogVersion(value: Long) {
        settings.lastChangelogVersion.update { value }
    }

    fun resetChangelogVersion() {
        settings.lastChangelogVersion.update { BuildConfigWrap.VERSION_CODE }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DeltaOnboardingFragmentViewModel>
}
