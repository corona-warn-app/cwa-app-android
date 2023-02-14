package de.rki.coronawarnapp.ui.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class OnboardingLoadingViewModel @Inject constructor(
    private val cwaSettings: CWASettings,
    private val onboardingSettings: OnboardingSettings
) : CWAViewModel() {

    val navigationEvents = SingleLiveEvent<OnboardingFragmentEvents>()

    fun navigate() = launch {
        when {
            !onboardingSettings.isOnboarded() -> {
                navigationEvents.postValue(OnboardingFragmentEvents.ShowOnboarding)
            }

            !cwaSettings.wasInteroperabilityShownAtLeastOnce.first() -> {
                navigationEvents.postValue(OnboardingFragmentEvents.ShowInteropDeltaOnboarding)
            }

            cwaSettings.lastChangelogVersion.first() / 10000 < BuildConfigWrap.VERSION_CODE / 10000 -> {
                navigationEvents.postValue(OnboardingFragmentEvents.ShowNewReleaseFragment)
            }

            else -> {
                navigationEvents.postValue(OnboardingFragmentEvents.OnboardingDone)
            }
        }
    }
}

sealed class OnboardingFragmentEvents {

    object ShowInteropDeltaOnboarding : OnboardingFragmentEvents()

    object ShowNewReleaseFragment : OnboardingFragmentEvents()

    object ShowOnboarding : OnboardingFragmentEvents()

    object OnboardingDone : OnboardingFragmentEvents()
}
