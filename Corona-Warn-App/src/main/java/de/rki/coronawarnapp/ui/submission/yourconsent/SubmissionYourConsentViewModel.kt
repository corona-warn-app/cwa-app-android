package de.rki.coronawarnapp.ui.submission.yourconsent

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionYourConsentViewModel @AssistedInject constructor(
    val dispatcherProvider: DispatcherProvider,
    interoperabilityRepository: InteroperabilityRepository,
    val submissionRepository: SubmissionRepository,
    @Assisted private val testType: BaseCoronaTest.Type
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        Timber.v("init() coronaTestType=%s", testType)
    }

    val clickEvent: SingleLiveEvent<SubmissionYourConsentEvents> = SingleLiveEvent()
    val errorEvent = SingleLiveEvent<Throwable>()
    private val consentFlow = submissionRepository.testForType(type = testType)
        .filterNotNull()
        .map { it.isAdvancedConsentGiven }
    val consent = consentFlow.asLiveData(context = dispatcherProvider.Default)

    val countryList = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)

    fun goBack() {
        clickEvent.postValue(SubmissionYourConsentEvents.GoBack)
    }

    fun switchConsent() = launch {
        try {
            if (consentFlow.first()) {
                Timber.v("revokeConsentToSubmission()")
                submissionRepository.revokeConsentToSubmission(type = testType)
            } else {
                Timber.v("giveConsentToSubmission()")
                submissionRepository.giveConsentToSubmission(type = testType)
            }
        } catch (e: Exception) {
            Timber.e(e, "switchConsent()")
            errorEvent.postValue(e)
        }
    }

    fun goLegal() {
        clickEvent.postValue(SubmissionYourConsentEvents.GoLegal)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionYourConsentViewModel> {
        fun create(testType: BaseCoronaTest.Type): SubmissionYourConsentViewModel
    }
}
