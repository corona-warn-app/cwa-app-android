package de.rki.coronawarnapp.ui.submission.qrcode.consent

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionConsentViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    interoperabilityRepository: InteroperabilityRepository
) : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val countries = interoperabilityRepository.countryListFlow.asLiveData()

    fun onConsentButtonClick() {
        submissionRepository.giveConsentToSubmission()
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToQRCodeScan)
    }

    fun onBackButtonClick() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDispatcher)
    }

    fun onDataPrivacyClick() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDataPrivacy)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionConsentViewModel>
}
