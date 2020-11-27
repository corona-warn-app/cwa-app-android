package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SubmissionSymptomIntroductionViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val symptomIndicationInternal = MutableStateFlow<Symptoms.Indication?>(null)
    val symptomIndication = symptomIndicationInternal
        .asLiveData(context = dispatcherProvider.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val showCancelDialog = SingleLiveEvent<Unit>()

    fun onNextClicked() {
        launch {
            when (symptomIndicationInternal.first()) {
                Symptoms.Indication.POSITIVE -> SubmissionNavigationEvents.NavigateToSymptomCalendar(
                    Symptoms.Indication.POSITIVE
                )
                Symptoms.Indication.NEGATIVE -> SubmissionNavigationEvents.NavigateToResultPositiveOtherWarning(
                    symptoms = Symptoms(
                        startOfSymptoms = null,
                        symptomIndication = Symptoms.Indication.NEGATIVE
                    )
                )
                else -> SubmissionNavigationEvents.NavigateToResultPositiveOtherWarning(
                    symptoms = Symptoms.NO_INFO_GIVEN
                )
            }.let { routeToScreen.postValue(it) }
        }
    }

    fun onPreviousClicked() {
        showCancelDialog.postValue(Unit)
    }

    fun onPositiveSymptomIndication() {
        symptomIndicationInternal.value = Symptoms.Indication.POSITIVE
    }

    fun onNegativeSymptomIndication() {
        symptomIndicationInternal.value = Symptoms.Indication.NEGATIVE
    }

    fun onNoInformationSymptomIndication() {
        symptomIndicationInternal.value = Symptoms.Indication.NO_INFORMATION
    }

    fun cancelSymptomSubmission() {
        Timber.d("Symptom submission was cancelled.")
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToTestResult)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionSymptomIntroductionViewModel>
}
