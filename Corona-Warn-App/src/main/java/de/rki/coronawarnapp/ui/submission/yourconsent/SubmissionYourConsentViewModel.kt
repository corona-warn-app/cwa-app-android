package de.rki.coronawarnapp.ui.submission.yourconsent

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionYourConsentViewModel @AssistedInject constructor(
    val dispatcherProvider: DispatcherProvider,
    interoperabilityRepository: InteroperabilityRepository,
    val submissionRepository: SubmissionRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    // TODO Use navargs to supply this
    private val coronaTestType: CoronaTest.Type = CoronaTest.Type.PCR

    init {
        Timber.v("init() coronaTestType=%s", coronaTestType)
    }

    val clickEvent: SingleLiveEvent<SubmissionYourConsentEvents> = SingleLiveEvent()
    private val consentFlow = submissionRepository.testForType(type = coronaTestType)
        .filterNotNull()
        .map { it.isAdvancedConsentGiven }
    val consent = consentFlow.asLiveData(context = dispatcherProvider.Default)

    val countryList = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)

    fun goBack() {
        clickEvent.postValue(SubmissionYourConsentEvents.GoBack)
    }

    fun switchConsent() = launch {
        if (consentFlow.first()) {
            Timber.v("revokeConsentToSubmission()")
            submissionRepository.revokeConsentToSubmission(type = coronaTestType)
        } else {
            Timber.v("giveConsentToSubmission()")
            submissionRepository.giveConsentToSubmission(type = coronaTestType)
        }
    }

    fun goLegal() {
        clickEvent.postValue(SubmissionYourConsentEvents.GoLegal)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionYourConsentViewModel>
}
