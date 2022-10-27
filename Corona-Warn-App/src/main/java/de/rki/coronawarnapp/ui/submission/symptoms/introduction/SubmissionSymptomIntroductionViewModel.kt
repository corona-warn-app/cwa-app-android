package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SubmissionSymptomIntroductionViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    private val autoSubmission: AutoSubmission,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    @Assisted private val testType: Type,
    @Assisted private val comesFromDispatcherFragment: Boolean,
    @AppScope private val appScope: CoroutineScope,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val symptomIndicationInternal = MutableStateFlow<Symptoms.Indication?>(null)
    val symptomIndication = symptomIndicationInternal.asLiveData(context = dispatcherProvider.Default)

    val navigation = SingleLiveEvent<NavDirections>()

    val navigateBack = SingleLiveEvent<Unit>()

    val showCancelDialog = SingleLiveEvent<Unit>()

    fun onNextClicked() {
        launch {
            when (symptomIndicationInternal.value) {
                Symptoms.Indication.POSITIVE -> {
                    navigation.postValue(
                        SubmissionSymptomIntroductionFragmentDirections
                            .actionSubmissionSymptomIntroductionFragmentToSubmissionSymptomCalendarFragment(
                                symptomIndication = Symptoms.Indication.POSITIVE,
                                testType = testType,
                                comesFromDispatcherFragment = comesFromDispatcherFragment
                            )
                    )
                }
                Symptoms.Indication.NEGATIVE -> {
                    submissionRepository.updateCurrentSymptoms(
                        Symptoms(
                            startOfSymptoms = null,
                            symptomIndication = Symptoms.Indication.NEGATIVE
                        )
                    )
                    performSubmission()
                    navigation.postValue(
                        SubmissionSymptomIntroductionFragmentDirections
                            .actionSubmissionSymptomIntroductionFragmentToSubmissionDoneFragment(testType)
                    )
                }
                Symptoms.Indication.NO_INFORMATION -> {
                    submissionRepository.updateCurrentSymptoms(
                        Symptoms(
                            startOfSymptoms = null,
                            symptomIndication = Symptoms.Indication.NO_INFORMATION
                        )
                    )
                    performSubmission()
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
        performSubmission()
        if (comesFromDispatcherFragment) {
            navigation.postValue(
                SubmissionSymptomIntroductionFragmentDirections
                    .actionSubmissionSymptomIntroductionFragmentToMainFragment()
            )
        } else navigateBack.postValue(Unit)
    }

    private fun performSubmission() {
        appScope.launch {
            try {
                autoSubmission.runSubmissionNow(testType)
            } catch (e: Exception) {
                Timber.e(e, "performSubmission() failed.")
            }
        }
    }

    fun onNewUserActivity() {
        launch {
            Timber.d("onNewUserActivity()")
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOMS, testType)
            autoSubmission.updateLastSubmissionUserActivity()
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionSymptomIntroductionViewModel> {
        fun create(
            testType: Type,
            comesFromDispatcherFragment: Boolean
        ): SubmissionSymptomIntroductionViewModel
    }
}
