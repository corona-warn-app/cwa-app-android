package de.rki.coronawarnapp.test.deltaonboarding.ui

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettings
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryUiSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DeltaOnboardingFragmentViewModel @AssistedInject constructor(
    private val settings: CWASettings,
    private val analyticsSettings: AnalyticsSettings,
    private val traceLocationSettings: TraceLocationSettings,
    private val contactDiaryUiSettings: ContactDiaryUiSettings,
    private val covidCertificateSettings: CovidCertificateSettings,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val changelogVersion = settings.lastChangelogVersion.asLiveData2()
    val isContactJournalOnboardingDone = contactDiaryUiSettings.isOnboardingDone.asLiveData2()
    val lastNotificationsOnboardingVersionCode = settings.lastNotificationsOnboardingVersionCode.asLiveData2()
    val isAnalyticsOnboardingDone = analyticsSettings.lastOnboardingVersionCode.asLiveData2()
    val isVaccinationRegistrationOnboardingDone = covidCertificateSettings.isOnboarded.asLiveData2()
    val isAttendeeOnboardingDone = traceLocationSettings.onboardingStatus.asLiveData2()

    fun updateChangelogVersion(value: Long) {
        launch { settings.updateLastChangelogVersion(value) }
    }

    fun resetChangelogVersion() {
        launch { settings.updateLastChangelogVersion(BuildConfigWrap.VERSION_CODE) }
    }

    fun clearChangelogVersion() {
        launch { settings.updateLastChangelogVersion(1) }
    }

    fun setContactJournalOnboardingDone(value: Boolean) = launch {
        when (value) {
            true -> ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12
            false -> ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED
        }.also { contactDiaryUiSettings.updateOnboardingStatus(onboardingStatus = it) }
    }

    val isDeltaOnboardingDone = settings.wasInteroperabilityShownAtLeastOnce.asLiveData2()

    fun setDeltaOnboardingDone(value: Boolean) {
        launch { settings.updateWasInteroperabilityShownAtLeastOnce(value) }
    }

    fun setAttendeeOnboardingDone(value: Boolean) = launch {
        traceLocationSettings.updateOnboardingStatus(
            if (value) TraceLocationSettings.OnboardingStatus.ONBOARDED_2_0
            else TraceLocationSettings.OnboardingStatus.NOT_ONBOARDED
        )
    }

    fun setVaccinationRegistrationOnboardingDone(value: Boolean) = launch {
        covidCertificateSettings.updateIsOnboarded(value)
    }

    fun setNotificationsOnboardingDone(value: Boolean) = launch {
        val version = if (value) BuildConfigWrap.VERSION_CODE else 0L
        settings.updateLastNotificationsOnboardingVersionCode(version)
    }

    fun setAnalyticsOnboardingDone(value: Boolean) = launch {
        analyticsSettings.updateLastOnboardingVersionCode(if (value) BuildConfigWrap.VERSION_CODE else 0L)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DeltaOnboardingFragmentViewModel>
}
