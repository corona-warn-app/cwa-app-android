package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
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
    private val submissionRepository: SubmissionRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val symptomIndication = submissionRepository.currentSymptoms.flow
        .map { it.symptomIndication }
        .asLiveData(context = dispatcherProvider.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val showCancelDialog = SingleLiveEvent<Unit>()
    val showUploadDialog = submissionRepository.isSubmissionRunning
        .asLiveData(context = dispatcherProvider.Default)

    fun onNextClicked() {
        launch {
            when (submissionRepository.currentSymptoms.value.symptomIndication) {
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
        submissionRepository.currentSymptoms.update {
            it.copy(symptomIndication = indication)
        }
    }

    fun onCancelConfirmed() {
        Timber.d("Symptom submission was cancelled.")
        launch {
            try {
                submissionRepository.startSubmission()
            } catch (e: Exception) {
                Timber.e(e, "onCancelConfirmed() failed.")
            } finally {
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
            }
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionSymptomIntroductionViewModel>
}
