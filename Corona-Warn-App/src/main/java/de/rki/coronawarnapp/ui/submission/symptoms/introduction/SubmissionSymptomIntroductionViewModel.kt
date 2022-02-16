package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class SubmissionSymptomIntroductionViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    private val autoSubmission: AutoSubmission,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    @Assisted private val testType: Type
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val symptomIndicationInternal = MutableStateFlow<Symptoms.Indication?>(null)
    val symptomIndication = symptomIndicationInternal.asLiveData(context = dispatcherProvider.Default)

    val navigation = SingleLiveEvent<NavDirections>()

    val showCancelDialog = SingleLiveEvent<Unit>()
    private val mediatorShowUploadDialog = MediatorLiveData<Boolean>()

    init {
        mediatorShowUploadDialog.addSource(
            autoSubmission.isSubmissionRunning.asLiveData(context = dispatcherProvider.Default)
        ) { show ->
            mediatorShowUploadDialog.postValue(show)
        }
    }

    val showUploadDialog: LiveData<Boolean> = mediatorShowUploadDialog

    fun onNextClicked() {
        launch {
            when (symptomIndicationInternal.value) {
                Symptoms.Indication.POSITIVE -> {
                    navigation.postValue(
                        SubmissionSymptomIntroductionFragmentDirections
                            .actionSubmissionSymptomIntroductionFragmentToSubmissionSymptomCalendarFragment(
                                symptomIndication = Symptoms.Indication.POSITIVE,
                                testType = testType
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

                    navigation.postValue(
                        SubmissionSymptomIntroductionFragmentDirections
                            .actionSubmissionSymptomIntroductionFragmentToSubmissionDoneFragment(testType)
                    )
                }
                Symptoms.Indication.NO_INFORMATION -> {
                    submissionRepository.currentSymptoms.update {
                        Symptoms(
                            startOfSymptoms = null,
                            symptomIndication = Symptoms.Indication.NO_INFORMATION
                        )
                    }
                    doSubmit()

                    navigation.postValue(
                        SubmissionSymptomIntroductionFragmentDirections
                            .actionSubmissionSymptomIntroductionFragmentToSubmissionDoneFragment(testType)
                    )
                }
                else -> Unit
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
                autoSubmission.runSubmissionNow(testType)
            } catch (e: Exception) {
                Timber.e(e, "doSubmit() failed.")
            } finally {
                Timber.i("Hide uploading progress and navigate to HomeFragment")
                mediatorShowUploadDialog.postValue(false)
                navigation.postValue(
                    SubmissionSymptomIntroductionFragmentDirections
                        .actionSubmissionSymptomIntroductionFragmentToMainFragment()
                )
            }
        }
    }

    fun onNewUserActivity() {
        Timber.d("onNewUserActivity()")
        analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS, testType)
        autoSubmission.updateLastSubmissionUserActivity()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionSymptomIntroductionViewModel> {
        fun create(testType: Type): SubmissionSymptomIntroductionViewModel
    }
}
