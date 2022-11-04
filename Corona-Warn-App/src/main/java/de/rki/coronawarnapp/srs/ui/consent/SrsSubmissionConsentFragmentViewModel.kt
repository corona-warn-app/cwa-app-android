package de.rki.coronawarnapp.srs.ui.consent

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SrsSubmissionConsentFragmentViewModel @AssistedInject constructor(
    @Assisted private val srsSubmissionType: SrsSubmissionType?,
    @Assisted private val inAppResult: Boolean,
    private val checkInRepository: CheckInRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<SrsSubmissionConsentNavigationEvents>()
    fun onDataPrivacyClick() {
        routeToScreen.postValue(SrsSubmissionConsentNavigationEvents.NavigateToDataPrivacy)
    }

    fun onConsentDialogConfirmed() = launch {
        if (!inAppResult) {
            Timber.tag(TAG).d("Navigate to TestType")
            routeToScreen.postValue(SrsSubmissionConsentNavigationEvents.NavigateToTestType)
        } else {
            val completedCheckInsExist = checkInRepository.completedCheckIns.first().isNotEmpty()
            val navDirections = if (completedCheckInsExist) {
                Timber.tag(TAG).d("Navigate to ShareCheckins")
                SrsSubmissionConsentNavigationEvents.NavigateToShareCheckins
            } else {
                Timber.tag(TAG).d("Navigate to ShareSymptoms")
                SrsSubmissionConsentNavigationEvents.NavigateToShareSymptoms
            }
            routeToScreen.postValue(navDirections)
        }
    }

    fun onConsentCancel() {
        routeToScreen.postValue(SrsSubmissionConsentNavigationEvents.NavigateToMainScreen)
    }

    fun proceed() {
        // TODO: Trigger keys sharing here when logic is implemented
        onConsentDialogConfirmed()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SrsSubmissionConsentFragmentViewModel> {
        fun create(
            srsSubmissionType: SrsSubmissionType?,
            inAppResult: Boolean
        ): SrsSubmissionConsentFragmentViewModel
    }
    companion object {
        private const val TAG = "SrsSubmissionConsentFragmentViewModel"
    }
}
