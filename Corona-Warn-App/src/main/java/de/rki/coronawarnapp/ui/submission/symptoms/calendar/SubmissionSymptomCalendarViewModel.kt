package de.rki.coronawarnapp.ui.submission.symptoms.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type
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
import java.time.LocalDate
import timber.log.Timber

class SubmissionSymptomCalendarViewModel @AssistedInject constructor(
    @Assisted val symptomIndication: Symptoms.Indication,
    @Assisted val testType: Type,
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    private val autoSubmission: AutoSubmission,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val symptomStartInternal = MutableStateFlow<Symptoms.StartOf?>(null)
    val symptomStart = symptomStartInternal.asLiveData(context = dispatcherProvider.Default)

    val routeToScreen = SingleLiveEvent<NavDirections>()
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

    fun onLastSevenDaysStart() {
        updateSymptomStart(Symptoms.StartOf.LastSevenDays)
    }

    fun onOneToTwoWeeksAgoStart() {
        updateSymptomStart(Symptoms.StartOf.OneToTwoWeeksAgo)
    }

    fun onMoreThanTwoWeeksStart() {
        updateSymptomStart(Symptoms.StartOf.MoreThanTwoWeeks)
    }

    fun onNoInformationStart() {
        updateSymptomStart(Symptoms.StartOf.NoInformation)
    }

    fun onDateSelected(localDate: LocalDate?) {
        updateSymptomStart(localDate?.let { Symptoms.StartOf.Date(it) })
    }

    private fun updateSymptomStart(startOf: Symptoms.StartOf?) {
        symptomStartInternal.value = startOf
    }

    fun onCalendarPreviousClicked() {
        showCancelDialog.postValue(Unit)
    }

    fun onDone() {
        if (symptomStartInternal.value == null) {
            IllegalStateException("Can't finish symptom indication without symptomStart value.")
                .reportProblem(tag = TAG, "UI should not allow symptom submission without start date.")
            return
        }
        Timber.tag(TAG).d("onDone() clicked on calender screen.")
        submissionRepository.currentSymptoms.update {
            Symptoms(
                symptomIndication = symptomIndication,
                startOfSymptoms = symptomStartInternal.value
            ).also { Timber.tag(TAG).v("Symptoms updated to %s", it) }
        }
        performSubmission {
            analyticsKeySubmissionCollector.reportSubmittedAfterSymptomFlow(testType)
        }
    }

    fun onCancelConfirmed() {
        Timber.d("onCancelConfirmed() clicked on calendar screen.")
        performSubmission {
            analyticsKeySubmissionCollector.reportSubmittedAfterCancel(testType)
        }
    }

    private fun performSubmission(onSubmitted: () -> Unit) {
        launch {
            try {
                autoSubmission.runSubmissionNow(testType)
                onSubmitted()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "performSubmission() failed.")
            } finally {
                Timber.i("Hide uploading progress and navigate to HomeFragment")
                mediatorShowUploadDialog.postValue(false)
                routeToScreen.postValue(
                    SubmissionSymptomCalendarFragmentDirections
                        .actionSubmissionSymptomCalendarFragmentToSubmissionDoneFragment(testType)
                )
            }
        }
    }

    fun onNewUserActivity() {
        Timber.d("onNewUserActivity()")
        analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.SYMPTOM_ONSET, testType)
        autoSubmission.updateLastSubmissionUserActivity()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionSymptomCalendarViewModel> {

        fun create(symptomIndication: Symptoms.Indication, testType: Type): SubmissionSymptomCalendarViewModel
    }

    companion object {
        private const val TAG = "SymptomsCalenderVM"
    }
}
