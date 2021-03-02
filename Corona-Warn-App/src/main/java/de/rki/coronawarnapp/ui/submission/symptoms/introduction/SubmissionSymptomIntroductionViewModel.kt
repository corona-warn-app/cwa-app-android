package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class SubmissionSymptomIntroductionViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    private val autoSubmission: AutoSubmission,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val symptomIndicationInternal = MutableStateFlow<Symptoms.Indication?>(null)
    val symptomIndication = symptomIndicationInternal.asLiveData(context = dispatcherProvider.Default)

    val navigation = SingleLiveEvent<NavDirections>()

    val showCancelDialog = SingleLiveEvent<Unit>()
    val showUploadDialog = autoSubmission.isSubmissionRunning
        .asLiveData(context = dispatcherProvider.Default)

    fun onNextClicked() {
        launch {
            when (symptomIndicationInternal.value) {
                Symptoms.Indication.POSITIVE -> {
                    navigation.postValue(
                        SubmissionSymptomIntroductionFragmentDirections
                            .actionSubmissionSymptomIntroductionFragmentToSubmissionSymptomCalendarFragment(
                                symptomIndication = Symptoms.Indication.POSITIVE
                            )
                    )
                }
                Symptoms.Indication.NEGATIVE -> {
                    submissionRepository.currentSymptoms.update {
                        Symptoms(
                            startOfSymptoms = null,
                            symptomIndication = Symptoms.Indication.NEGATIVE
                        )
                    }
                    doSubmit()
                }
                Symptoms.Indication.NO_INFORMATION -> {
                    submissionRepository.currentSymptoms.update {
                        Symptoms(
                            startOfSymptoms = null,
                            symptomIndication = Symptoms.Indication.NO_INFORMATION
                        )
                    }
                    doSubmit()
                }
            }
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
        symptomIndicationInternal.value = indication
    }

    fun onCancelConfirmed() {
        Timber.d("Symptom submission was cancelled.")
        doSubmit()
    }

    private fun doSubmit() {
        launch {
            try {
                autoSubmission.runSubmissionNow()
            } catch (e: Exception) {
                Timber.e(e, "doSubmit() failed.")
            } finally {
                navigation.postValue(
                    SubmissionSymptomIntroductionFragmentDirections
                        .actionSubmissionSymptomIntroductionFragmentToMainFragment()
                )
            }
        }
    }

    fun onNewUserActivity() {
        Timber.d("onNewUserActivity()")
        analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS)
        autoSubmission.updateLastSubmissionUserActivity()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionSymptomIntroductionViewModel>
}
