package de.rki.coronawarnapp.ui.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class OnboardingViewModel @AssistedInject constructor(appConfigProvider: AppConfigProvider) : CWAViewModel() {
    val maxEncounterAgeInDays = appConfigProvider.currentConfig.map { it.maxEncounterAgeInDays }.asLiveData2()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingViewModel>
}
