package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionSymptomIntroductionViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionSettings: SubmissionSettings
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val symptomIndication = submissionSettings.symptoms.flow
        .map { it.symptomIndication }
        .asLiveData(context = dispatcherProvider.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val showCancelDialog = SingleLiveEvent<Unit>()

    fun onNextClicked() {
        launch {
            when (submissionSettings.symptoms.value.symptomIndication) {
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
        updateSymptomIndication(Symptoms.Indication.POSITIVE)
    }

    fun onNegativeSymptomIndication() {
        updateSymptomIndication(Symptoms.Indication.NEGATIVE)
    }

    fun onNoInformationSymptomIndication() {
        updateSymptomIndication(Symptoms.Indication.NO_INFORMATION)
    }

    private fun updateSymptomIndication(indication: Symptoms.Indication) {
        Timber.d("updateSymptomIndication(indication=$indication)")
        submissionSettings.symptoms.update {
            it.copy(symptomIndication = indication)
        }
    }

    fun cancelSymptomSubmission() {
        Timber.d("Symptom submission was cancelled.")
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToTestResult)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionSymptomIntroductionViewModel>
}
