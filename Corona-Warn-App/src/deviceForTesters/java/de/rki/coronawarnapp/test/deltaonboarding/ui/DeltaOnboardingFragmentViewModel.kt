package de.rki.coronawarnapp.test.deltaonboarding.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.ui.ContactDiarySettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DeltaOnboardingFragmentViewModel @AssistedInject constructor(
    private val settings: CWASettings,
    private val traceLocationSettings: TraceLocationSettings,
    private val contactDiarySettings: ContactDiarySettings,
    private val covidCertificateSettings: CovidCertificateSettings,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val changelogVersion: LiveData<Long> =
        settings.lastChangelogVersion.flow.asLiveData(context = dispatcherProvider.Default)

    fun updateChangelogVersion(value: Long) {
        settings.lastChangelogVersion.update { value }
    }

    fun resetChangelogVersion() {
        settings.lastChangelogVersion.update { BuildConfigWrap.VERSION_CODE }
    }

    fun clearChangelogVersion() {
        settings.lastChangelogVersion.update { 1 }
    }

    fun isContactJournalOnboardingDone() = contactDiarySettings.isOnboardingDone

    fun setContactJournalOnboardingDone(value: Boolean) {
        contactDiarySettings.onboardingStatus = if (value)
            ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12
        else
            ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED
    }

    fun isDeltaOnboardingDone() = settings.wasInteroperabilityShownAtLeastOnce

    fun setDeltaOnboardingDone(value: Boolean) {
        settings.wasInteroperabilityShownAtLeastOnce = value
    }

    fun isAttendeeOnboardingDone() =
        traceLocationSettings.onboardingStatus.value == TraceLocationSettings.OnboardingStatus.ONBOARDED_2_0

    fun setAttendeeOnboardingDone(value: Boolean) {
        traceLocationSettings.onboardingStatus.update {
            if (value) TraceLocationSettings.OnboardingStatus.ONBOARDED_2_0
            else TraceLocationSettings.OnboardingStatus.NOT_ONBOARDED
        }
    }

    fun isVaccinationRegistrationOnboardingDone() = covidCertificateSettings.isOnboarded.value

    fun setVaccinationRegistrationOnboardingDone(value: Boolean) {
        covidCertificateSettings.isOnboarded.update { value }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DeltaOnboardingFragmentViewModel>
}
