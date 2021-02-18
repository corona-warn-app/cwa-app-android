package de.rki.coronawarnapp.ui.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingLoadingViewModel @AssistedInject constructor(private val cwaSettings: CWASettings) : CWAViewModel() {

    val popupEvents = SingleLiveEvent<OnboardingFragmentEvents>()

    fun showPopUpsOrNavigate() {
        when {
            !LocalData.isOnboarded() -> {
                popupEvents.postValue(OnboardingFragmentEvents.ShowOnboarding)
            }
            !LocalData.isInteroperabilityShownAtLeastOnce -> {
                popupEvents.postValue(OnboardingFragmentEvents.ShowInteropDeltaOnboarding)
            }
            cwaSettings.lastChangelogVersion.value < BuildConfigWrap.VERSION_CODE -> {
                popupEvents.postValue(OnboardingFragmentEvents.ShowNewReleaseFragment)
            }
            else -> {
                popupEvents.postValue(OnboardingFragmentEvents.OnboardingDone)
            }
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingLoadingViewModel>
}

sealed class OnboardingFragmentEvents {

    object ShowInteropDeltaOnboarding : OnboardingFragmentEvents()

    object ShowNewReleaseFragment : OnboardingFragmentEvents()

    object ShowOnboarding : OnboardingFragmentEvents()

    object OnboardingDone : OnboardingFragmentEvents()
}
